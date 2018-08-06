import unittest
import server
import ast
import json


class DataTest(unittest.TestCase):
    def setUp(self):
        server.app.config['TESTING'] = True
        self.server = server.app.test_client()
        server.db.drop_all()
        server.db.create_all()

    def post_messages(self, token):
        self.server.post('/messages', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('A message'))
        self.server.post('/messages', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('A second message'))
        self.server.post('/messages', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('Another message'))

    def convert_to_literal(self, rv):
        rv = rv.data.decode(encoding='utf-8')
        rv = ast.literal_eval(rv)
        return rv

    def create_users(self):
        self.server.post('/user', headers={'Content-Type': 'application/json'},
                         data=json.dumps({'email': 'user@email.com', 'name': 'user', 'password': 'password'}))
        self.server.post('/user', headers={'Content-Type': 'application/json'},
                         data=json.dumps({'email': 'eriny656@student.liu.se', 'name': 'eriny656', 'password': 'password'}))
        self.server.post('/user', headers={'Content-Type': 'application/json'},
                         data=json.dumps({'email': 'somon123@student.liu.se', 'name': 'somon123', 'password': 'password'}))

    def login_user(self, email):
        self.server.post('/user/login', headers={'Content-Type': 'application/json'},
                         data=json.dumps({'email': '{0}'.format(email), 'password': 'password'}))
        user = server.User.query.filter_by(email='{0}'.format(email)).first()
        token = server.Token.query.filter_by(user_id=user.id).first()
        return token.token

    def test_create_users(self):
        self.create_users()
        users = server.User.query.all()
        user_list = []
        for user in users:
            user_list.append(user.name)
        self.assertEqual(user_list, ['user', 'eriny656', 'somon123'], "Failed in create_users")

    def test_login_user(self):
        self.create_users()
        token = self.login_user('user@email.com')
        user = server.User.query.filter_by(name='user').first()
        test_token = server.Token.query.filter_by(user_id=user.id).first()
        self.assertEqual(token, str(test_token), "Failed in login_user")

    def test_logout_user(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.server.post('/user/logout', headers={"Authorization": token})
        tokens = server.Token.query.all()
        self.assertEqual(tokens, [])

    def test_post_messages(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.post_messages(token)
        messages = server.Message.query.all()
        message_list = []
        for message in messages:
            message_list.append(message.message)
        self.assertEqual(message_list, ['A message', 'A second message', 'Another message'], "Failed in post_messages")

    def test_get_messages(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.post_messages(token)
        rv = self.convert_to_literal(self.server.get('/messages'))
        self.assertEqual('A second message', rv[1]["message"], "failed in get_messages")

    def test_get_message_id(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.post_messages(token)
        rv = self.convert_to_literal(self.server.get('/messages'))
        message_two_id = rv[1]["id"]
        rv = self.convert_to_literal(self.server.get('/messages/{0}'.format(message_two_id)))
        self.assertEqual('A second message', rv["message"], "failed in get_message_id")

    def test_flag_as_read(self):
        self.create_users()
        user_token = self.login_user('user@email.com')
        somon123_token = self.login_user('somon123@student.liu.se')
        self.post_messages(user_token)
        rv = self.convert_to_literal(self.server.get('/messages'))
        message_one_id = rv[0]["id"]
        message_two_id = rv[1]["id"]
        self.server.post('/user/logout', headers={"Authorization": user_token})
        eriny656_token = self.login_user('eriny656@student.liu.se')
        self.server.post('/messages/{0}/flag/eriny656'.format(message_one_id),
                         headers={'Authorization': eriny656_token})
        self.server.post('/user/logout', headers={"Authorization": eriny656_token})
        self.server.post('/messages/{0}/flag/somon123'.format(message_two_id),
                         headers={'Authorization': somon123_token})
        self.server.post('/user/logout', headers={"Authorization": somon123_token})
        rv = self.convert_to_literal(self.server.get('/messages'))
        self.assertEqual(['eriny656'], rv[0]['read_by'], "failed in flag_as_read")
        self.assertEqual(['somon123'], rv[1]['read_by'], "failed in flag_as_read")
        self.assertEqual([], rv[2]['read_by'], "failed in flag_as_read")

    def test_get_unread(self):
        self.create_users()
        token = self.login_user('user')
        self.post_messages(token)
        rv = self.convert_to_literal(self.server.get('/messages'))
        message_one_id = rv[0]["id"]
        message_two_id = rv[1]["id"]
        self.server.post('/messages/{0}/flag/user'.format(message_one_id), headers={"Authorization": token})
        self.server.post('/messages/{0}/flag/user'.format(message_two_id), headers={"Authorization": token})
        rv = self.convert_to_literal(self.server.get('/messages/unread/user', headers={"Authorization": token}))
        self.assertEqual('Another message', rv[0]['message'], "failed in get_unread")

    def test_delete_message(self):
        self.create_users()
        token = self.login_user('user')
        self.post_messages(token)
        rv = self.convert_to_literal(self.server.get('/messages'))
        message_one_id = rv[0]["id"]
        message_two_id = rv[1]["id"]
        message_three_id = rv[2]["id"]
        self.server.delete('http://127.0.0.1:5000/messages/{0}'.format(message_one_id),
                           headers={"Authorization": token})
        self.server.delete('http://127.0.0.1:5000/messages/{0}'.format(message_two_id),
                           headers={"Authorization": token})
        rv = self.convert_to_literal(self.server.get('/messages'))
        message_three = self.convert_to_literal(self.server.get('/messages/{0}'.format(message_three_id)))
        self.assertEqual(rv[0], message_three, "Failed in delete_message")

    def test_long_message_post(self):
        self.create_users()
        token = self.login_user('user')
        rv = self.server.post('/messages', headers={'Content-Type': 'application/json',
                                                    'Authorization': token}, data=json.dumps('A message longer ' +
                'than 140 characters..............................................................................' +
                '................................'))
        self.assertEqual(rv.status_code, 400, 'Failed in message > 140 characters')

    def test_messages_delete(self):
        self.create_users()
        token = self.login_user('user')
        resp = self.server.delete('/messages', headers={'Authorization': token})
        self.assertEqual(resp.status_code, 405, 'Failed in improper method to /messages')

    def test_bad_signature(self):
        self.create_users()
        rv = self.server.post("/messages", headers={'Content-Type': 'application/json', 'Authorization': 'incorrect'},
                              data=json.dumps('invisible message'))
        self.assertEqual(rv.status_code, 401, 'Failed in bad_signature')
