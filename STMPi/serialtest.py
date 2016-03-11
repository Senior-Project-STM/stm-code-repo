import serial
import time

ser = serial.Serial('/dev/ttyACM3', 9600)

while(True):
	ser.write("Start")
	time.sleep(5)