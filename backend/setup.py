from setuptools import setup

setup(name='looking-for-group',
      version='1.0',
      description='OpenShift App',
      author='Eric Nylander',
      author_email='eriny656@student.liu.se',
      url='http://labapp-eriny656.openshift.ida.liu.se/',
      install_requires=['Flask', 'Flask-SQLAlchemy>=2.1', 'sqlalchemy>=1.1.4'],
     )
