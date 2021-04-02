package com.pstiwari.android;

public class LogCall {
    public LogCall(String currnettime, int timeDurationSeconds, String callstatus, String myId, String reciverId, String reciverName, String reciverImage, boolean isVideo) {
        this.currnettime = currnettime;
        this.timeDurationSeconds = timeDurationSeconds;
        this.callstatus = callstatus;
        this.myId = myId;
        this.reciverId = reciverId;
        this.reciverName = reciverName;
        this.reciverImage = reciverImage;
        this.isVideo = isVideo;
    }

    private String currnettime;
    private int timeDurationSeconds;
    private String callstatus, myId, reciverId, reciverName, reciverImage;
    private boolean isVideo;

    public String getCurrnettime() {
        return currnettime;
    }

    public void setCurrnettime(String currnettime) {
        this.currnettime = currnettime;
    }

    public int getTimeDurationSeconds() {
        return timeDurationSeconds;
    }

    public void setTimeDurationSeconds(int timeDurationSeconds) {
        this.timeDurationSeconds = timeDurationSeconds;
    }

    public String getCallstatus() {
        return callstatus;
    }

    public void setCallstatus(String callstatus) {
        this.callstatus = callstatus;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String getReciverId() {
        return reciverId;
    }

    public void setReciverId(String reciverId) {
        this.reciverId = reciverId;
    }

    public String getReciverName() {
        return reciverName;
    }

    public void setReciverName(String reciverName) {
        this.reciverName = reciverName;
    }

    public String getReciverImage() {
        return reciverImage;
    }

    public void setReciverImage(String reciverImage) {
        this.reciverImage = reciverImage;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public LogCall() {
    }


}
