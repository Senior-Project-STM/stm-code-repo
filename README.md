Senior Project STM
===================
This is the repo for our Scanning Tunneling Microscope project. The project was for CS492-493 Senior Project, at the University of Illinois at Urbana-Champaign.
There are 4 seperate sections:
* An Android App
* A Bluetooth Server on the Raspberry Pi
* A Web Server on the Raspberry Pi
* Arduino code

##Android App - [STMAndroid](https://github.com/Senior-Project-STM/stm-code-repo/tree/master/STMAndroid)
===========================
One of the requirements for this project was an Android App. The Android app was designed to start, control, and stop scans on the STM. As we were not able to fully complete the hardware, the microscope does not fully work. The Android App is able to control the microscope demo that we have working. 

In order to use the app, you must have the Bluetooth Server running, with Bluetooth enabled on the computer you are running it on. The app allows you to connect to this microscope, and then start and stop scans. It also allows you to save the results of these scans.

You can run it by importing the project into Android Studio, and then uploading it to your device.

[Here](https://www.youtube.com/watch?v=tKDuY3glu70) is a link to a video demonstrating how the app works.

##External Libraries Used:
* [subsampling-scale-image-view](https://github.com/davemorrissey/subsampling-scale-image-view) by Dave Morrissey
* [recyclerview-multiselect](https://github.com/bignerdranch/recyclerview-multiselect) by BigNerdRanch


##Raspberry Pi Bluetooth Server - [STMPi](https://github.com/Senior-Project-STM/stm-code-repo/tree/master/STMPi)
=======================================
The Raspberry Pi was supposed to be used to control the Arduinos, and interface with the Android and Web apps. The two apps would communicate with the Raspberry Pis, and send commands to start/stop a scan. The Raspberry Pi would read these commands, and then control the ADCs/DACs through the Arduinos accordingly.

Currently, this section contains the a Python2.7 Bluetooth server that run on the Raspberry Pi. It is used in conjunction with the Android App to initiate and stop scans, and send the scan data over Bluetooth. It will also eventually control the Arduinos and do actual scans, but for now, it is a demonstration app to show how communication between an Android Phone and the microscope will work.

The server opens up a Bluetooth socket, which the Android app can then connect to. All communication occurs through this socket, including the sending/recieving of commands, and data transfer.

In order to simulate data, we tooks scan results from working STM. We take a random 10x10 section of this scan data, and then graph it using numpy/matplotlib. This data is then saved as a png, and then sent via Bluetooth to the Android App. The Android app reads this image, and then displays it to the user. The scan data is read in line by line, and a new image is created as each line is read. This simulates the actual microscope, as it also scans the sample line by line.

You can run it by running bluetoothserver.py. Make sure that you have Python 2.7 installed, and that Bluetooth is enabled when you run it.

##External Required Libraries:
* pybluez
* serial
* numpy
* matplotlib


##Raspberry Pi Web Server - [WebServer](https://github.com/Senior-Project-STM/stm-code-repo/tree/master/WebServer)
=====================================
This repo only consists the front end of the project. Backend code is hosted on Raspberry Pi.
Please install Rasbian operating system on Raspberry Pi. Then make sure Python and Django are installed as well.
For the demo purposes, you can use python's simple server to host the static files.

##Arduino Section - [STMArduino](https://github.com/Senior-Project-STM/stm-code-repo/tree/master/STMArduino)
==============================
We were planning on using the Arduino to control the various DACS, and ADCS required to operate the microscope. We decided to the the Arduino as it was fairly cheap, yet still had the hardware controls that we needed. As we were not able to finish the hardware, we could not finish the code for this section.

Right now, it is merely a demo to show communication between the Raspberry Pi and Arduino. 

##Hardware
==========
In addition to this, our project has a hardware section, which is incomplete. We used various sources to start the build process, and have done what we can

Sources:
* [A previous attempt to build an STM](https://dberard.com/home-built-stm/)
* [A wiring diagram we used](https://drive.google.com/open?id=0B5njquGxAWb6ZFVhVWRqVGlsZGNsdy1xVFJMUnVhaE5TQ1NZ)


##Main Contributors
* Atul Nambudiri
* Jiayi Cao
* Jennifer Cheng
* Anshuman Girdhar

##Other Contributers
* Parth Kothari
* Elliot Young

##Mentors
* Professor Joseph Lyding
* Professor Lawrence Angrave

##License
```
The MIT License (MIT)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
