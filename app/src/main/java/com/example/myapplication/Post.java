package com.example.myapplication;

public class Post {
    public String id;
    public String replyId;
    public String postType;
    public String metin;
    public String image;

    public String username;
    public String date;


    public Post(String id,String replyId,String postType,String metin, String image,String username, String date ) {
        this.id = id;
        this.replyId = replyId;
        this.postType = postType;
        this.metin = metin;
        this.image = image;
        this.username = username;
        this.date = date;


    }

}
