import bluetooth
import time
import random
import threading

serv_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
uuid = "37407000-8cf0-11bd-b23e-10b75c30d20a"

print("Creating Bluetooth Server");

port = bluetooth.PORT_ANY
serv_socket.bind(("", port))
serv_socket.listen(1)
bluetooth.advertise_service(serv_socket, "STM", uuid)

socket, address = serv_socket.accept()
print("Bluetooth Connection has been initiated with" + str(address[0]))

i = 0
def send_pictures():
	global i
	images = ["test2.jpg", "test3.jpg", "test4.jpg"]
	item = images[i % 3]
	with open(item, "rb") as image:
		stuff = image.read()
		print("Sending Image %d" % i)
		socket.send(stuff)
		socket.send("Done")
		i += 1
	threading.Timer(25.0, send_pictures).start()

send_pictures()

while(True):
	command = socket.recv(100)
	print("Received a command: " + command)