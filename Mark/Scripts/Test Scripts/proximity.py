import os
import time
import RPi.GPIO as GPIO
GPIO.setmode(GPIO.BOARD)
GPIO.setup(13, GPIO.IN)
# Gives continuous output to determine whether carafe or other object
# is present in front of the sensro
while(True):
    print(GPIO.input(13)) #0 indicates proximity, 1 indicates abscence
