import os
import binascii
import datetime
from flask import Flask, request, json, jsonify, abort, g, flash, url_for, send_file, redirect
from flask_sqlalchemy import SQLAlchemy
from functools import wraps
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
from werkzeug import SharedDataMiddleware
from itsdangerous import (TimedJSONWebSignatureSerializer
                          as Serializer, BadSignature, SignatureExpired)

app = Flask(__name__)

if 'NAMESPACE' in os.environ and os.environ['NAMESPACE'] == 'heroku':
    db_uri = os.environ['DATABASE_URL']
    debug_flag = False
else:
    db_path = os.path.join(os.path.dirname(__file__), 'app.db')
    db_uri = 'sqlite:///{}'.format(db_path)
    debug_flag = True

app.config["SQLALCHEMY_DATABASE_URI"] = db_uri

app.config['SECRET_KEY'] = 'i folded my soldier well in his blanket'

db = SQLAlchemy(app)

APPLICATION_URL = "https://looking-for-group-boardgames.herokuapp.com"

SECONDS_IN_ONE_WEEK = 604800
ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg', 'gif']

read_by_table = db.Table('read_by', db.metadata,
                         db.Column('message_id', db.String, db.ForeignKey('message.id')),
                         db.Column('user_id', db.Integer, db.ForeignKey('user.id')))

comments_table = db.Table('comments', db.metadata,
                          db.Column('message_id', db.String, db.ForeignKey('message.id')),
                          db.Column('match_id', db.Integer, db.ForeignKey('match.id')))

matches_table = db.Table('matches_table', db.metadata,
                         db.Column('played', db.Integer, db.ForeignKey('match.id')),
                         db.Column('played_by', db.Integer, db.ForeignKey('user.id')))

follow_table = db.Table('follow', db.metadata,
                           db.Column('follower_id', db.Integer, db.ForeignKey('user.id'), primary_key=True),
                           db.Column('followed_id', db.Integer, db.ForeignKey('user.id'), primary_key=True))

''' DOING SOME UPLOAD MAGIC '''
app.config["UPLOAD_FOLDER"] = "./photos"
app.add_url_rule('/photos/<filename>', 'uploaded_file', build_only=True)

app.wsgi_app = SharedDataMiddleware(app.wsgi_app, {
    'opt/app-root/src/photos': app.config['UPLOAD_FOLDER']
})


