import tensorflow as tf #library for ML
from tensorflow import keras #library to import keras
from keras.preprocessing.image import ImageDataGenerator #keras library for importting images
from keras.models import Sequential #CNN model
from keras.layers import Dense, Dropout, Activation, Flatten, Conv2D, MaxPooling2D #CNN different layers needed
import numpy as np #for plotting 
import matplotlib.pyplot as plt #for plotting
#from sklearn.metrics import classification_report, confusion_matrix #used to view results of CNN model

model = Sequential()

#input layer (1) 
#filters -> number of filters model will learn = 16 
# kernel size = 2-tuple specifying the width and height of the 2D convolution window (3x3)
# padding = same to preserve the spatial dimensions of the volume for output volume size to match the input volume size
# activiation function used relu -> best loss functions
# input shape = number of images, pixel x pixel value, number of layers
model.add(Conv2D(filters = 16, kernel_size = (3,3), padding='same', activation='relu', input_shape=(32, 32, 3))) 

# max pooling filter to reduce the dimensions of the feature maps by 2x2 (2)
model.add(MaxPooling2D((2,2)))

# conv2D layer (4)
model.add(Conv2D(filters = 16, kernel_size = (3,3), padding='same', activation='relu', input_shape=(32, 32, 3))) 

#max pooling filter to reduce the dimensions of the feature maps by 2x2 (5)
model.add(MaxPooling2D((2,2)))

# flatten layer - to Flattening a tensor means to remove all of the dimensions except for one (7)
model.add(Flatten())  

# dense layer feeds all outputs from the previous layer to all its neurons,
# each neuron providing one output to the next layer (8)
model.add(Dense(16, activation='relu'))

# dense layer repeats previous dense layer but outputs it to 32 instead (9)
model.add(Dense(32, activation='relu'))

# output layer (10)
#11 = number of classes it can be sorted to, linear regression model so use linear
model.add(Dense(11, activation='softmax')) 

# compiles the model utilizing Adam learning rate of 1e-4 & loss function as mean squared log error
# attempted using mean square error, mean abs error, & sparse categorical but best was msle
# accuracy was not used since image regression model focuses only on loss functions
# model.compile(optimizer=keras.optimizers.Adam(lr=1e-4), loss='msle')  
model.compile(optimizer=keras.optimizers.Adam(lr=1e-4), loss='categorical_crossentropy')  

#provides summary of layers within the cnn model, # of parameters inputted, & output shape
model.summary() 

print('is this working?')