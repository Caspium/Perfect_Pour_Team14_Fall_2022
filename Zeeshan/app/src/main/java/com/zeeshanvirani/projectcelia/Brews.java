package com.zeeshanvirani.projectcelia;

public class Brews {

    String firebase_id;
    String user_id;
    String date;
    String time;
    String rating;
    String roastTypes;
    String beanType;
    String strength;

    public Brews(String firebase_id, String user_id, String date, String time, String roastTypes, String rating, String beanType, String strength) {
        this.firebase_id = firebase_id;
        this.user_id = user_id;
        this.date = date;
        this.time = time;
        this.roastTypes = roastTypes;
        this.beanType = beanType;
        this.rating = rating;
        this.strength = strength;
    }

    public String getDateTime(){
        return this.date + " - " + this.time;
    }

}
