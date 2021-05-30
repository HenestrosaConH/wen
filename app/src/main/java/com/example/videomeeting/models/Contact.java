package com.example.videomeeting.models;

public class Contact {

    boolean isContact;

    public Contact() {}

    public Contact(boolean isContact) {
        this.isContact = isContact;
    }

    public boolean isContact() {
        return isContact;
    }
    public void setContact(boolean contact) {
        isContact = contact;
    }
}