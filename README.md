# How to build the system (locally)

## PREREQUISITES
* Android studio 3.4
* Python 3.7
* Pip 19.0.3

---

* Clone this project.

## Android

* Open the project in android studio
* Sync gradle files
* Add your local ip-adress to
* Go into res/values/strings.xml and replace [YOUR_URL] i \<string name="url">[YOUR_URL]\</string> with your local url.
* Connect an android device to your computer
* Press run in android studio and select your connected device.

## Server

* Start a terminal
* Create a virtual environment for this project.
* Enter the virtual environment.
* cd to backend/
* Run 'pip install -r requirements.txt'
* Run 'py app.py'