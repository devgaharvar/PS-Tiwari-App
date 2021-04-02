package com.pstiwari.android.Notification;


import com.google.gson.annotations.SerializedName;

public class RootModel_Android<T> {

    @SerializedName("to") //  "to" changed to token
    private String token;
    @SerializedName("data")
    private T data;
    @SerializedName("priority")
    private String priority = "high";

    public RootModel_Android(String token, T data) {
        this.token = token;
        this.data = data;
        this.priority = priority;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}