import requests
import json

url = "http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com"

r = requests.post(url + '/user', headers={'Content-Type': 'application/json'},data=json.dumps({'email':'user@email.com','name':'user','password':'password'}))

