package com.sagie.myfirstapplication.models;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    private String name;
    private String email;
    private String birthDate;
    private String address;
    private String uid;

    public User() {}

    public User(String name, String email, String birthDate, String address, String uid) {
        this.name = name;
        this.email = email;
        this.birthDate = birthDate;
        this.address = address;
        this.uid = uid;
    }

    public Task<Void> saveToFirebase() {
        return FirebaseDatabase.getInstance().getReference("users").child(uid).setValue(this);
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public String getUid() {
        return uid;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}