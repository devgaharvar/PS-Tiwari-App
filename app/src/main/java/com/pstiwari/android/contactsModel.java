package com.pstiwari.android;

public class contactsModel {

    String Contacts;
    String key;
    public contactsModel()
    {}
    public contactsModel(String contacts, String key) {
        Contacts = contacts;
        this.key = key;
    }

    public String getContacts() {
        return Contacts;
    }

    public void setContacts(String contacts) {
        Contacts = contacts;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
