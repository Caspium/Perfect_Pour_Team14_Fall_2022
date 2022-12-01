from bluetooth import *
import RPi.GPIO as GPIO
import time
import threading
import glob
#Camera
from picamera2 import Picamera2

from smbus import SMBus
import time
import os
import sys
import numpy as np
import io
from PIL import Image

from pprint import *

#MACHINE LEARNING LIBRARIES#
import tensorflow as tf #library for ML
from tensorflow import keras #library to import keras
from tensorflow.keras.preprocessing.image import ImageDataGenerator #keras library for importting images
from tensorflow.keras.models import Sequential, load_model #CNN model
from tensorflow.keras.layers import Dense, Dropout, Activation, Flatten, Conv2D, MaxPooling2D #CNN different layers needed
#import numpy as np
import csv
#import os
import os.path
#from PIL import Image

# WATER ML INIT

pimodel = load_model("model.h5")
#opens my csv of labels and reads it into a list of labels
with open("singlelabel.csv", newline='') as csvfile:
    labels = list(csv.reader(csvfile))
size = (32,32) #size that I want my image resized to 


# WIRLESS INIT

uuid = "b3f75a8f-fa4b-4dbc-8e79-51a486a30fa9"

messagequeue = ["BREWING_COMPLETE"]
#messagequeue = []

water_ready = False
brew_in_progress = False

# HARDWARE INIT
# Setting up pin IO and device files
GPIO.setmode(GPIO.BOARD)
# Valve
GPIO.setup(32, GPIO.OUT)
GPIO.output(32, GPIO.LOW)
# Heat
GPIO.setup(15, GPIO.OUT)
GPIO.output(15, GPIO.LOW)
# Proximity Sensor INPUT
GPIO.setup(13, GPIO.IN)
# Temp Sensor
# These tow lines mount the device:
os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')
base_dir = '/sys/bus/w1/devices/'
# Get all the filenames begin with 28 in the path base_dir.
# 28 denotes that it's probably a temp sensor
device_folder = glob.glob(base_dir + '28*')[0]
device_file = device_folder + '/w1_slave'

# Initialize Camera
i2cbus = SMBus(1) # I2C BUS for Ring Light
### LIGHT CONTROL ###
#Registers within LED Drivers
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

#Addresses of LED Drivers over I2C
DRIVER1 = 0x40
DRIVER2 = 0x41
DRIVER3 = 0x42
DRIVER4 = 0x43
ALLCALL = 0x48

framedelay = 0.03 # ensures capture happens after led toggles
# Set of dictionaries to extract certain variables based on wavelength
colorname = dict([(525,"525"), (590,"590"), (625,"625"), (680,"680"), (780,"780"), (810,"810"), (870,"870"), (930,"930")])
colorreg = dict([ (525,LEDOUT0), (590,LEDOUT0), (625,LEDOUT0), (680,LEDOUT0), (780,LEDOUT1), (810,LEDOUT1), (870,LEDOUT1), (930,LEDOUT1)])
colorregval = dict([(525,0x80), (590,0x20), (625,0x08), (680,0x02), (780,0x80), (810,0x20), (870,0x08), (930,0x02)])
pwmreg = dict([(525,PWM3), (590,PWM2), (625,PWM1), (680,PWM0), (780,PWM7), (810,PWM6), (870,PWM5), (930,PWM4)])
# initialize/reset the device
def initialize():
    i2cbus.write_byte_data(ALLCALL, MODE1, 0x01)

# calling each function will toggle and set brightness of leds, of the wavelength
# numbers to the side coincide with what that register was for in the original ring light
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
picam2.still_configuration.main.size = (640, 480) # resolution
picam2.still_configuration.align()
picam2.configure("still") # type of capture to perform: still image
# set gains to static numbers so that images are not processed to be uniform
picam2.set_controls({"ExposureTime": 40000, "AnalogueGain": 1.0, "AwbEnable": 0, "AeEnable": 0}) 
picam2.start()
time.sleep(1)

# reads the raw data from the temp sensor
def read_temp_raw():
    f = open(device_file, 'r')
    lines = f.readlines()
    f.close()
    return lines

