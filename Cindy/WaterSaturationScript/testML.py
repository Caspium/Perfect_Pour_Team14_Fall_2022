#test.py

import tensorflow as tf #library for ML
from tensorflow import keras #library to import keras
from tensorflow.keras.preprocessing.image import ImageDataGenerator #keras library for importting images
from tensorflow.keras.models import Sequential, load_model #CNN model
from tensorflow.keras.layers import Dense, Dropout, Activation, Flatten, Conv2D, MaxPooling2D #CNN different layers needed
import numpy as np
import csv
import os
import os.path
from PIL import Image


pimodel = load_model("model_final.h5")
#opens my csv of labels and reads it into a list of labels
with open("singlelabel.csv", newline='') as csvfile:
    labels = list(csv.reader(csvfile))

size = (32,32) #size that I want my image resized to 
    
imgs = [] #list that will hold my images
path = "/home/perfect/Desktop/validationimages/" #path of images folder
valid_images = [".png"] #extension of images
for f in os.listdir(path): #iterate through every image in folder
    ext = os.path.splitext(f)[1] #adding to list
    if ext.lower() not in valid_images:
        continue
imgs.append(np.array(Image.open(os.path.join(path,f)).resize(size,resample=0, box=None))) #opening image & adding path to it then appending it to list

pimodel.summary()
pimodel.compile(optimizer=keras.optimizers.Adam(lr=1e-4), loss='categorical_crossentropy')  
prediction = pimodel.predict(np.array(imgs))

print(prediction)
