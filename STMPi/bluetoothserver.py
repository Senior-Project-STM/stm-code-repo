import bluetooth #pybluez
import time
import random
import threading
import serial
import select
import numpy as np
import matplotlib
matplotlib.use('SVG')
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import csv
import time
import os
import random
from threading import Thread, Lock



def init_connection():
	"""
	This method sets up a bluetooth server connection on any available bluetooth port. 
	The given uuid uniquely represents the bluetooth connection for the STM, so they must be the same on
	both the server and the Android phone
	""" 
	serv_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)		#Create a bluetooth socket
	uuid = "37407000-8cf0-11bd-b23e-10b75c30d20a"

	print("Creating Bluetooth Server");

	port = bluetooth.PORT_ANY			#Get any open bluetooth port
	serv_socket.bind(("", port))		#Bind to the bluetooth port
	serv_socket.listen(1)				#Listen on the port
	bluetooth.advertise_service(serv_socket, "STM", uuid)		#Advertise the stm service with the given uuid

	socket, address = serv_socket.accept()			#Accept the incoming connection
	print("Bluetooth Connection has been initiated with" + str(address[0]))
	return socket

t = 0
sending = False
#ser = serial.Serial('/dev/ttyACM0', 9600) #The serial port the arduino is connected on. Change this if needed


def scan():
	"""
	This function will look in the test_csv file, and find a random size x size chunk to display.
	It will then send this image out line by line, which will simulate the microscope
	"""
	global t
	global sending
	global socket
	v_array = np.zeros((1,1))			# Intermediary array with the results of the scan of one line 
	size = 6		#The size of the original scan to take

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
	start = random.randint(0, 400 - size - 5)		#Find a random position to start at
	print(start)
	print 'start getting'
	values = list()
	with open('test_data.csv', 'rb') as f:		#Open up the input csv file
		reader = csv.reader(f)
		i = 0	
		for row in reader:					#Loop through row by row
			if sending:						#Make sure we are still sending out images
				if 'Channel' in row[0]:						#The first few lines have some additional configuration info
					channel = row[0].split(':')[1].strip()
				elif 'Width' in row[0]:
					width = row[0].split(':')[1].strip()
				elif 'Height' in row[0]:
					height = row[0].split(':')[1].strip()
				elif 'Value units' in row[0]:
					value_units = row[0].split(':')[1].strip()
				else:
					if i >= start:		#Only send out the size x size image in the range you want
						if i < start + size + 1:		
							row = map(float,row)	
							values.append(row[start:start+size])	#Take size values from the row, so that you have a size by size square of values
							v_array = np.asarray(values)		#Save the rwo of the csv file as a numpy array

							ax1.clear()						#Clear out the image, and update it with new values
							ax1.set_xlim([0, size])
							ax1.set_ylim([size, 0])
							im = ax1.pcolor(v_array, cmap=cmap, vmin=vmin, vmax=vmax)
							fig.savefig("out.jpg", bbox_inches=0)			#Save the updated image as a jpg
							with open("out.jpg", "rb") as image:		#Open the image back up
								stuff = image.read()
								print("Communicating with Arduino")
								#ser.write("Start")			#Send a request to the Arduino
								#response = ser.readline()   #Wait for a response from the Arduino
								print("Sending Image %d" % t)	#Send the image
								for j in range(2):				#Send it twice to make sure it is received. Sometimes, the send does not work
									socket.send(stuff)
									socket.send("Done")			#Send Done so that the Android app knows that sending is complete
									time.sleep(1)
							time.sleep(1)
						else: 
							for j in range(3):					#Send this out to signal that scanning has finished
								socket.send("Scan Finished")
							t = 0	
							sending = False			#Set scanning back to false
							fig.close()				#Close the old figure as we are no longer scanning
							break
					i += 1
			else:
				fig.close()				#Close the old figure as we are no longer scanning
				break	


socket = init_connection()		#Init the connection
while(True):		#This waits and reads for incoming commands.
	command = ""
	try:
		command = socket.recv(100)		#Try to read a command
	except bluetooth.btcommon.BluetoothError:	#The socket has been closed
		print("Connection Closed")
		print("Restarting")
		socket = init_connection()			#Reinit the connection to restart the server
		sending = False
		continue
	print("Received a command: " + command)	
	if command == "Start Scan":			#If the command is to start the scan
		if sending != True:
			sending = True					#Start the scan function on a new Thread
			t1 = Thread(target = scan)
			t1.start()
	elif command == "Reset Scan":			#Restart the scan
		sending = False