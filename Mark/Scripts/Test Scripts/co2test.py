import os
import glob
import time
import RPi.GPIO as GPIO
import time
import csv
from scd30_i2c import SCD30

scd30 = SCD30()

scd30.set_measurement_interval(2)
scd30.start_periodic_measurement()
start = time.perf_counter()
with open('co2.csv', 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    while True:
        if scd30.get_data_ready():
            m = scd30.read_measurement()
            if m is not None:
                print(f"CO2: {m[0]:.2f}ppm, temp: {m[1]:.2f}'C, rh: {m[2]:.2f}%")
                writer.writerow([str(time.perf_counter()-start), m[0]])
            time.sleep(2)
        else:
            time.sleep(0.2)
