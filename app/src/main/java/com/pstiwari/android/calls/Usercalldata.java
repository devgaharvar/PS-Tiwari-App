package com.pstiwari.android.calls;

public class Usercalldata {
    boolean incall;
    public Usercalldata() {
    }
    public Usercalldata(boolean incall) {
        this.incall = incall;
    }

    public boolean isIncall() {
        return incall;
    }

    public void setIncall(boolean incall) {
        this.incall = incall;
    }
}