# translates raw temp sensor data into f and c temp values and returns both
def read_temp():
    lines = read_temp_raw()
    # Analyze if the last 3 characters are 'YES'.
    while lines[0].strip()[-3:] != 'YES':
        time.sleep(0.2)
        lines = read_temp_raw()
    # Find the index of 't=' in a string.
    equals_pos = lines[1].find('t=')
    if equals_pos != -1:
        # Read the temperature .
        temp_string = lines[1][equals_pos+2:]
        temp_c = float(temp_string) / 1000.0
        temp_f = temp_c * 9.0 / 5.0 + 32.0
        return temp_c, temp_f




def begin_brewing( temp_target, low_target, volume_target):
    # Carafe Detection
    if(GPIO.input(13)): #If prox returns 1 (missing carafe)
        send_message("Carafe not detected. Brew Aborting")
        return

    # variable tells pouring thread whether the water is up to temp yet
    global water_ready
    water_ready =  False

    # variable tells heating thread whether we're still brewing or not
    global brew_in_progress
    brew_in_progress = True

    # creates and starts our heating thread passing the temp target as an argument
    heat_thread = threading.Thread(target=heating_control, args=(temp_target,))
    heat_thread.start()
    send_message("STARTED_HEATING")
    time.sleep(5)

    # creates and starts the pouring thread with the flow and volume arguments
    pour_thread = threading.Thread(target=pour, args=(flow_target, volume_target))
    pour_thread.start()
    time.sleep(5)

    # wait for pouring to finish and for the pour thread to join
    pour_thread.join()

    # If the pour thread joins, the brew is over and heating can stop
    brew_in_progress = False

    heat_thread.join() # Blocking wait for heat to successfully end
    # Inform User/App Coffee is ready
    send_message("BREWING_COMPLETE")
    return

# send message to app
# STARTED_HEATING and STARTED_POURING denote stage
# BREWING_COMPLETE will end connection with app
# any other messages sent will display message to user
# other messages will also end connection with app as they are assumed to be irrecoverable errors
def send_message(messagetosend):
    try:
        client_sock.send(messagetosend)
    except IOError:
        messagequeue.append(messagetosend)


def heating_control(temp):
    temp = 120 #overwrite app submitted temperature
    print("Heat control starting at :", temp)
    
    global water_ready 
    water_ready =  False # whether water is up to temp
    notified = False # whether we've let pour thread know to start
    
    i = 0
    while(brew_in_progress):
        # read temp sensor
        temps = read_temp()
        f = temps[1]

        # print temp every 50 samples
        if(not(i%50)):
            print("Current temp:", i, f)
        i+=1

        # binary control system
        if(int(f)<int(temp)):
            GPIO.output(15, GPIO.HIGH)
        else:
            GPIO.output(15, GPIO.LOW)
            # Print to console when water is up to temp
            if(not notified):
                print("Target Temp reached, stabilizing")
                water_ready = True
                notified = True
            
            

