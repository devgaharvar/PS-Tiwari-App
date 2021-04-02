package com.pstiwari.android.Notification;


public class CallNotification {
    String senderName,senderAva,to,channelId,callerId,callerName,callerAva,nType ="voiceCall" ,senderId,Mid;

    public CallNotification() {
    }

    public CallNotification(String senderName, String senderAva, String to, String channelId, String callerId, String callerName, String callerAva, String senderId, String mid) {
        this.senderName = senderName;
        this.senderAva = senderAva;
        this.to = to;
        this.channelId = channelId;
        this.callerId = callerId;
        this.callerName = callerName;
        this.callerAva = callerAva;
        this.senderId = senderId;
        Mid = mid;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAva() {
        return senderAva;
    }

    public void setSenderAva(String senderAva) {
        this.senderAva = senderAva;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerAva() {
        return callerAva;
    }

    public void setCallerAva(String callerAva) {
        this.callerAva = callerAva;
    }

    public String getnType() {
        return nType;
    }

    public void setnType(String nType) {
        this.nType = nType;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMid() {
        return Mid;
    }

    public void setMid(String mid) {
        Mid = mid;
    }


}
