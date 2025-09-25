package com.sagie.myfirstapplication;

import static com.sagie.myfirstapplication.FBRef.refStudents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FrameActivity extends AppCompatActivity {

    Button btnLogin, btnRegister, btnLogout, btnHome, btnRead, btnRemove;
    TextView tVMsg, tVStatus;
    EditText etEmail, etPassword;
    ListView ls;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;

    ArrayList<String> stuList = new ArrayList<>();
    ArrayList<Student> stuValues = new ArrayList<>();
    ArrayList<String> studentKeys = new ArrayList<>(); // לשמור את המפתחות של התלמידים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        init();
        auth = FirebaseAuth.getInstance();

        // כפתור חזרה לדף ראשי
        btnHome = findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FrameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // AuthStateListener – יעדכן את הטקסט בזמן אמת
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                tVStatus.setText("משתמש פעיל: " + user.getEmail());
            } else {
                tVStatus.setText("משתמש לא מחובר");
            }
        };

        // כפתור Read
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refStudents.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {
                        stuList.clear();
                        stuValues.clear();
                        studentKeys.clear(); // לא לשכוח לנקות את המפתחות

                        for (DataSnapshot data : ds.getChildren()) {
                            String key = data.getKey(); // שמור את המפתח
                            Student stuTmp = data.getValue(Student.class);
                            stuValues.add(stuTmp);
                            studentKeys.add(key); // שמור את המפתח ברשימה
                            String str2 = stuTmp.getStuName();
                            stuList.add(str2); // הצג רק את שם התלמיד
                        }
                        ArrayAdapter<String> adp = new ArrayAdapter<>(FrameActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, stuList);
                        ls.setAdapter(adp);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tVMsg.setText("שגיאה בטעינת התלמידים");
                    }
                });
            }
        });

        // כפתור Remove
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = ls.getCheckedItemPosition(); // קבל את המיקום של התלמיד שנבחר
                if (position != -1) { // אם יש תלמיד שנבחר
                    String studentKey = studentKeys.get(position); // קבל את המפתח של התלמיד
                    deleteStudentFromDatabase(studentKey); // מחוק את התלמיד מ-Firebase
                } else {
                    tVMsg.setText("אנא בחר תלמיד למחוק");
                }
            }
        });
    }

    public void init() {
        ls = findViewById(R.id.ls);
        btnRemove = findViewById(R.id.btnRemove);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogout = findViewById(R.id.btnLogout);
        btnRead = findViewById(R.id.btnRead);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tVMsg = findViewById(R.id.tVMsg);
        tVStatus = findViewById(R.id.tVStatus);

        // Set onClick listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> createUser());
        btnLogout.setOnClickListener(v -> auth.signOut());
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    // פונקציה ליצירת משתמש חדש
    public void createUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String uid = user.getUid();
                        Student newStudent = new Student(10, 2, email, "123456789");
                        refStudents.child("students").push().setValue(newStudent);

                        tVMsg.setText("User created successfully\nEmail: " + user.getEmail());
                    } else {
                        tVMsg.setText("An error occurred. Please try again later.");
                    }
                });
    }

    // פונקציה להתחברות למשתמש קיים
    public void loginUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in...");
        pd.show();

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        Student newStudent = new Student(10, 2, email, "123456789");
                        refStudents.child("students").push().setValue(newStudent);
                        tVMsg.setText("Logged in successfully\nEmail: " + user.getEmail());
                    } else {
                        tVMsg.setText("An error occurred. Please try again later.");
                    }
                });
    }

    // פונקציה למחיקת תלמיד מ-Firebase
    private void deleteStudentFromDatabase(String studentKey) {
        refStudents.child(studentKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    tVMsg.setText("התלמיד נמחק בהצלחה.");
                    refreshStudentList(); // ריענון הרשימה לאחר מחיקה
                })
                .addOnFailureListener(e -> {
                    tVMsg.setText("שגיאה במחיקת התלמיד: " + e.getMessage());
                });
    }

    // פונקציה לריענון רשימת התלמידים
    private void refreshStudentList() {
        refStudents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                stuList.clear();
                stuValues.clear();
                studentKeys.clear();
                for (DataSnapshot data : ds.getChildren()) {
                    String key = data.getKey();
                    Student stuTmp = data.getValue(Student.class);
                    stuValues.add(stuTmp);
                    studentKeys.add(key);
                    stuList.add(stuTmp.getStuName());
                }
                ArrayAdapter<String> adp = new ArrayAdapter<>(FrameActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, stuList);
                ls.setAdapter(adp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tVMsg.setText("שגיאה בטעינת התלמידים");
            }
        });
    }
}
