#Python script that calls Water Saturation Machine Learning algorithm to activate in the Raspberry Pi

import tensorflow as tf #library for ML
from tensorflow import keras #library to import keras
from keras.preprocessing.image import ImageDataGenerator #keras library for importting images
from keras.models import Sequential, load_model#CNN model
from keras.layers import Dense, Dropout, Activation, Flatten, Conv2D, MaxPooling2D #CNN different layers needed
import numpy as np #for plotting 
import csv
import os
import os.path
from PIL import Image

def watsat()
    pimodel = load_model("model.h5")