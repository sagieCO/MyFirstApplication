package com.sagie.myfirstapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBRef {
    // התחברות לחשבון משתמשים
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    // חיבור למסד נתונים
    public static FirebaseDatabase FBDB = FirebaseDatabase.getInstance();

    // מצביע לשורש "Students" במסד הנתונים
    public static DatabaseReference refStudents = FBDB.getReference("Students");
}
