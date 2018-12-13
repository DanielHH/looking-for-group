import unittest
import app
import ast
import json
import time
import os
import io


class DataTest(unittest.TestCase):
    def setUp(self):
        app.app.config['TESTING'] = True
        self.server = app.app.test_client()
        app.db.drop_all()
        app.db.create_all()

    def post_messages(self, token):
        self.server.post('/messages', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('A message'))
        self.server.post('/messages', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('A second message'))
        self.server.post('/messages', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('Another message'))

    def post_comments(self, token):
        self.server.post('/matches/1', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('A comment'))
        self.server.post('/matches/1', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('A second comment'))
        self.server.post('/matches/1', headers={'Content-Type': 'application/json', "Authorization": token},
                         data=json.dumps('Another comment'))

    def test_dummy(self):
        self.server.post('/dummy')

    def post_matches(self, token):
        self.server.post('/matches', headers={'Content-Type': 'application/json', 'Authorization': token},
                         data=json.dumps({'location': 'here', 'max_players': 3, 'title': 'Robot Wars'}))
        self.server.post('/matches', headers={'Content-Type': 'application/json', 'Authorization': token},
                         data=json.dumps({'location': 'there', 'max_players': 5, 'title': 'One Night Werewolf'}))
        self.server.post('/matches', headers={'Content-Type': 'application/json', 'Authorization': token},
                         data=json.dumps({'location': 'everywhere', 'max_players': None, 'title': 'Everything'}))

    def join_match(self, token):
        rv = self.server.post('/matches/1/join', headers={'Content-Type': 'application/json', 'Authorization': token},
                              data=json.dumps({}))
        return rv

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

    def post_image(self, user_id, token):
        file = io.BytesIO(b'content\n')
        self.server.post('/images/' + str(user_id), headers={'Content-Type': 'multipart/form-data',
                                                             'boundary': '--ArbitraryBoundary--',
                                                             'Authorization': token},
                         data=dict(image=(file, 'output_file.jpg'))
                         )

    def create_test_file(self, filename):
        f = open("./photos/" + filename, "w")
        f.write("content\n")
        f.close()

    def delete_test_file(self, filename):
        filepath = "./photos/" + filename
        if os.path.exists(filepath):
            os.remove(filepath)
            return True
        else:
            return False

    def login_user(self, email):
        rv = self.convert_to_literal(self.server.post('/user/login', headers={'Content-Type': 'application/json'},
                                                      data=json.dumps({'email': '{0}'.format(email),
                                                                       'password': 'password'})))
        return rv['token']

    def test_create_users(self):
        self.create_users()
        users = app.User.query.all()
        user_list = []
        for user in users:
            user_list.append(user.name)
        self.assertEqual(user_list, ['user', 'eriny656', 'somon123'], "Failed in create_users")

    def test_login_user(self):
        self.create_users()
        token = self.login_user('user@email.com')
        user = app.User.query.filter_by(name='user').first()
        test_token = app.Token.query.filter_by(user_id=user.id).first()
        self.assertEqual(token, str(test_token), "Failed in login_user")

    def test_logout_user(self):
        self.create_users()
        token1 = self.login_user('user@email.com')
        time.sleep(1)
        token2 = self.login_user('user@email.com')
        time.sleep(1)
        token3 = self.login_user('user@email.com')
        self.server.post('/user/logout', headers={"Authorization": token3})

        tokens = app.Token.query.all()
        token_list = []
        for tok in tokens:
            token_list.append(str(tok))

        self.assertEqual(token_list, [token1, token2])

    def test_get_picture(self):
        self.create_users()
        user = app.User.query.filter_by(name='user').first()
        filename = "user_file.utest"
        self.create_test_file(filename)

        user.picture = filename

        file_content = self.server.get('/user/' + str(user.id) + "/image").data

        self.assertEqual(file_content, b'content\n')
        self.assertEqual(self.delete_test_file(filename), True)

    def test_post_picture(self):
        self.create_users()
        user = app.User.query.filter_by(name='user').first()
        token = self.login_user('user@email.com')
        self.post_image(user.id, token)
        local_file = open("./photos/output_file.jpg", "r")
        self.assertEqual(local_file.read(), "content\n")
        local_file.close()
        self.delete_test_file("output_file.jpg")

    def test_post_messages(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.post_messages(token)
        messages = app.Message.query.all()
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
        message_three_id = rv[2]["id"]
        self.server.post('/user/logout', headers={"Authorization": user_token})
        eriny656_token = self.login_user('eriny656@student.liu.se')
        self.server.post('/messages/{0}/flag'.format(message_one_id),
                         headers={'Authorization': eriny656_token})
        self.server.post('/user/logout', headers={"Authorization": eriny656_token})
        self.server.post('/messages/{0}/flag'.format(message_two_id),
                         headers={'Authorization': somon123_token})
        self.server.post('/user/logout', headers={"Authorization": somon123_token})
        message_one = self.convert_to_literal(self.server.get('/messages/{0}'.format(message_one_id)))
        message_two = self.convert_to_literal(self.server.get('/messages/{0}'.format(message_two_id)))
        message_three = self.convert_to_literal(self.server.get('/messages/{0}'.format(message_three_id)))
        self.assertEqual(['eriny656@student.liu.se'], message_one['read_by'], "failed in flag_as_read")
        self.assertEqual(['somon123@student.liu.se'], message_two['read_by'], "failed in flag_as_read")
        self.assertEqual([], message_three['read_by'], "failed in flag_as_read")

    def test_get_unread(self):
        # TODO: fix
        self.create_users()
        user_token = self.login_user('user@email.com')
        self.post_messages(user_token)
        rv = self.convert_to_literal(self.server.get('/messages'))
        message_one_id = rv[0]["id"]
        message_two_id = rv[1]["id"]
        eriny656_token = self.login_user('eriny656@student.liu.se')
        self.server.post('/messages/{0}/flag'.format(message_one_id), headers={"Authorization": eriny656_token})
        self.server.post('/messages/{0}/flag'.format(message_two_id), headers={"Authorization": eriny656_token})
        rv = self.convert_to_literal(self.server.get('/messages/unread', headers={"Authorization": eriny656_token}))
        self.assertEqual('Another message', rv[0]['message'], "failed in get_unread")

    def test_delete_message(self):
        # TODO: fix
        self.create_users()
        token = self.login_user('user@email.com')
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

    def test_post_matches(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.post_matches(token)

        rv = json.loads(self.server.get('/matches').data.decode(encoding='utf-8'))

        self.assertEqual(rv[0]['location'], 'here', 'Failed in post matches')
        self.assertEqual(rv[0]['match_id'], 1, 'Failed in post matches')
        self.assertEqual(rv[0]['max_players'], 3, 'Failed in post matches')
        self.assertEqual(rv[0]['title'], 'Robot Wars', 'Failed in post matches')

    def test_post_comment(self):
        self.create_users()
        user_token = self.login_user("user@email.com")

        self.post_matches(user_token)
        self.post_comments(user_token)

        messages = app.Message.query.all()
        message_list = []
        for message in messages:
            message_list.append(message.message)
        self.assertEqual(message_list, ['A comment', 'A second comment', 'Another comment'], "Failed in post_messages")

    def test_get_match(self):
        self.create_users()
        token = self.login_user('user@email.com')
        self.post_matches(token)

        rv = json.loads(self.server.get('/matches/1').data.decode(encoding='utf-8'))

        self.assertEqual(rv['location'], 'here', 'Failed in post matches')
        self.assertEqual(rv['match_id'], 1, 'Failed in post matches')
        self.assertEqual(rv['max_players'], 3, 'Failed in post matches')

    def test_join_match(self):
        self.create_users()
        post_token = self.login_user('user@email.com')
        self.post_matches(post_token)

        join_token = self.login_user('eriny656@student.liu.se')

        rv = json.loads(self.join_match(join_token).data)
        print(rv["played_by"])
        self.assertEqual(rv["played_by"][0]["email"], "user@email.com", "Failed in join match")

        rv = json.loads(self.join_match(post_token).data)
        print(rv["played_by"])
        self.assertEqual(rv["played_by"][0]["email"], "eriny656@student.liu.se", "Failed in leave match")

    def test_view_profile(self):
        self.create_users()
        rv = json.loads(self.server.get('http://127.0.0.1:5000/user/1').data)

        self.assertEqual(rv['email'], 'user@email.com')

    def test_long_message_post(self):
        self.create_users()
        token = self.login_user('user@email.com')
        rv = self.server.post('/messages', headers={'Content-Type': 'application/json',
                                                    'Authorization': token}, data=json.dumps('A message longer ' +
                'than 140 characters..............................................................................' +
                '................................'))
        self.assertEqual(rv.status_code, 400, 'Failed in message > 140 characters')

    def test_messages_delete(self):
        self.create_users()
        token = self.login_user('user@email.com')
        resp = self.server.delete('/messages', headers={'Authorization': token})
        self.assertEqual(resp.status_code, 405, 'Failed in improper method to /messages')

    def test_bad_signature(self):
        self.create_users()
        rv = self.server.post("/messages", headers={'Content-Type': 'application/json', 'Authorization': 'incorrect'},
                              data=json.dumps('invisible message'))
        self.assertEqual(rv.status_code, 401, 'Failed in bad_signature')
