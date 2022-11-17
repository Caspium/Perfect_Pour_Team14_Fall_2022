from __future__ import print_function

from numpy.core.numeric import zeros_like
import picamera
from smbus import SMBus
import time

import os

import RPi.GPIO as GPIO
import sys
import picamera.array
import numpy as np
import io
from PIL import Image
#import matplotlib.pyplot as plt
#import matplotlib.image as mpimg
#import cv2
from gpiozero import LED

from csv import writer

from skimage.draw import disk
from skimage.draw import circle_perimeter


from picamera.array import PiRGBArray
from picamera import PiCamera
import cv2


from picamera import mmal, mmalobj, exc
from picamera.mmalobj import to_rational

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

colorname = ["525", "590", "625", "680", "780", "810", "870", "930"]

#Reset Pin setup
reset_pin = LED(14)
reset_pin.on()

i2cbus = SMBus(1)

# initialize/reset the device
def initialize():
    i2cbus.write_byte_data(ALLCALL, MODE1, 0x01)

#reset the drivers
def reset():
    reset_pin.off()
    time.sleep(0.5)
    reset_pin.on()

# enable colors/brightness
def color(color, state, brightness):
    # 525
    if (color == 0):
        i2cbus.write_byte_data(ALLCALL, PWM3, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x80)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x00)
    # 590
    if (color == 1):
        i2cbus.write_byte_data(ALLCALL, PWM2, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x20)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x00)
    # 625
    if (color == 2):
        i2cbus.write_byte_data(ALLCALL, PWM1, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x08)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x00)
    # 680
    if (color == 3):
        i2cbus.write_byte_data(ALLCALL, PWM0, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x02)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x00)
    # 780
    if (color == 4):
        i2cbus.write_byte_data(ALLCALL, PWM7, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x80)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x00)
    # 810
    if (color == 5):
        i2cbus.write_byte_data(ALLCALL, PWM6, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x20)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x00)
    # 870
    if (color == 6):
        i2cbus.write_byte_data(ALLCALL, PWM5, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x08)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x00)
    # 930
    if (color == 7):
        i2cbus.write_byte_data(ALLCALL, PWM4, brightness)
        if state:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x02)
        else:
            i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x00)

# all colors
def colorall(state):
    if state:
        i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0xAA)
        i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0xAA)
    else:
        i2cbus.write_byte_data(ALLCALL, LEDOUT0, 0x00)
        i2cbus.write_byte_data(ALLCALL, LEDOUT1, 0x00)


### Camera Control ###
# max res is 3280x2464, but might run out of memory
width = 640
height = 480
# The horizontal resolution is rounded up to the nearest multiple of 32 pixels, 
# while the vertical resolution is rounded up to the nearest multiple of 16 pixels. 
# For example, if the requested resolution is 100x100, the capture will actually contain 128x112 pixels worth of data, but pixels beyond 100x100 will be uninitialized.
array_height = height
array_width = width
if (width % 32):
    array_width = width + (32 - (width % 32) )
if (height % 16):
    array_height = height + (16 - (height % 16))

camera = PiCamera()
camera.resolution = (width, height)
camera.video_stabilization = False
#camera.iso = 400

#camera.shutter_speed = 800
camera.start_preview()

camera.framerate = 80
rawCapture = PiRGBArray(camera, size=(width, height))
#time.sleep(2)
#camera.framerate = 5

#stream = picamera.PiCameraCircularIO(camera, )

# RGB Format
def rgbsnapshot():
    #camera.start_preview()
    #time.sleep(2)
    output = np.empty((array_height * array_width * 3), dtype=np.uint8)
    camera.capture(output, 'rgb')
    output = output.reshape((array_height, array_width, 3))
    output = output[:height, :width, :]
    return output

# YUV420 Format
def yuvsnapshot():
    print("opening stream")
    output = np.empty((array_height, array_width), dtype = np.uint8)
    try:
        camera.capture(output, 'yuv')
    except IOError:
        pass
    output = output[:array_height, :array_width]
    return output



### Camera Initialization ###
# default brightness is 0, the maximum
# brightness ranges from 0 - 255
#brightnessarray = [0x08, 0x40, 0x02, 0x02, 0x08, 0x08, 0x10, 0x20] AMIR

# Last Calibration 1/26
brightnessarray = [0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff]
#will be changed once white lip incorporated
def brightness_calibration(color_):
    colorall(False)
    saturated = True
    sensitivity = 10 # needs testing to find approprite value
    brightness_threshold = 180
    print(colorname[color_])
    while(saturated):
            color(color_, True, brightnessarray[color_])
            if(color_ == 0):
                num_pixels_sat = (yuvsnapshot() > brightness_threshold).sum() + (rgbsnapshot()[:, :, 1] > brightness_threshold).sum()
            else:
                num_pixels_sat = (yuvsnapshot() > brightness_threshold).sum() + (rgbsnapshot()[:, :, 0] > brightness_threshold).sum()
            print("There are " + str(num_pixels_sat) + " with " + str(brightnessarray[color_]) + " brightness")
            if (brightnessarray[color_] < 0):
                brightnessarray[color_] = 0x1
                break
            elif(num_pixels_sat <= sensitivity):
                saturated = False
            else:
                brightnessarray[color_] -= 0x2
    return

def brightnessall():
    for i in range(8):
        brightness_calibration(i)
    print(brightnessarray)
    return

### Image Processing ###

