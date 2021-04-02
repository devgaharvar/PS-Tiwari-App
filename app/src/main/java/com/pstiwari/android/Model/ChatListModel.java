package com.pstiwari.android.Model;

public class ChatListModel {

    String device_token;
    String image;
    String name;
    String phone;
    String status;
    String uid;
    public ChatListModel()
    {

    }
    public ChatListModel(String device_token, String image, String name, String phone, String status, String uid) {
        this.device_token = device_token;
        this.image = image;
        this.name = name;
        this.phone = phone;
        this.status = status;
        this.uid = uid;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}
