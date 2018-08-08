import os
import binascii
import datetime
from flask import Flask, request, json, jsonify, abort, g, flash
from flask_sqlalchemy import SQLAlchemy
from functools import wraps
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
from itsdangerous import (TimedJSONWebSignatureSerializer
                          as Serializer, BadSignature, SignatureExpired)

app = Flask(__name__)
if "OPENSHIFT_POSTGRESQL_DB_URL" in os.environ:
    app.config["SQLALCHEMY_DATABASE_URI"] = os.environ['OPENSHIFT_POSTGRESQL_DB_URL']
else:
    app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:////tmp/test.db"

db = SQLAlchemy(app)

app.config['SECRET_KEY'] = 'i folded my soldier well in his blanket'

SECONDS_IN_ONE_WEEK = 604800
ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg', 'gif']

read_by_table = db.Table('read_by', db.metadata,
                         db.Column('message_id', db.String,
                                   db.ForeignKey('message.id')),
                         db.Column('user_id', db.Integer,
                                   db.ForeignKey('user.id')))

matches_table = db.Table('matched_players', db.metadata,
                         db.Column('match_id', db.String,
                                   db.ForeignKey('match.id')),
                         db.Column('user_id', db.Integer,
                                   db.ForeignKey('user.id')))


class Token(db.Model):
    __tablename__ = 'token'
    id = db.Column(db.Integer, primary_key=True)
    token = db.Column(db.String(200))
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

    def __init__(self, token):
        self.token = token

    def __repr__(self):
        return self.token


class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(127), unique=True)
    name = db.Column(db.String, nullable=False)
    password = db.Column(db.String(255), nullable=False)

    picture = db.Column(db.Largebinary, nullable=True)

    tokens = db.relationship('Token')
    read = db.relationship('Message', secondary=read_by_table)
    played = db.relationship('Match', secondary=matches_table)

    def __init__(self, email, name, password):
        self.email = email
        self.name = name
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)

    def generate_auth_token(self, expiration=SECONDS_IN_ONE_WEEK):
        s = Serializer(app.config['SECRET_KEY'], expires_in=expiration)
        token = Token(s.dumps(self.id).decode('utf-8'))
        token.user_id = self.id
        self.tokens.append(token)
        db.session.commit()
        return str(token)

    def set_picture(self, picture):
        self.picture = picture

    def __repr__(self):
        return self.email


class Message(db.Model):
    __tablename__ = 'message'
    id = db.Column(db.String(24), primary_key=True)
    message = db.Column(db.String(140), unique=False, nullable=False)
    date = db.Column(db.DateTime, nullable=False)
    author_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    add_author_id = db.relationship('User', foreign_keys=[author_id])
    msg_read_by = db.relationship('User', secondary=read_by_table)

    def __init__(self, message, author_id, message_id):
        self.message = message
        self.id = message_id
        self.author_id = author_id

    def __repr__(self):
        return self.message


class Match(db.Model):
    __tablename__ = 'match'
    id = db.Column(db.String(24), primary_key=True)
    max_players = db.Column(db.Integer)
    cur_players = db.Column(db.Integer)
    created_date = db.Column(db.DateTime, nullable=False)
    started_date = db.Column(db.DateTime, nullable=True)
    started_by = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)

    coord_location = db.Column(db.ARRAY(db.Float, dimensions=2), nullable=True)
    name_location = db.Column(db.Text, nullable = True)

    comments = db.relationship('Message', backref='match')
    played_by = db.relationship('User', secondary=matches_table)

    # TODO: add actual games to the db and connect to matches

    def __init__(self, max_players, creator, location):
        self.max_players = max_players
        self.started_by = creator
        self.cur_players = 1
        self.created_date = datetime.datetime

        if type(location) is str:
            self.name_location = location
        elif type(location) is list:
            self.coord_location = location

    def __repr__(self):
        # TODO: change this to game and date
        return self.id


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
    user = User.query.filter_by(id=data).first()
    return user


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route("/")
def index():
    return "Message board"


@app.route("/user", methods=["POST"])
def create_user():
    if request.method == "POST":
        email = request.get_json()['email']
        name = request.get_json()['name']
        password = request.get_json()['password']
        # TODO: control for user already exists
        user = User(email, name, password)
        db.session.add(user)
        db.session.commit()
        return 'HTTP 200', 200

    else:
        return abort(405)


@app.route("/user/login", methods=["POST"])
def login_user():
    if request.method == "POST":
        email = request.get_json()['email']
        password = request.get_json()['password']
        user = User.query.filter_by(email=email).first()
        if not user:
            return "email or password incorrect"
        elif user.check_password(password):
            token = user.generate_auth_token()
            return json.dumps({'token': token})
        else:
            return "email or password incorrect"

    else:
        return abort(405)