def pour(flow_target, volume_target):
    
    flow_target = float(flow_target)
    volume_target = float(volume_target)

    # Water valve control
    GPIO.output(32, GPIO.LOW)

    # SIZES 12 24 32
    floz_per_sec = 8/60 # able to pour 8 oz of water in 60 seconds
    amount_poured = 0
    # Change flow target to accomodate older versions of database
    if(flow_target == 12):
        flow_target = 50
    if(flow_target == 14):
        flow_target = 50
    if(flow_target == 16):
        flow_target = 50
    if(flow_target == 18):
        flow_target = 75
    if(flow_target == 20):
        flow_target = 100
    
    # idle while waiting for water
    while(not water_ready):
        time.sleep(1)
    # temp usually overshoots, so wait for it to settle
    print("pour hears water is ready")
    print("giving temp time to stabilize")
    time.sleep(20)
    print("done stabilizing")


    default_pour_time = 30 # 30 seconds of pouring or 4 oz

    send_message("STARTED_POURING")
    # loop pouring until within 1 oz of target
    while(amount_poured < volume_target-1):
        # open valve and pour for 30 seconds * flow multiplier
        GPIO.output(32, GPIO.HIGH)
        time.sleep(default_pour_time*(flow_target/100))
        GPIO.output(32, GPIO.LOW)
        amount_poured += default_pour_time*(flow_target/100) * floz_per_sec
        print("Amount Poured:", amount_poured)
        
        saturated = True

        
        # waits until camera says it's not too saturated or 20 seconds elapses
        start = time.time() # when drain wait started
        while(saturated):
            # START ML check
            imgs = [] #list that will hold images

            framedelay = 0.03
            # capture the multispectral image set
            # 525
            color525(brightness525)
            time.sleep(framedelay)
            array = picam2.capture_array("main")
            array = Image.fromarray(array)
            array.save("/home/perfect/Desktop/validationimages/"  + '_525' + ".png")
            # 590
            color590(brightness590)
            time.sleep(framedelay)
            array = picam2.capture_array("main")
            array = Image.fromarray(array)
            array.save("/home/perfect/Desktop/validationimages/"  + '_590' + ".png")
            # 680
            color680(brightness680)
            time.sleep(framedelay)
            array = picam2.capture_array("main")
            array = Image.fromarray(array)
            array.save("/home/perfect/Desktop/validationimages/"  + '_680' + ".png")
            # 930
            color930(brightness930)
            time.sleep(framedelay)
            array = picam2.capture_array("main")
            array = Image.fromarray(array)
            array.save("/home/perfect/Desktop/validationimages/"  + '_930' + ".png")

            # bring the captured images back from storage
            path = "/home/perfect/Desktop/validationimages/" #path of images folder
            valid_images = [".png"] #extension of images
            for f in os.listdir(path): #iterate through every image in folder
                ext = os.path.splitext(f)[1] #adding to list
                if ext.lower() not in valid_images:
                    continue
                imgs.append(np.array(Image.open(os.path.join(path,f)).resize(size,resample=0, box=None))) #opening image & adding path to it then appending it to list

            #pimodel.summary()
            # input images to ML algo
            pimodel.compile(optimizer=keras.optimizers.Adam(lr=1e-4), loss='categorical_crossentropy')  
            prediction = pimodel.predict(np.array(imgs))

            prediction = prediction.astype(int)
            # trusts the max value recieved from each input
            for i in range(len(prediction[0])):
                if prediction[0][i] == 1:
                    wave_525 = i

            for i in range(len(prediction[1])):
                if prediction[1][i] == 1:
                    wave_590 = i

            for i in range(len(prediction[2])):
                if prediction[2][i] == 1:
                    wave_680 = i

            for i in range(len(prediction[3])):
                if prediction[3][i] == 1:
                    wave_930 = i
            # max guess of the 4 images
            max_label = max([wave_525, wave_590,wave_680,wave_930])
            print("ML Saturation Predictions:", wave_525, wave_590,wave_680,wave_930)
            #print(wave_525)
            #print(wave_590)
            #print(wave_680)
            #print(wave_930)
            if((max_label<5) or (time.time()-start)>20):
                saturated = False

    return



# START WIRELESS COMM LOOP

while True:
    server_sock = BluetoothSocket(RFCOMM)
    server_sock.bind(("",PORT_ANY))
    server_sock.listen(1)
    
    port = server_sock.getsockname()[1]
    
    advertise_service( server_sock, "BTS",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ] )

    print("Waiting for connection on RFCOMM channel %d" % port)

    client_sock, client_info = server_sock.accept()
    print("Accepted connection from ", client_info)
    # send any outstanding messages
    if len(messagequeue) > 0:
        print("There are messages to send. Sending them now.")
        for x in messagequeue:
            client_sock.send(x)

        messagequeue.clear()
    else:
        print("The message queue is empty.")

    try:
        while True:
            data = client_sock.recv(1024).decode("utf-8")
            if len(data) == 0: break
            print("Received:", data)
            ndata = data.split(':')
            if ndata[0] == "START_BREW":
                print("Starting Brew with", ndata[1], "and", ndata[2], "and", ndata[3])
                temp_target = ndata[1]
                flow_target = ndata[2]
                volume_target = ndata[3]
                begin_brewing(float(temp_target), float(flow_target), float(volume_target))
            elif ndata[0] == "CONTINUE_BREWING":
                print("Continue Brewing Process...")

    except IOError:
        pass

    print("Disconnected")

    client_sock.close()
    server_sock.close()
    print("All Closed")
