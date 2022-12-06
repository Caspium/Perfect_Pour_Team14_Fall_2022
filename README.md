#Add instructions for device setup here


##########Machine Learning & Database Set Up#######################

To run the water saturation algorithm, refer to the testML.py script and the model_final.h5 nested in Cindy\WaterSaturationScript. Save both files to your dedicated Raspberry Pi along with creating a folder in the same path to house where the image inputs should be saved to during the brew. To run the machine learning, type python3 testML.py and it should automatically the algorithm and predict the water saturation labels for the images saved in the folder.

To run the user feedback script, create a firebase database account and set up a dedicated cloud. Remember to upgrade your account to include cloud functions capabilities. Refer to Cindy\UserFeedbackScript to for the main.py script and upload that script to the firebase database cloud and set the trigger to only occur when a change happens in the database collections also known as the brews folder to update. The script already includes lines that have the password to the database and enable you to access the database from the script.

Android Application Setup

The source code for the application can be found within the "Zeeshan" folder. Open up the source code and edit the DataHandler.java file. Line 31 "DEVICE_MACADDR" needs to equal the mac address to the bluetooth device the application should search and connect to. Afterwards the app can be built and compiled into an apk and installed on an android device. 

##########Hardware Setup#######################

Rasbian Bullseye should be installed on the Rassberry Pi within the device

The files within Mark\Scripts\Production Scripts should be placed in the home directory

The libraries imported within main.py should be installed via pip

To launch the program, run startupmain.sh over bash
This can be set to autmatically start when the device boots by adding the bash script path to /etc/rc.local


