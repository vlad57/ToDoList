package com.example.vladup.appdit;

public class Model {

    private String id;
    private String title;
    private String content;
    private String date;
    private String color;
    //private String favoris;
    private boolean isSelected;

    //Le putain de constructeur de ces morts
    public Model(){
    }

    public Model (String Id, String Title, String Content, String Date, String Color, Boolean isSelected){
        this.id = Id;
        this.title = Title;
        this.content = Content;
        this.date = Date;
        this.color = Color;
        this.isSelected = isSelected;
    }

    public Model (String Id, String Title, String Content, String Date, String Color){
        this.id = Id;
        this.title = Title;
        this.content = Content;
        this.date = Date;
        this.color = Color;
    }

    //Méthodes de récupérations
    public String getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getContent(){return this.content;}
    public String getDate(){return this.date;}
    public String getColor(){return this.color;}
    public boolean isSelected(){return isSelected;}

    //Méthodes de setage
    public void setId(String Id){this.id = Id;}
    public void setTitle(String Title){this.title = Title;}
    public void setContent(String Content){this.content = Content;}
    public void setDate(String Date){this.date = Date;}
    public void setColor(String Color){this.color = Color;}
    public void setSelected(boolean isSelected){this.isSelected = isSelected;}
}

