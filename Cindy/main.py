import firebase_admin  # needed for connecting to database
from firebase_admin import credentials  # needed for certifying password to database
from firebase_admin import firestore  # needed for admin access to database
import pandas as pd  # for pandas dataframe - structure of organizing data
from sklearn.model_selection import train_test_split  # used to split dataset into training and testing dataset
from sklearn import neighbors  # import library for KNN
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix, f1_score, precision_score


# imports tools needed to verify results of models

def main(p1, p2):
    # the purpose of this is to gain access to the database
    if not firebase_admin._apps:  # using a if not statement to prevent overwriting over same password key
        # saves to cred variable of admin password key
        cred = credentials.Certificate("cert.json")

        # calls upon firebase admin library and inserts password key to gain access to private database
        default_app = firebase_admin.initialize_app(cred)

    db = firestore.client()  # saves db as the database
    docs = db.collection('trainingandvalidating').stream()  # reads from all documents in a collection of the database
    data = pd.DataFrame()  # this creates the pandas dataframe containing all of the user feedback from the database
    for doc in docs:  # iterates through the document stream
        data = data.append(doc.to_dict(), ignore_index=True)  # appends it to my pandas dataframe
    # this replaces all of the string values of coffee bean type and converting it to float values
    data['bean_type'].loc[data['bean_type'] == 'robusta'] = 1.0  # converting robusta to 1
    data['bean_type'].loc[data['bean_type'] == 'arabica'] = 2.0  # converting arabica to 2
    data['bean_type'].loc[data['bean_type'] == 'liberica'] = 3.0  # converting liberica to 3
    data['bean_type'].loc[data['bean_type'] == 'excelsa'] = 4.0  # converting excelsa to 4

    # this replaces all of the string values of grind size and converting it to float values
    data['grind_size'].loc[data['grind_size'] == 'medium'] = 0.0  # converting medium to 0
    data['grind_size'].loc[data['grind_size'] == 'larger'] = 1.0  # converting larger to 1
    data['grind_size'].loc[data['grind_size'] == 'smaller'] = 2.0  # converting smaller to 2

    # this replaces all of the string values of roast type and converting it to float values
    data['roast_type'].loc[data['roast_type'] == 'mild'] = 0.0  # converting mild to 0
    data['roast_type'].loc[data['roast_type'] == 'medium'] = 1.0  # converting medium to 1
    data['roast_type'].loc[data['roast_type'] == 'mediumdark'] = 2.0  # converting mediumdark to 2
    data['roast_type'].loc[data['roast_type'] == 'dark'] = 3.0  # converting dark to 3

    # converts all strings/objects into floats
    data = data._convert(numeric=True)

    # deletes all non-null responses
    data.dropna(
        subset=['target_saturation', 'strength', 'cup_size', 'grind_size', 'bean_type', 'user_id', 'temperature',
                'roast_type', 'rating'], inplace=True)

    ###############SATURATION KNN########################
    #first splitting 60% out for training then 40% for validation & testing
    x_train, x_test, y_train, y_test = train_test_split(data[['roast_type','bean_type']].values, data[['target_saturation']].values, test_size=0.2, random_state=42) 

    #then splitting the 40% into 20% validation & 20% testing
    x_train, x_validate, y_train, y_validate = train_test_split(x_train, y_train, test_size=0.25, random_state=42) 

    knn_sat = neighbors.KNeighborsClassifier(n_neighbors=9) #set K Nearest Neighbors model as variable 
    knn_sat.fit(x_train, y_train) #fitting training independent and dependent data 
    y_pred = knn_sat.predict(x_validate) #model predict independent validation dataset values

    ############TEMP KNN#######################
    #first splitting 60% out for training then 40% for validation & testing
    x_train, x_test, y_train, y_test = train_test_split(data[['roast_type','bean_type']].values, data[['temperature']].values, test_size=0.2, random_state=42) 

    #then splitting the 40% into 20% validation & 20% testing
    x_train, x_validate, y_train, y_validate = train_test_split(x_train, y_train, test_size=0.25, random_state=42) 

    knn_temp = neighbors.KNeighborsClassifier(n_neighbors=9) #set K Nearest Neighbors model as variable 
    knn_temp.fit(x_train, y_train) #fitting training independent and dependent data 
    y_pred = knn_temp.predict(x_validate) #model predict independent validation dataset values

    ################GRIND SIZE##########################
    # first splitting 60% out for training then 40% for validation & testing
    x_train, x_test, y_train, y_test = train_test_split(data[['roast_type', 'bean_type']].values,
                                                        data[['grind_size']].values, test_size=0.2, random_state=42)

    # then splitting the 40% into 20% validation & 20% testing
    x_train, x_validate, y_train, y_validate = train_test_split(x_train, y_train, test_size=0.25, random_state=42)

    knn_grind = neighbors.KNeighborsClassifier(n_neighbors=9)  # set K Nearest Neighbors model as variable
    knn_grind.fit(x_train, y_train)  # fitting training independent and dependent data
    y_pred = knn_grind.predict(x_validate)  # model predict independent validation dataset values


    # now reading from database to implement machine learning
    docs = db.collection('brews').stream()  # reads from all documents in a collection of the database
    data = pd.DataFrame()  # this creates the pandas dataframe containing all of the user feedback from the database
    doc_id = []
    for doc in docs:  # iterates through the document stream
        doc_id.append(doc.id)
        data = data.append(doc.to_dict(), ignore_index=True)  # appends it to my pandas dataframe

    # this replaces all of the string values of coffee bean type and converting it to float values
    data['bean_type'].loc[data['bean_type'] == 'Robusta'] = 1.0  # converting robusta to 1
    data['bean_type'].loc[data['bean_type'] == 'Arabica'] = 2.0  # converting arabica to 2
    data['bean_type'].loc[data['bean_type'] == 'Liberica'] = 3.0  # converting liberica to 3
    data['bean_type'].loc[data['bean_type'] == 'Excelsa'] = 4.0  # converting excelsa to 4

    # this replaces all of the string values of grind size and converting it to float values
    # data['grind_size'].loc[data['grind_size'] == 'medium'] = 0.0 #converting medium to 0
    # data['grind_size'].loc[data['grind_size'] == 'larger'] = 1.0 #converting larger to 1
    # data['grind_size'].loc[data['grind_size'] == 'smaller'] = 2.0 #converting smaller to 2

    # this replaces all of the string values of roast type and converting it to float values
    data['roast_type'].loc[data['roast_type'] == 'Light'] = 0.0  # converting mild to 0
    data['roast_type'].loc[data['roast_type'] == 'Medium'] = 1.0  # converting medium to 1
    data['roast_type'].loc[data['roast_type'] == 'MedDark'] = 2.0  # converting mediumdark to 2
    data['roast_type'].loc[data['roast_type'] == 'Dark'] = 3.0  # converting dark to 3

    data = data._convert(numeric=True)  # converts all strings/objects into floats

    path_parts = p2.resource.split('/documents/')[1].split('/')
    brewdocid = '/'.join(path_parts[1:])
    recent_data = db.collection("brews").document(brewdocid).stream
    recent_entry = pd.DataFrame() #this creates the pandas dataframe containing all of the user feedback from the database
    for doc in recent_data: #iterates through the document stream
        data = data.append(doc.to_dict(),ignore_index=True) # appends it to my pandas dataframe

    if recent_entry['rating'].values == 10.0: #retains values if ratings are 10
        #runs knn prediction on rating, saturation, temp, strength, and grind to train model
        rating_prediction = knn_strength.predict(recent_entry[['roast_type','bean_type']].values)
        saturation_prediction = knn_sat.predict(recent_entry[['roast_type','bean_type']].values)
        temp_prediction = knn_temp.predict(recent_entry[['roast_type','bean_type']].values)
        strength_prediction = knn_strength.predict(recent_entry[['roast_type','bean_type']].values)
        grind_prediction = knn_grind.predict(recent_entry[['roast_type','bean_type']].values)
        
    elif recent_entry['rating'].values < 10.0: #implements algorithm if ratings are less than 10
        #runs knn prediction on rating, saturation, temp, strength, and grind to 

        rating_prediction = knn_strength.predict(recent_entry[['roast_type','bean_type']].values)
        saturation_prediction = knn_sat.predict(recent_entry[['roast_type','bean_type']].values)
        temp_prediction = knn_temp.predict(recent_entry[['roast_type','bean_type']].values)
        strength_prediction = knn_strength.predict(recent_entry[['roast_type','bean_type']].values)
        grind_prediction = knn_grind.predict(recent_entry[['roast_type','bean_type']].values)

        for i in grind_prediction: #pulling data from grind prediction array to add to database
            grind_predict = i
        
        for i in temp_prediction: #pulling data from temp prediction array to add to database
            temp_predict = i

        for i in saturation_prediction: #pulling data from saturation prediction array to add to database
            sat_predict = i

        users_ref = db.collection("brews").document(brewdocid).update({ #updates document to include new grind suggestion
            'grind_size': str(grind_predict)
        })

        useridstring = recent_entry['user_id'].values #converting user id into string from array
        for i in useridstring:
            useridstring = i

        temp_ref = db.collection("users").document(str(useridstring)).update({ #updates document to include next temperature
            'next_target_temperature': str(temp_predict)
        })

        sat_ref = db.collection("users").document(str(useridstring)).update({ #updates document to include next water saturation
            'next_target_saturation': str(sat_predict)
        })

