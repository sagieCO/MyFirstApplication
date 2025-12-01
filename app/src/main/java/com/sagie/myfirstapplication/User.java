package com.sagie.myfirstapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private String name;
    private String email;

    private String age;
    private String address;
    private String uid; // ניתן להוסיף UID כאן, או להשתמש בו מה-auth

    // קונסטרקטור מלא
    public User(String name, String age, String address, String uid) {
        this.name = name;
        this.age = age;
        this.address = address;
        this.uid = uid;
    }

    // קונסטרקטור ריק – דרוש על ידי Firebase
    public User() {}

    // פונקציה לשמירה ב-Firebase
    public void saveToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users"); // טבלת users

        // אם יש UID שמירה לפי UID, אחרת נוציא אוטומטית מזהה חדש
        if (uid != null) {
            ref.child(uid).setValue(this);
        } else {
            ref.push().setValue(this);
        }
    }

    // Getters ו-Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String  age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
