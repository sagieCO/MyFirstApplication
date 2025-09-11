package com.sagie.myfirstapplication;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class FrameActivity extends AppCompatActivity {

    Button btnHome, btnLogin, btnLogout;
    TextView tVMsg;
    EditText etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        btnLogin = findViewById(R.id.btnLogin);
        // btnHome = findViewById(R.id.btnHome); // אם יש כפתור כזה ב-XML
        btnLogout = findViewById(R.id.btnLogout);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tVMsg = findViewById(R.id.tVMsg);

        btnLogin.setOnClickListener(v -> createUser());

        // התנתקות
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            tVMsg.setText("התנתקת בהצלחה");
        });

        // בדיקה אם כבר משתמש מחובר
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            tVMsg.setText("משתמש מחובר: " + currentUser.getEmail());
        }
    }

    public void createUser() {
        String email = etEmail.getText().toString();
        String pass = etPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            tVMsg.setText("Please fill all fields");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Creating user...");
        pd.show();

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();

                        if (task.isSuccessful()) {
                            Log.i("FrameActivity", "createUserWithEmailAndPassword:success");
                            FirebaseUser user = refAuth.getCurrentUser();
                            tVMsg.setText("User created successfully\nUid: " + user.getUid());
                        } else {
                            Exception exp = task.getException();

                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                tVMsg.setText("Invalid email address.");
                            } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                tVMsg.setText("Password too weak.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                tVMsg.setText("User already exists.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                tVMsg.setText("General authentication failure.");
                            } else if (exp instanceof FirebaseNetworkException) {
                                tVMsg.setText("Network error. Please check your connection.");
                            } else {
                                tVMsg.setText("An error occurred. Please try again later.");
                            }
                        }
                    }
                });
    }
}
