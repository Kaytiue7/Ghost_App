package com.example.myapplication;

public class PostUser {
    private String username;
    private String profilePhoto;

    public PostUser(String username, String profilePhoto) {
        this.username = username != null ? username : "Unknown User";
        this.profilePhoto = profilePhoto != null ? profilePhoto : "default_profile_photo.png"; // Varsayılan resim
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    @Override
    public String toString() {
        return "PostUser{" +
                "username='" + username + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                '}';
    }
}
