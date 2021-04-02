package com.pstiwari.android.Notification;


import com.google.gson.annotations.SerializedName;

import static org.webrtc.ContextUtils.getApplicationContext;

public class RootModel_iOS<T> {
    @SerializedName("to") //  "to" changed to token
    private String token;
    //    @SerializedName("notification")
//    private NotificationModel model = new NotificationModel("Hi","I am gousu");
    @SerializedName("content_available")
    private boolean content = true;
    @SerializedName("mutable-content")
    private boolean mutable = true;
    @SerializedName("apns-push-type")
    private String pushType = "alert";
    @SerializedName("apns-priority")
    private int apns = 10;
    @SerializedName("apns-topic")
    private String topic = getApplicationContext().getPackageName();
    @SerializedName("data")
    private T data;
    @SerializedName("priority")
    private String priority = "high";

    public RootModel_iOS(String token, T data) {
        this.token = token;
        this.data = data;
        topic = getApplicationContext().getPackageName();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isContent() {
        return content;
    }

    public void setContent(boolean content) {
        this.content = content;
    }

    public boolean isMutable() {
        return mutable;
    }

    public void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public int getApns() {
        return apns;
    }

    public void setApns(int apns) {
        this.apns = apns;
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