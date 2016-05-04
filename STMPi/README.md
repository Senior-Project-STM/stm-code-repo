STMPi
==========
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