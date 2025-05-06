package com.example.shortvideo.model;

public class UserModel {
    private String id;
    private String name;
    private String password;
    private String uriImg;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Bắt buộc có constructor rỗng cho Firebase
    public UserModel() {
    }

    public UserModel(String id, String name, String password, String uriImg, String email) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.uriImg = uriImg;
        this.email = email;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUriImg() {
        return uriImg;
    }

    public void setUriImg(String uriImg) {
        this.uriImg = uriImg;
    }
}
