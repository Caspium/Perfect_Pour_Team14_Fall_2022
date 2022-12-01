#!/usr/bin/python3

from picamera2 import Picamera2

from smbus import SMBus
import time
import os
import sys
import numpy as np
import io
from PIL import Image

from pprint import *



i2cbus = SMBus(1)
### LIGHT CONTROL ###
#Registers
MODE1 = 0x00
MODE2 = 0x01
PWM0 = 0x02
PWM1 = 0x03
PWM2 = 0x04
PWM3 = 0x05
PWM4 = 0x06
PWM5 = 0x07
PWM6 = 0x08
PWM7 = 0x09
GRPPWM = 0x0A
GRPFREQ = 0x0B
LEDOUT0 = 0x0C
LEDOUT1 = 0x0D

#Addresses
DRIVER1 = 0x40
DRIVER2 = 0x41
DRIVER3 = 0x42
DRIVER4 = 0x43
ALLCALL = 0x48

colorname = dict([(525,"525"), (590,"590"), (625,"625"), (680,"680"), (780,"780"), (810,"810"), (870,"870"), (930,"930")])
colorreg = dict([ (525,LEDOUT0), (590,LEDOUT0), (625,LEDOUT0), (680,LEDOUT0), (780,LEDOUT1), (810,LEDOUT1), (870,LEDOUT1), (930,LEDOUT1)])
colorregval = dict([(525,0x80), (590,0x20), (625,0x08), (680,0x02), (780,0x80), (810,0x20), (870,0x08), (930,0x02)])
pwmreg = dict([(525,PWM3), (590,PWM2), (625,PWM1), (680,PWM0), (780,PWM7), (810,PWM6), (870,PWM5), (930,PWM4)])
# initialize/reset the device
def initialize():
    i2cbus.write_byte_data(ALLCALL, MODE1, 0x01)


def color525(brightness):
    i2cbus.write_byte_data(ALLCALL,PWM0, brightness) #680
    i2cbus.write_byte_data(ALLCALL,PWM7, brightness) #780
    i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x02) #680
    i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x80) #780
def color590(brightness):
    i2cbus.write_byte_data(ALLCALL,PWM1, brightness) #625
    i2cbus.write_byte_data(ALLCALL,PWM6, brightness) #810
    i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x08) #625
    i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x20) #810
def color680(brightness):
    i2cbus.write_byte_data(ALLCALL,PWM2, brightness) #590
    i2cbus.write_byte_data(ALLCALL,PWM5, brightness) #870
    i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x20) #590
    i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x08) #870
def color930(brightness):
    i2cbus.write_byte_data(ALLCALL,PWM3, brightness) #525
    i2cbus.write_byte_data(ALLCALL,PWM4, brightness) #930
    i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x80) #525
    i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x02) #930



print("Initializing Ring")
initialize()
### ADJUST LED BRIGHTNESS HERE ###
brightness525 = 0xff
brightness590 = 0xff
brightness680 = 0xff
brightness930 = 0xff


print("Initializing Camera")
picam2 = Picamera2()
#pprint(picam2.sensor_modes)

### IMAGE RESOLUTION & CONFIG ###
picam2.still_configuration.main.size = (640, 480)
picam2.still_configuration.align()
picam2.configure("still")
picam2.set_controls({"ExposureTime": 40000, "AnalogueGain": 1.0, "AwbEnable": 0, "AeEnable": 0})
picam2.start()
time.sleep(1)


## CAL

framedelay = 0.03
print("Frame delay:", framedelay)
testname = input("INPUT TEST NAME: ")
input("START TEST <enter>")
# 525
color525(brightness525)
time.sleep(framedelay)
array = picam2.capture_array("main")
array = Image.fromarray(array)
array.save(testname + '_525' + ".png")
# 590
color590(brightness590)
time.sleep(framedelay)
array = picam2.capture_array("main")
array = Image.fromarray(array)
array.save(testname + '_590' + ".png")
# 680
color680(brightness680)
time.sleep(framedelay)
array = picam2.capture_array("main")
array = Image.fromarray(array)
array.save(testname + '_680' + ".png")
# 930
color930(brightness930)
time.sleep(framedelay)
array = picam2.capture_array("main")
array = Image.fromarray(array)
array.save(testname + '_930' + ".png")



i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x00)
i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x00)
