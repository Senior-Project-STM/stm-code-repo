import bluetooth
import time
import random

nearby_devices = bluetooth.discover_devices(lookup_names=True)
target = None
for addr, name in nearby_devices:
	if name == "Galaxy S6":
		target = addr
		print("Initiating Connection to Galaxy S6")


socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

print("Connecting to socket")
socket.connect((target, 5))

images = ["test2.jpg", "test3.jpg", "test4.jpg"]
i = 0

while True:
	for item in images:
		with open(item, "rb") as image:
			stuff = image.read()
			print("Sending Image %d" % i)
			socket.send(stuff)
			socket.send("Done")
			i += 1
			time.sleep(5)