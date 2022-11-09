package com.zeeshanvirani.projectcelia;

public class Brews {

    String firebase_id;
    String user_id;
    String date;
    String time;
    String rating;
    String roastTypes;

    public Brews(String firebase_id, String user_id, String date, String time, String roastTypes, String rating) {
        this.firebase_id = firebase_id;
        this.user_id = user_id;
        this.date = date;
        this.time = time;
        this.roastTypes = roastTypes;
        this.rating = rating;
    }

    public String getDateTime(){
        return this.date + " - " + this.time;
    }

}
