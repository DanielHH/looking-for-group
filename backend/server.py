import os
import binascii
from flask import Flask, request, json, jsonify, abort, g
from flask_sqlalchemy import SQLAlchemy
from functools import wraps
from werkzeug.security import generate_password_hash, check_password_hash
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

read_by_table = db.Table('read_by', db.metadata,
                         db.Column('message_id', db.String,
                                   db.ForeignKey('message.id')),
                         db.Column('user_id', db.Integer,
                                   db.ForeignKey('user.id')))


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
    name = db.Column(db.String)
    password = db.Column(db.String(255))

    tokens = db.relationship('Token')
    read = db.relationship('Message', secondary=read_by_table)

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

    def __repr__(self):
        return self.email


class Message(db.Model):
    __tablename__ = 'message'
    id = db.Column(db.String(24), primary_key=True)
    message = db.Column(db.String(140), unique=False)
    author_id = db.Column(db.Integer, db.ForeignKey('user.id'))

    add_author_id = db.relationship('User', foreign_keys=[author_id])
    msg_read_by = db.relationship('User', secondary=read_by_table)

    def __init__(self, message, author_id, message_id):
        self.message = message
        self.id = message_id
        self.author_id = author_id

    def __repr__(self):
        return self.message


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
    app.run()
