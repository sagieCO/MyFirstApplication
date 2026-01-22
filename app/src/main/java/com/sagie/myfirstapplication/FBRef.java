package com.sagie.myfirstapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FBRef {
    // התחברות לחשבון משתמשים
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static FirebaseFirestore FBFS = FirebaseFirestore.getInstance();
    public static CollectionReference refImages = FBFS.collection("Images");
    public static FirebaseDatabase fbRef = FirebaseDatabase.getInstance();
    public static DatabaseReference usersRef = fbRef.getReference("users");
    public static DatabaseReference mechinotRef = fbRef.getReference("mechinot");
    // חיבור למסד נתונים

    // מצביע לשורש "Students" במסד הנתונים
}
