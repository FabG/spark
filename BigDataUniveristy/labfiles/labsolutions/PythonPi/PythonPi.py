#Import statements
import sys
from random import random
from operator import add
from pyspark import SparkContext
if __name__ == "__main__":
		"""
			Usage: pi [partitions]
		"""
		#Create the SparkContext
		sc = SparkContext(appName="PythonPi")

		#Run the calculations to estimate Pi
		partitions = int(sys.argv[1]) if len(sys.argv) > 1 else 2
		n = 100000 * partitions
		def f(_):
			x = random() * 2 - 1
			y = random() * 2 - 1
			return 1 if x ** 2 + y ** 2 < 1 else 0

		#Create the RDD, run the transformations, and action to calculate Pi
		count = sc.parallelize(xrange(1, n + 1), partitions).map(f).reduce(add)
		#Print the value of Pi
		print "Pi is roughly %f" % (4.0 * count / n)
		#Stop the SparkContext
		sc.stop()