@app.route("/user/logout", methods=["POST"])
@verify_login
def logout_user():
    if g.user is None:
        return abort(401)

    elif request.method == "POST":
        headers = request.headers
        token_value = headers["Authorization"]
        token = Token.query.filter_by(token=token_value).first()
        if token_value == token.token:
            # user_logout_message = "{0} is now logged out on this device".format(str(g.user))
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
        message = Message.query.filter_by(id=message_id).first()
        # message is type Message
        if message:
            read_by = []
            for usr in message.msg_read_by:
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
    message = Message.query.filter_by(id=message_id).first()
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
            for usr in message.msg_read_by:
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
    if g.user is None:
        return abort(401)

    elif request.method == "POST":
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


@app.route("/messages/<message_id>/flag/<user_email>", methods=["POST"])
@verify_login
def flag_as_read(message_id, user_email):
    # TODO: change validation to token
    if g.user is None or g.user.email != user_email:
        return abort(401)

    messages = Message.query.all()
    if not messages:
        return abort(400)

    elif request.method == "POST":
        message = Message.query.filter_by(id=message_id).first()
        if message:
            user = User.query.filter_by(email=user_email).first()
            if user not in message.msg_read_by:
                message.msg_read_by.append(user)
            else:
                message.msg_read_by.remove(user)
            db.session.commit()
            return "HTTP 200", 200
        else:
            return abort(400)

    else:
        return abort(405)


@app.route("/messages/unread/<user_email>", methods=["GET"])
@verify_login
def read_unread_messages(user_email):
    if g.user is None or g.user.email != user_email:
        return abort(401)

    elif request.method == "GET":
        unread_messages = []
        messages = Message.query.all()
        for message in messages:
            read_by = []
            for usr in message.msg_read_by:
                read_by.append(str(usr))
            if user_email not in read_by:
                unread_messages.append({'read_by': read_by,
                                        'message': str(message),
                                        'id': message.id})
        return json.dumps(unread_messages)

    else:
        return abort(405)


@app.route("/images/<user_email>", methods=["POST"])
@verify_login
def upload_image(user_email):
    if g.user is None or g.user.email != user_email:
        return abort(401)

    elif request.method == "POST":
        if 'file' not in request.files:
            flash('No file part')
            return abort(400)
        file = request.files['file']

        if file.filename == '':
            flash("No selected file")
            return abort(400)

        if file and allowed_file(file.filename):
            user = User.query.filter_by(email=user_email).first()
            user.set_picture(secure_filename(file.filename))
            db.session.commit()
            return "HTTP 200", 200

    else:
        return abort(405)


@app.route("/matches", methods=["GET"])
@verify_login
def get_matches():
    if g.user is None:
        return abort(401)

    matches = Match.query.all()
    if not matches:
        return abort(400)

    if request.method == "GET":
        match_list = []

        for match in matches:
            location = match.name_location    # can still be null
            cur_players = match.cur_players   # can still be null
            max_players = match.max_players   # can still be null
            created_date = match.created_date
            match_id = match.id

            match_list.append({'location': location, 'created_date': created_date,
                               'cur_players': cur_players, 'max_players': max_players,
                               'match_id': match_id})

        return json.dumps(match_list)

    else:
        return abort(405)


@app.route("/matches/<match_id>", methods=["GET"])
@verify_login
def get_match(match_id):
    if g.user is None:
        return abort(401)

    matches = Match.query.all()
    if not matches:
        return abort(400)

    match = Match.query.filter_by(id=match_id).first()
    if not match:
        return abort(400)

    if request.method == "GET":
        match_data = {}

        if match.name_location:
            match_data['location'] = match.name_location
        elif match.coord_location:
            match_data['location'] = match.coord_location

        match_data['created_date'] = match.created_date
        match_data['cur_players'] = match.cur_players
        match_data['max_players'] = match.max_players
        match_data['match_id'] = match.id
        match_data['started_by'] = match.started_by
        match_data['started_date'] = match.started_date

        player_list = []
        for player_id in matches_table.query.filter_by(match_id=match.id).first:
            user_info = User.query.filter_by(id=player_id).first()
            player_data = {'id': user_info.id,
                           'name': user_info.name,
                           'email': user_info.email}

            if user_info.picture:
                player_data['picture'] = user_info.picture

            player_list.append(player_data)

        match_data['played_by'] = player_list

        comment_list = []
        for comment_id in match.comments:
            comment = Message.query.filter_by(id=comment_id).first()

            comment_info = {'id': comment_id,
                            'author': comment.author_id,
                            'message': comment.message,
                            'date': comment.date}

            comment_list.append(comment_info)

        match_data['comments'] = comment_list

        return json.dumps(match_data)

    else:
        return abort(405)


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


@app.errorhandler(500)
def nonspecific_error(err):
    return 'HTTP 500: ' + str(err), 500


if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0', port=8080)
