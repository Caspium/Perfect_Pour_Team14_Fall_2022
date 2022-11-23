
# def cnn():
#     #building the CNN model
#     model = Sequential()

#     # input layer (1) 
#     #filters -> number of filters model will learn = 16 
#     # kernel size = 2-tuple specifying the width and height of the 2D convolution window (3x3)
#     # padding = same to preserve the spatial dimensions of the volume for output volume size to match the input volume size
#     # activiation function used relu -> best loss functions
#     # input shape = number of images, pixel x pixel value, number of layers
#     model.add(Conv2D(filters = 16, kernel_size = (3,3), padding='same', activation='relu', input_shape=(32, 32, 3))) 

#     # max pooling filter to reduce the dimensions of the feature maps by 2x2 (2)
#     model.add(MaxPooling2D((2,2)))

#     # conv2D layer (4)
#     model.add(Conv2D(filters = 16, kernel_size = (3,3), padding='same', activation='relu', input_shape=(32, 32, 3))) 

#     #max pooling filter to reduce the dimensions of the feature maps by 2x2 (5)
#     model.add(MaxPooling2D((2,2)))

#     # flatten layer - to Flattening a tensor means to remove all of the dimensions except for one (7)
#     model.add(Flatten())  

#     # dense layer feeds all outputs from the previous layer to all its neurons,
#     # each neuron providing one output to the next layer (8)
#     model.add(Dense(16, activation='relu'))

#     # dense layer repeats previous dense layer but outputs it to 32 instead (9)
#     model.add(Dense(32, activation='relu'))

#     # output layer (10)
#     #11 = number of classes it can be sorted to, linear regression model so use linear
#     model.add(Dense(11, activation='softmax')) 

#     # compiles the model utilizing Adam learning rate of 1e-4 & loss function as mean squared log error
#     # attempted using mean square error, mean abs error, & sparse categorical but best was msle
#     # accuracy was not used since image regression model focuses only on loss functions
#     # model.compile(optimizer=keras.optimizers.Adam(lr=1e-4), loss='msle')  
#     model.compile(optimizer=keras.optimizers.Adam(lr=1e-4), loss='categorical_crossentropy')  

#     #provides summary of layers within the cnn model, # of parameters inputted, & output shape
#     model.summary() 

# def tran_cnn():
#     # this is to train the neural network model
#     # determines steps per epoch for training = the number of batches to be selected for one epoch 
#     STEP_SIZE_TRAIN = train_generator.n//train_generator.batch_size 

#     # determines steps per epoch for validating = the number of batches to be selected for one epoch 
#     STEP_SIZE_VALID = valid_generator.n//valid_generator.batch_size

#     model.fit_generator(generator=train_generator, #takes train generator function from above & has model fit it to training data
#                         steps_per_epoch=STEP_SIZE_TRAIN, #pulls steps per epoch value above for training
#                         validation_data=valid_generator, #takes validate generator function from above & has model test val data
#                         validation_steps=STEP_SIZE_VALID, #pulls steps per epoch value above for validating
#                         epochs=20 # epochs may be increased but best loss was at 20 epochs
#             ) 
#     #train different epochs & show side by side

# def train():
#     import csv
#     import os
#     import os.path
#     from PIL import Image
#     #opens my csv of labels and reads it into a list of labels
#     with open(r"C:\Users\cindy\OneDrive\Documents\403\inputimages\singlelabel.csv", newline='') as csvfile:
#         labels = list(csv.reader(csvfile))

#     size = (32,32) #size that I want my image resized to 
        
#     imgs = [] #list that will hold my images
#     path =  r"C:\Users\cindy\OneDrive\Documents\403\inputimages\single" #path of images folder
#     valid_images = [".png"] #extension of images
#     for f in os.listdir(path): #iterate through every image in folder
#         ext = os.path.splitext(f)[1] #adding to list
#         if ext.lower() not in valid_images:
#             continue
#         imgs.append(np.array(Image.open(os.path.join(path,f)).resize(size,resample=0, box=None))) #opening image & adding path to it then appending it to list

#     for i in range(len(imgs)): #iterates through my images list and scales it by 255 (RGB)
#         imgs[i] = (imgs[i].astype(float) / 255.0) #need to preprocess data, need to scale pixels to be between 0-1
#         i=i+1
    
#     predictions = model.predict(np.array(imgs)) #prediction on my single image
#     print(predictions) #print array 
#     plt.imshow(imgs[0])

#     plt.figure()

import tensorflow as tf #library for ML
from tensorflow import keras #library to import keras
from keras.preprocessing.image import ImageDataGenerator #keras library for importting images
from keras.models import Sequential #CNN model
from keras.layers import Dense, Dropout, Activation, Flatten, Conv2D, MaxPooling2D #CNN different layers needed
import numpy as np #for plotting 
import matplotlib.pyplot as plt #for plotting
from sklearn.metrics import classification_report, confusion_matrix #used to view results of CNN model

converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir) # path to the SavedModel directory
tflite_model = converter.convert()

# Save the model.
with open('model.tflite', 'wb') as f:
  f.write(tflite_model)

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