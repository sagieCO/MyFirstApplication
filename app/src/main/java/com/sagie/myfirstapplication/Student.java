package com.sagie.myfirstapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Student {
    private int gradeClass;
    private int stuClass;
    private String stuName;
    private String stuID;
    public Student (int gradeClass, int stuClass, String stuName, String stuID) {
        this.gradeClass = gradeClass;
        this.stuClass = stuClass;
        this.stuName = stuName;
        this.stuID = stuID;
    }
    public Student () {}
    public void saveToFirebase() {
        // יצירת הפנייה למסד הנתונים
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("students"); // הפנייה לטבלת students

        // הוספת התלמיד עם מזהה ייחודי
        ref.child(stuID).setValue(this);
    }
    public int getGradeClass() {
        return gradeClass;
    }

    public void setGradeClass(int gradeClass) {
        this.gradeClass = gradeClass;
    }

    public int getStuClass() {
        return stuClass;
    }

    public void setStuClass(int stuClass) {
        this.stuClass = stuClass;
    }

    public String getStuName() {
        return stuName;
    }

    public void setStuName(String stuName) {
        this.stuName = stuName;
    }

    public String getStuID() {
        return stuID;
    }

    public void setStuID(String stuID) {
        this.stuID = stuID;
    }
}
