import csv

output = []

with open('test_data2.csv', 'rb') as f:
	reader = csv.reader(f)
	for row in reader:
		output.append(row[0:50])

with open('test_data3.csv', 'w') as out:
	for row in output:
		out.write(','.join(row) + '\n')