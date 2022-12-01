import os
import time
import RPi.GPIO as GPIO
# set valve IO to output
GPIO.setmode(GPIO.BOARD)
GPIO.setup(32, GPIO.OUT)

# Valve off
GPIO.output(32, GPIO.LOW)
time.sleep(5)
# Valve on
GPIO.output(32, GPIO.HIGH)
# 60 seconds equates to 8 oz of water
time.sleep(60)
# valve off
GPIO.output(32, GPIO.LOW)
