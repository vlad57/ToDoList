package com.example.vladup.appdit;

public class ModelNotification {
    private String date;
    private String time;
    private String notifid;
    private String statenotif;
    private String idtask;

    ModelNotification(){

    }

    //Setteurs
    public void setDate(String date) {this.date = date;}
    public void setTime(String time) {this.time = time;}
    public void setNotifid(String notifid) {this.notifid = notifid;}
    public void setStatenotif(String statenotif) {this.statenotif = statenotif;}
    public void setIdtask(String idtask) {this.idtask = idtask;}

    //Getteurs
    public String getDate() {return date;}
    public String getTime() {return time;}
    public String getNotifid() {return notifid;}
    public String getStatenotif() {return statenotif;}
    public String getIdtask() {return idtask;}
}
