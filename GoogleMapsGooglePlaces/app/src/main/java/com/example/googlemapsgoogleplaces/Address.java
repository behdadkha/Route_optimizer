package com.example.googlemapsgoogleplaces;

public class Address {
    private String address;
    private boolean isDone;

    public Address(String address, boolean isDone) {
        this.address = address;
        this.isDone =  isDone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}
