package com.example.myapplication;

public class Post {
    public String id;
    public String replyId;
    public String metin;
    public String image;
    public String username;
    public String date;
    public String pp;
    private int likeCount;
    public Post(String id,String replyId,String metin, String image,String username, String date, String pp, int likeCount) {
        this.id = id;
        this.replyId = replyId;
        this.metin = metin;
        this.image = image;
        this.username = username;
        this.date = date;
        this.pp = pp;
        this.likeCount = likeCount;
    }
}