def normalize_white(rinput):
    avg = np.average(rinput)
    normarray =  np.full_like(rinput, avg)
    normarray = normarray/rinput
    return normarray

def lock_gain():
    camera.exposure_mode = 'off'
    g = camera.awb_gains
    print("Debugging: Gain is:", g)
    camera.awb_mode = 'off'
    camera.awb_gains = g


MMAL_PARAMETER_ANALOG_GAIN = mmal.MMAL_PARAMETER_GROUP_CAMERA + 0x59
MMAL_PARAMETER_DIGITAL_GAIN = mmal.MMAL_PARAMETER_GROUP_CAMERA + 0x5A

def set_gain(camera, gain, value):
    """Set the analog gain of a PiCamera.
    
    camera: the picamera.PiCamera() instance you are configuring
    gain: either MMAL_PARAMETER_ANALOG_GAIN or MMAL_PARAMETER_DIGITAL_GAIN
    value: a numeric value that can be converted to a rational number.
    """
    if gain not in [MMAL_PARAMETER_ANALOG_GAIN, MMAL_PARAMETER_DIGITAL_GAIN]:
        raise ValueError("The gain parameter was not valid")
    ret = mmal.mmal_port_parameter_set_rational(camera._camera.control._port, 
                                                    gain,
                                                    to_rational(value))
    if ret == 4:
        raise exc.PiCameraMMALError(ret, "Are you running the latest version of the userland libraries? Gain setting was introduced in late 2017.")
    elif ret != 0:
        raise exc.PiCameraMMALError(ret)

def set_analog_gain(camera, value):
    """Set the gain of a PiCamera object to a given value."""
    set_gain(camera, MMAL_PARAMETER_ANALOG_GAIN, value)

def set_digital_gain(camera, value):
    """Set the digital gain of a PiCamera object to a given value."""
    set_gain(camera, MMAL_PARAMETER_DIGITAL_GAIN, value)


camera.exposure_mode = 'off'
analog_gain = 0.5
digital_gain = 0.5
print("Attempting to set analogue gain to:", analog_gain)
set_analog_gain(camera, 0.8)
print("Attempting to set digital gain to:", digital_gain)
set_digital_gain(camera, 1)


test_name = input("Test Name: ")
duration = 9999

save_en = True
reset()
initialize()
time.sleep(0.1)

header = ['Time', '525', '590', '625', '680', '780', '810', '870', '930',
         '525_norm', '590_norm', '625_norm', '680_norm', '780_norm', '810_norm', '870_norm', '930_norm']
output_list = [0,0,0,0,0,0,0,0,0]
averaging_array = [
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0],
    [0,0,0,0,0,0,0,0]]

output_930 = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
with open((str(test_name) + '.csv'), 'w') as csvout:
    write = writer(csvout)
    write.writerow(header)
    i = 0
    j = 0
    pic_num = 0
    print("########Starting Capture########")
    counter = time.perf_counter()
    start_time = time.perf_counter()
    for frame in camera.capture_continuous(rawCapture, format="bgr", use_video_port=True):
        
    
        #reset()
        color(0, False, brightnessarray[0])
        color(7, False, brightnessarray[7])
        if(i<7):
            color(i+1, True, brightnessarray[i+1]) ##################
        else:
            color(0, True, brightnessarray[0])
        
        image = frame.array
        Ycoords, Xcoords = disk((height//2 - 15, width//2-10), 60)
        Y_outline, X_outline = circle_perimeter(height//2 - 15, width//2-10, 60)
        if (i == 0): #green
            avg = np.average(image[Ycoords, Xcoords, 1])
        else: #red
            avg = np.average(image[Ycoords, Xcoords, 2])
        print(colorname[i] + ": " + str(avg))
        averaging_array[i][0] = avg
        

        if(save_en):
            #image[Y_outline, X_outline] = [0,0,0]
            image_conv = np.array(image[:,:,::-1])
            #image_conv[Y_outline, X_outline] = [0,0,0]
            image_out = Image.fromarray(image_conv)
            #image_conv[Y_outline, X_outline] = [0,0,0]
            image_out.save(test_name + '_' + colorname[i] + '_' + str(pic_num) + ".png")
            if (i==7):
                pic_num += 1
        

        rawCapture.truncate(0)
    
        if i==7:
            print("FPS: " + str(1/((time.perf_counter()-counter)/8)))
            print(time.perf_counter()-start_time)
        if ((time.perf_counter() - start_time ) > duration)  and i==7:
            break
        #increment
        if(i==7):
            i=0
            counter = time.perf_counter()
            
                
        
        else:
            i+=1
        if(i==0):
            if(True):
                out_525 = averaging_array[0][0]
                out_590 = averaging_array[1][0]
                out_625 = averaging_array[2][0]
                out_680 = averaging_array[3][0]
                out_780 = averaging_array[4][0]
                out_810 = averaging_array[5][0]
                out_870 = averaging_array[6][0]
                out_930 = averaging_array[7][0]
                output_list = [
                    (time.perf_counter() - start_time),
                    out_525,
                    out_590,
                    out_625,
                    out_680,
                    out_780,
                    out_810,
                    out_870,
                    out_930,
                    out_525/out_810,
                    out_590/out_810,
                    out_625/out_810,
                    out_680/out_810,
                    out_780/out_810,
                    1,
                    out_870/out_810,
                    out_930/out_810,
                    ]

                write.writerow(output_list)
        
        #print(time.perf_counter()-counter)
print("########Testing Completed########")
