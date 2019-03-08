from setuptools import setup

setup(name='looking-for-group',
      version='1.0',
      description='Heroku App',
      author='Eric Nylander',
      author_email='eriny656@student.liu.se',
      url='https://looking-for-group-boardgames.herokuapp.com/',
      install_requires=['Flask', 'Flask-SQLAlchemy>=2.3.1', 'sqlalchemy>=1.1.4', 'werkzeug', 'itsdangerous'],
     )
