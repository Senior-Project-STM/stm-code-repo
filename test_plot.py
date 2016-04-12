import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import csv
import time
import os
import threading
from threading import Thread

#sample min: -2.24e-12
#sample max: 2.75e-10

v_array = np.zeros((1,1))

def get_data():
	global v_array

	print 'start getting'
	values = list()
	with open('test_data.csv', 'rb') as f:
		reader = csv.reader(f)
		for row in reader:
			if 'Channel' in row[0]:
				channel = row[0].split(':')[1].strip()
			elif 'Width' in row[0]:
				width = row[0].split(':')[1].strip()
			elif 'Height' in row[0]:
				height = row[0].split(':')[1].strip()
			elif 'Value units' in row[0]:
				value_units = row[0].split(':')[1].strip()
			else:
				row = map(float,row)
				values.append(row)
				v_array = np.asarray(values)
				time.sleep(1)

def get_random_data():
	global v_array
	print 'start getting random'

	v_array = np.random.uniform(-2.24e-12, 2.75e-10, size=(1,400))
	print np.shape(v_array)
	time.sleep(1)
	for i in range(1,400):
		row = np.random.uniform(-2.24e-12, 2.75e-10, size=(1,400))
		v_array = np.append(v_array, row)
		print np.shape(v_array)
		time.sleep(1)

def plot_data():
	global v_array

	print 'start plotting'
	fig = plt.figure()
	ax1 = fig.add_subplot(1,1,1)
	ax1.set_xlim([0, 400])
	ax1.set_ylim([400, 0])
	cmap = 'afmhot'
	vmin = -2e-12
	vmax = 5e-11
	#so that colorbar works later
	im = ax1.pcolor(v_array, cmap=cmap, vmin=vmin, vmax=vmax)

	def animate(i):
		ax1.clear()
		ax1.set_xlim([0, 400])
		ax1.set_ylim([400, 0])
		im = ax1.pcolor(v_array, cmap=cmap, vmin=vmin, vmax=vmax)

	anim = animation.FuncAnimation(fig, animate, interval=1000)#, repeat=False)
	fig.colorbar(im)
	plt.show(block=True)

if __name__ == '__main__':
	t1 = Thread(target = get_data)
	#t1 = Thread(target = get_random_data)
	t2 = Thread(target = plot_data)

	t1.start()
	t2.start()

	t1.join()
	t2.join()
	
	print 'done'
	#...it never finishes...... b/c animation never ends