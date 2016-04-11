import bluetooth #pybluez
import time
import random
import threading
import serial
import select

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

i = 0
sending = False
def send_pictures():		#This method will send all 11 images in a row, if scanning has started.
	global i
	global sending
	if sending == True:
		images = ["scan_images/0.jpg", "scan_images/1.jpg", "scan_images/2.jpg", "scan_images/3.jpg", "scan_images/4.jpg", "scan_images/5.jpg", "scan_images/6.jpg", "scan_images/7.jpg", "scan_images/8.jpg", "scan_images/9.jpg", "scan_images/10.jpg"]
		item = images[i]
		with open(item, "rb") as image:
			stuff = image.read()
			print("Communicating with Arduino")
			#ser.write("Start")			#Send a request to the Arduino
			#response = ser.readline()   #Wait for a response from the Arduino
			print("Sending Image %d" % i)
			for j in range(5):
				socket.send(stuff)
				socket.send("Done")
				time.sleep(1)
			i += 1
		if i == 11:
			for j in range(3):
				socket.send("Scan Finished")
			i = 0
			sending = False
		else:
			threading.Timer(7.0, send_pictures).start()

while(True):		#This waits and reads for incoming commands.
	command = socket.recv(100)
	print("Received a command: " + command)
	if command == "Start Scan":
		if sending != True:
			sending = True
			send_pictures();
	elif command == "Reset Scan":
		sending = False
		i = 0