class Token(db.Model):
    __tablename__ = 'token'
    token = db.Column(db.String(200), primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

    def __init__(self, token, user_id):
        self.token = token
        self.user_id = user_id

    def __repr__(self):
        return self.token


class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(127), unique=True, nullable=False)
    name = db.Column(db.String, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    picture = db.Column(db.String, nullable=True)

    tokens = db.relationship('Token')
    read = db.relationship('Message',
                           secondary=read_by_table,
                           back_populates='read_by')
    played = db.relationship('Match',
                             secondary=matches_table,
                             back_populates='played_by')

    def __init__(self, email, name, password, image_name=''):
        self.email = email
        self.name = name
        self.password = generate_password_hash(password)
        self.picture = image_name

    def check_password(self, password):
        return check_password_hash(self.password, password)

    def generate_auth_token(self, expiration=SECONDS_IN_ONE_WEEK):
        s = Serializer(app.config['SECRET_KEY'], expires_in=expiration)
        token = Token(s.dumps(self.id).decode('utf-8'), self.id)
        self.tokens.append(token)
        db.session.add(token)
        db.session.commit()

        return str(token)

    def set_picture(self, picture):
        self.picture = picture

    def follow(self, uid):
        db.session.add(db.Follow(follower_id=self.id, followed_id=uid))
        db.session.commit()

    def __repr__(self):
        return self.email


class Message(db.Model):
    __tablename__ = 'message'
    id = db.Column(db.String(24), primary_key=True)
    message = db.Column(db.String(140), unique=False, nullable=False)
    author_id = db.Column(db.Integer, unique=False, nullable=False)
    date = db.Column(db.DateTime, nullable=False)

    posted_on = db.relationship('Match',
                                secondary=comments_table,
                                back_populates='comments')
    read_by = db.relationship('User',
                              secondary=read_by_table,
                              back_populates='read')

    def __init__(self, message, author_id, message_id, match=None):
        self.message = message
        self.id = message_id
        self.author_id = author_id
        self.date = datetime.datetime.now()

        if match:
            self.posted_on.append(match)
            #  Posts message as a comment on the match indicated by match_id

    def __repr__(self):
        return self.message


class Match(db.Model):
    __tablename__ = 'match'
    id = db.Column(db.Integer, primary_key=True)
    max_players = db.Column(db.Integer, nullable=True)  # Value of null implies potentially infinite players
    cur_players = db.Column(db.Integer, nullable=False)
    created_date = db.Column(db.DateTime, nullable=False)
    game_on_date = db.Column(db.DateTime, nullable=True)
    # started_by = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    name_location = db.Column(db.Text, nullable=True)

    game_name = db.Column(db.Text, nullable=True)

    comments = db.relationship('Message',
                               secondary=comments_table,
                               back_populates='posted_on')

    played_by = db.relationship('User',
                                secondary=matches_table,
                                back_populates='played')

    def __init__(self, game_name, max_players, location, uid):
        self.max_players = max_players
        # self.started_by = uid
        self.cur_players = 1

        self.created_date = datetime.datetime.now()

        self.played_by.append(uid)

        self.game_name = game_name

        if type(location) is str:
            self.name_location = location

    def join(self, uid):
        self.played_by.append(uid)

    def __str__(self):
        return str(self.id)

    def __repr__(self):
        # TODO: change this to game and date
        return str(self.id)

    def increment_cur_players(self):
        if self.cur_players < self.max_players:
            self.cur_players += 1
            return True
        return False

    def decrement_cur_players(self):
        if self.cur_players == 1:
            return False
        else:
            self.cur_players -= 1
            return True


def verify_login(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        if request.headers is None:
            return abort(401)
        token = request.headers['Authorization']
        if token is None:
            return abort(401)
        g.user = verify_auth_token(token)
        if g.user is None:
            return abort(401)
        return func(*args, **kwargs)
    return wrapper


def verify_auth_token(token):
    s = Serializer(app.config['SECRET_KEY'])
    try:
        data = s.loads(token)
    except SignatureExpired:
        return None
    except BadSignature:
        return None
    user = User.query.get(data)
    return user


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route("/")
def index():
    return "Message board"


@app.route("/user", methods=["POST"])
def create_user():
    if request.method == "POST":
        if app.config['TESTING']:
            print(request.get_data())

        image = None
        filename = None

        if 'image' in request.files:
            image = request.files['image']

        if image and image.filename != '':
            filename = secure_filename(image.filename)
            image.save(os.path.join(app.config["UPLOAD_FOLDER"], filename))
            print(url_for('uploaded_file', filename=filename))

        try:
            data = json.loads(request.get_data())
        except Exception:
            # Cannot find json.decoder for JSONDecodeError
            data = request.form

        if app.config['TESTING']:
            print(data)
            print("email: " + data.get('email'))
            print("name: " + data.get('name'))
            print("password: " + data.get('password'))

        email = data['email']

        if User.query.filter_by(email=email).first():
            abort(403)
            # 403 error means user already exists. Should be handled in frontend.

        name = data['name']
        password = data['password']

        user = User(email, name, password, filename)
        db.session.add(user)
        db.session.commit()

        if app.config['TESTING']:
            print("uid: " + str(user.id))
            print("email: " + user.email)
            print("name: " + user.name)
            print("password: " + user.password)

        return 'HTTP 200', 200

    else:
        return abort(405)


@app.route("/user/verify", methods=["POST", "GET"])
@verify_login
def verify_user():
    if request.method in ["POST", "GET"]:
        return "HTTP 200", 200
    else:
        return abort(405)


@app.route("/user/login", methods=["POST"])
def login_user():
    if request.method == "POST":
        data = request.get_json()

        if app.config['TESTING']:
            print("json: " + json.dumps(data))

        email = data['email']
        password = data['password']
        user = User.query.filter_by(email=email).first()

        if not user:
            return "email or password incorrect", 400

        elif user.check_password(password):
            token = user.generate_auth_token()
            user_id = user.id

            if app.config['TESTING']:
                print("user: " + user.email)
                print("token: " + token)

            return json.dumps({'token': token, 'id': user_id}), 200

        else:
            return "email or password incorrect", 400

    else:
        return abort(405)


@app.route("/getidwithemail/<user_email>", methods=["GET"])
def get_user_id_with_email(user_email):
    if request.method == "GET":
        user = User.query.filter_by(email=user_email).first()
        if user:
            data = {'id': user.id}
            return json.dumps(data)
        else:
            return abort(403)
    else:
        return abort(405)


@app.route("/user/<user_id>/image", methods=["GET"])
def get_profile_picture(user_id):
    if request.method == "GET":
        user = User.query.get(user_id)

        if user and user.picture:
            file = "." + url_for('uploaded_file', filename=user.picture)
            # This works since url_for requires a relative path
            return send_file(file)

        else:
            return abort(403)
            # The requested user does not exist, or the picture has not been uploaded

    else:
        return abort(405)

"""
@app.route("/getuserwithemail/<user_email>/image", methods=["GET"])
def get_profile_picture_with_email(user_email):
    if request.method == "GET":
        user = User.query.filter_by(email=user_email).first()

        if user and user.picture:
            file = "." + url_for('uploaded_file', filename=user.picture)
            # This works since url_for requires a relative path
            return send_file(file)

        else:
            return abort(403)
            # The requested user does not exist, or the picture has not been uploaded

    else:
        return abort(405)
"""

@app.route("/images/<user_id>", methods=["POST"])
@verify_login
def post_profile_picture(user_id):
    if request.method == "POST":
        if 'image' not in request.files:
            flash('No file part')
            return abort(400)

        image = request.files['image']

        if image and image.filename:
            filename = secure_filename(image.filename)
            image.save(os.path.join(app.config["UPLOAD_FOLDER"], filename))

            if app.config['TESTING']:
                print(url_for('uploaded_file', filename=filename))

            g.user.set_picture(filename)
            db.session.commit()

            return "HTTP 200", 200

    else:
        return abort(405)


@app.route("/user/<user_id>", methods=["GET"])
def view_profile(user_id):
    user = User.query.get(user_id)
    if not user:
        return abort(400)

    if request.method == "GET":
        data = {'email': user.email,
                'name': user.name}

        match_list = []
        for match in Match.query.filter(Match.played_by.any(id=user_id)).all():
            match_data = {'location': match.name_location,
                          'game_name': match.game_name,
                          'created_date': match.created_date,
                          'cur_players': match.cur_players,
                          'max_players': match.max_players,
                          'match_id': match.id,
                          # 'started_by': match.started_by,
                          'game_on_date': match.game_on_date}
            match_list.append(match_data)

        data['matches_played'] = match_list

        # TODO: Fix follows for users
        """
        follows_list = []
        for user in User.query.filter(User.follows.any(id=user_id)).all():
            user_data = {'id': user.id,
                         'email': user.email,
                         'name': user.name,
                         'picture': user.picture}

            follows_list.append(user_data)

        data['follows'] = follows_list
        """

        return json.dumps(data)

    else:
        return abort(405)

"""
@app.route("/getuserwithemail/<user_email>", methods=["GET"])
def view_profile_with_email(user_email):
    user = User.query.filter_by(email=user_email).first()
    if not user:
        return abort(400)

    if request.method == "GET":
        data = {'email': user.email,
                'name': user.name}

        match_list = []
        for match in Match.query.filter(Match.played_by.any(email=user_email)).all():
            match_data = {'location': match.name_location,
                          'game_name': match.game_name,
                          'created_date': match.created_date,
                          'cur_players': match.cur_players,
                          'max_players': match.max_players,
                          'match_id': match.id,
                          # 'started_by': match.started_by,
                          'game_on_date': match.game_on_date}
            match_list.append(match_data)

        data['matches_played'] = match_list

        return json.dumps(data)

    else:
        return abort(405)
"""


@app.route("/getusers/<request_id>/<user_email>", methods=["GET"])
def get_users(request_id, user_email):
    users = User.query.all()
    if not users:
        return abort(400)

    elif request.method == "GET":
        user_list = []
        for user in User.query.filter(User.email.like(user_email+'%')).all():
            user_list.append(user.email)
        
        data = {'id': request_id, 'users': user_list}
        return json.dumps(data)
    else:
        return abort(405)


@app.route("/user/<follow_id>/follow", methods=["POST", "GET"])
@verify_login
def follow_user(follow_id):
    # TODO: This needs testing

    if request.method in ["POST", "GET"]:
        if g.user.id not in User.query.filter(User.follows.any(id=follow_id)).all():
            g.user.follows.append(follow_id)

        else:
            g.user.follows.remove(follow_id)

        return "HTTP 200", 200

    else:
        return abort(405)


@app.route("/user/logout", methods=["POST"])
@verify_login
def logout_user():
    if request.method == "POST":
        headers = request.headers
        token_value = headers["Authorization"]
        token = Token.query.get(token_value)

        if token and token_value == token.token:
            # user_logout_message = "{0} is now logged out on this device".format(str(g.user))
            g.user.tokens.remove(token)
            db.session.delete(token)
            db.session.commit()
            return 'HTTP 200', 200

        return abort(401)

    else:
        return abort(405)


@app.route("/messages/<message_id>", methods=["GET"])
def get_message(message_id):
    messages = Message.query.all()
    if not messages:
        return abort(400)

    elif request.method == "GET":
        message = Message.query.get(message_id)
        # message is type Message
        if message:
            read_by = []
            for usr in message.read_by:
                read_by.append(str(usr))
            return jsonify(read_by=read_by,
                           message=str(message),
                           id=message.id)
        else:
            return abort(400)

    else:
        return abort(405)


@app.route("/messages/<message_id>", methods=["DELETE"])
@verify_login
def delete_message(message_id):
    messages = Message.query.all()
    if not messages:
        return abort(400)
    message = Message.query.get(message_id)
    if not message:
        return abort(400)

    if message.author_id != g.user.id:
        return abort(401)

    elif request.method == "DELETE":
        if message:
            db.session.delete(message)
            db.session.commit()
            return 'HTTP 200', 200
        else:
            return abort(400)

    else:
        return abort(405)


@app.route("/messages", methods=["GET"])
def get_messages():
    if request.method == "GET":
        messages = Message.query.all()
        message_list = []
        # Each message in messages is type Message
        for message in messages:
            read_by = []
            for usr in message.read_by:
                read_by.append(str(usr))
            message_list.append({'read_by': read_by,
                                 'message': str(message),
                                 'id': message.id})
        return json.dumps(message_list)

    else:
        return abort(405)


@app.route("/messages", methods=["POST"])
@verify_login
def post_message():
    if request.method == "POST":
        message = request.get_json()
        if len(message) > 140:
            return abort(400)
        # [2:-1] removes extra symbols added by binascii
        # TODO: control for duplicate message ids
        message_id = str(binascii.b2a_hex(os.urandom(12)))[2:-1]
        db.session.add(Message(message, g.user.id, message_id))
        db.session.commit()
        return "HTTP 200", 200

    else:
        return abort(405)


@app.route("/messages/<message_id>/flag", methods=["POST"])
@verify_login
def flag_as_read(message_id):
    # TODO: change validation to token

    messages = Message.query.all()
    if not messages:
        return abort(400)

    elif request.method == "POST":
        message = Message.query.get(message_id)
        if message:
            user = User.query.filter_by(email=g.user.email).first()
            if user not in message.read_by:
                message.read_by.append(user)
            else:
                message.read_by.remove(user)
            db.session.commit()
            return "HTTP 200", 200
        else:
            return abort(400)

    else:
        return abort(405)


@app.route("/messages/unread", methods=["GET"])
@verify_login
def read_unread_messages():
    if request.method == "GET":
        unread_messages = []
        messages = Message.query.all()
        for message in messages:
            read_by = []
            for usr in message.read_by:
                read_by.append(str(usr))
            if g.user not in read_by:
                unread_messages.append({'read_by': read_by,
                                        'message': str(message),
                                        'id': message.id})
        return json.dumps(unread_messages)

    else:
        return abort(405)


@app.route("/matches", methods=["GET"])
def get_matches():
    matches = Match.query.all()
    if not matches:
        # TODO: Have frontend show a 'no games' window?
        return 'No games available', 200

    if request.method == "GET":
        match_list = []

        for match in matches:
            location = match.name_location    # can still be null
            max_players = match.max_players   # can still be null
            cur_players = match.cur_players
            created_date = match.created_date
            match_id = match.id
            game_name = match.game_name

            match_list.append({'location': location, 'created_date': created_date,
                               'cur_players': cur_players, 'max_players': max_players,
                               'match_id': match_id, 'game_name': game_name})

            if app.config['TESTING']:
                print("location: " + location)
                print("created_date: " + str(created_date))
                print("cur_players: " + str(cur_players))
                print("max_players: " + str(max_players))
                print("match_id: " + str(match_id))
                print("game_name: " + str(game_name))

        return json.dumps(match_list)

    else:
        return abort(405)


@app.route("/matches", methods=["POST"])
@verify_login
def post_match():
    if request.method == "POST":
        data = request.get_json()

        game_name = data['game_name']
        location = data['location']
        max_players = data['max_players']

        match = Match(game_name, max_players, location, g.user)

        db.session.add(match)
        db.session.commit()

        return "HTTP 200", 200

    else:
        return abort(405)


@app.route("/matches/<match_id>", methods=["GET"])
def get_match(match_id):
    matches = Match.query.all()
    if not matches:
        return abort(400)

    match = Match.query.get(match_id)
    if not match:
        return abort(400)

    if request.method == "GET":
        return json.dumps(get_match_data(match))

    else:
        return abort(405)


def get_match_data(match):
    match_data = {'location': match.name_location,
                  'game_name': match.game_name,
                  'created_date': match.created_date,
                  'cur_players': match.cur_players,
                  'max_players': match.max_players,
                  'match_id': match.id,
                  # 'started_by': match.started_by,
                  'game_on_date': match.game_on_date}

    #  Get all players currently in the lobby for the game
    player_list = []
    for user in User.query.filter(User.played.any(id=match.id)).all():
        player_data = {'id': user.id,
                       'name': user.name,
                       'email': user.email}

        if user.picture:
            player_data['picture'] = user.picture

        player_list.append(player_data)

    match_data['played_by'] = player_list

    #  Get all comments posted on the current game
    comment_list = []
    for comment in Message.query.filter(Message.posted_on.any(id=match.id)).all():
        comment_data = {'id': comment.id,
                        'author': comment.author_id,
                        'message': comment.message,
                        'name': User.query.get(comment.author_id).name,
                        'date': comment.date}

        comment_list.append(comment_data)

    match_data['comments'] = comment_list
    return match_data


@app.route("/matches/<match_id>", methods=["POST"])
@verify_login
def post_comment(match_id):
    match = Match.query.get(match_id)
    if not match:
        print("match id NOT located in database")
        return abort(400, "match id {0} NOT located in database".format(match_id))

    if request.method == "POST":
        comment = request.get_json()
        if len(comment) > 140:
            return abort(400, "Message too long")
        # [2:-1] removes extra symbols added by binascii
        # TODO: control for duplicate message ids
        message_id = str(binascii.b2a_hex(os.urandom(12)))[2:-1]
        db.session.add(Message(comment, g.user.id, message_id, match))
        db.session.commit()

        return json.dumps(get_match_data(match))

    else:
        return abort(405)


@app.route("/matches/<match_id>/join", methods=["GET", "POST"])
@verify_login
def join_match(match_id):
    if request.method in ["GET", "POST"]:
        match = Match.query.get(match_id)

        if app.config['TESTING']:
            print(match.played_by)

        if g.user not in match.played_by:
            if match.increment_cur_players():
                match.played_by.append(g.user)
                db.session.commit()
                return json.dumps(get_match_data(match))
            else:
                return abort(409)

        elif match.decrement_cur_players():
            match.played_by.remove(g.user)
            db.session.commit()
            return json.dumps(get_match_data(match))

        else:
            db.session.delete(match)
            db.session.commit()
            return redirect(APPLICATION_URL + "/matches/", 200)

    else:
        return abort(405)


@app.route("/dummy", methods=["POST"])
def post_dummy_data():
    db.drop_all()
    db.create_all()

    db.session.add(User('user@email.com', 'user', 'password'))
    db.session.add(User('eriny656@student.liu.se', 'eric', 'password'))
    db.session.add(User('danhe178@student.liu.se', 'daniel', 'password'))
    db.session.add(User('davka234@student.liu.se', 'david', 'password'))
    db.session.add(User('dagla547@student.liu.se', 'dag', 'password'))

    db.session.commit()

    daniel_uid = User.query.filter_by(email='danhe178@student.liu.se').first()
    eric_uid = User.query.filter_by(email='eriny656@student.liu.se').first()
    user_uid = User.query.filter_by(email='user@email.com').first()

    db.session.add(Match('Terraforming Uranus', 3, "irblosset", daniel_uid))
    db.session.add(Match('PanDAMNic', 5, "skäggetorp", eric_uid))
    db.session.add(Match('Talisman', 2, "C-huset", user_uid))

    db.session.commit()

    if app.config['TESTING']:
        print(Match.query.all())
        print(Match.query.get(1).game_name)

    return "HTTP 200", 200


@app.errorhandler(400)
def parameter_error(err):
    return 'HTTP 400: ' + str(err), 400


@app.errorhandler(401)
def unauthorized(err):
    return 'HTTP 401: ' + str(err), 401


@app.errorhandler(404)
def page_not_found(err):
    return 'HTTP 404: ' + str(err), 404


@app.errorhandler(405)
def method_error(err):
    return 'HTTP 405: ' + str(err), 405


@app.errorhandler(409)
def conflict(err):
    return 'HTTP 409: ' + str(err), 409


@app.errorhandler(500)
def nonspecific_error(err):
    return 'HTTP 500: ' + str(err), 500


if __name__ == "__main__":
    app.debug = debug_flag
    post_dummy_data()
    #db.drop_all()
    #db.create_all()
    app.run(os.environ.get('PORT'))
    #app.run(host='0.0.0.0')
