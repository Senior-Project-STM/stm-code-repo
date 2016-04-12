import bluetooth #pybluez
import time
import random
import threading
import serial
import select
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import csv
import time
import os
import random
from threading import Thread, Lock

serv_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)		#Create a bluetooth socket
uuid = "37407000-8cf0-11bd-b23e-10b75c30d20a"

#ser = serial.Serial('/dev/ttyACM0', 9600) #The serial port the arduino is connected on. Change this if needed

print("Creating Bluetooth Server");

port = bluetooth.PORT_ANY			#Get any open bluetooth port
serv_socket.bind(("", port))		#Bind to the bluetooth port
serv_socket.listen(1)				#Listen on the port
bluetooth.advertise_service(serv_socket, "STM", uuid)		#Advertise the stm service with the given uuid

socket, address = serv_socket.accept()			#Accept the incoming connection
print("Bluetooth Connection has been initiated with" + str(address[0]))

t = 0
sending = False

# # The old send_pictures
# def send_pictures():		#This method will send all 11 images in a row, if scanning has started.
# 	global t
# 	global sending
# 	if sending == True:
# 		images = ["scan_images/0.jpg", "scan_images/1.jpg", "scan_images/2.jpg", "scan_images/3.jpg", "scan_images/4.jpg", "scan_images/5.jpg", "scan_images/6.jpg", "scan_images/7.jpg", "scan_images/8.jpg", "scan_images/9.jpg", "scan_images/10.jpg"]
# 		item = images[t]
# 		with open(item, "rb") as image:
# 			stuff = image.read()
# 			print("Communicating with Arduino")
# 			#ser.write("Start")			#Send a request to the Arduino
# 			#response = ser.readline()   #Wait for a response from the Arduino
# 			print("Sending Image %d" % t)
# 			for j in range(3):
# 				socket.send(stuff)
# 				socket.send("Done")
# 				time.sleep(1)
# 			t += 1
# 		if t == 11:
# 			for j in range(3):
# 				socket.send("Scan Finished")
# 			t = 0
# 			sending = False
# 		else:
# 			threading.Timer(1.0, send_pictures).start()

# while(True):		#This waits and reads for incoming commands.
# 	command = socket.recv(100)
# 	print("Received a command: " + command)
# 	if command == "Start Scan":
# 		if sending != True:
# 			sending = True
# 			send_pictures();
# 	elif command == "Reset Scan":
# 		sending = False
# 		i = 0


def scan():
	"""
	This function will look in the test_csv file, and find a random size x size chunk to display.
	It will then send this image out line by line, which will simulate the microscope
	"""
	global t
	global sending
	v_array = np.zeros((1,1))			# Intermediary array with the results of the scan of one line 
	size = 40 		#The size of the original scan to take

	#These lines initalize the figure
	fig = plt.figure()
	ax1 = fig.add_subplot(1,1,1)
	ax1.set_xlim([0, size])
	ax1.set_ylim([size, 0])
	ax = plt.Axes(fig, [0., 0., 1., 1.])
	ax.set_axis_off()
	fig.add_axes(ax)
	cmap = 'afmhot'
	vmin = -2e-12
	vmax = 5e-11
	#so that colorbar works later
	im = ax1.pcolor(v_array, cmap=cmap, vmin=vmin, vmax=vmax)


	v_array = np.zeros((1,1))		#Zero out the output array 
	start = random.randint(0, 400 - size)
	print 'start getting'
	values = list()
	with open('test_data.csv', 'rb') as f:
		reader = csv.reader(f)
		i = 0
		for row in reader:
			if sending:
				if 'Channel' in row[0]:
					channel = row[0].split(':')[1].strip()
				elif 'Width' in row[0]:
					width = row[0].split(':')[1].strip()
				elif 'Height' in row[0]:
					height = row[0].split(':')[1].strip()
				elif 'Value units' in row[0]:
					value_units = row[0].split(':')[1].strip()
				else:
					if i >= start:		#Only send out the size x size image in the range you want
						if i < start + size:
							row = map(float,row)
							values.append(row[start:start+size])
							v_array = np.asarray(values)		#Save the rwo of the csv file as a numpy array

							ax1.clear()
							ax1.set_xlim([0, size])
							ax1.set_ylim([size, 0])
							im = ax1.pcolor(v_array, cmap=cmap, vmin=vmin, vmax=vmax)
							fig.savefig("out.jpg", bbox_inches=0)			#Save the updated image as a jpg
							with open("out.jpg", "rb") as image:
								stuff = image.read()
								print("Communicating with Arduino")
								#ser.write("Start")			#Send a request to the Arduino
								#response = ser.readline()   #Wait for a response from the Arduino
								print("Sending Image %d" % t)
								for j in range(2):
									socket.send(stuff)
									socket.send("Done")
									time.sleep(1)
							time.sleep(2)
						else: 
							for j in range(3):
								socket.send("Scan Finished")
							t = 0	
							sending = False
							break
					i += 1
			else:
				break	


while(True):		#This waits and reads for incoming commands.
	command = socket.recv(100)
	print("Received a command: " + command)
	if command == "Start Scan":
		if sending != True:
			sending = True
			t1 = Thread(target = scan)
			t1.start()
	elif command == "Reset Scan":
		sending = False


