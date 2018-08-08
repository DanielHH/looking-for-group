from setuptools import setup

setup(name='looking-for-group',
      version='1.0',
      description='OpenShift App',
      author='Eric Nylander',
      author_email='eriny656@student.liu.se',
      url='http://looking-for-group-looking-for-group.193b.starter-ca-central-1.openshiftapps.com',
      install_requires=['Flask', 'Flask-SQLAlchemy>=2.3.1', 'sqlalchemy>=1.1.4', 'werkzeug', 'itsdangerous'],
     )
