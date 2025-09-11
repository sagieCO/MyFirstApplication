package com.sagie.myfirstapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class FrameActivity extends AppCompatActivity {

    Button btnLogin, btnRegister, btnLogout;
    TextView tVMsg, tVStatus;
    EditText etEmail, etPassword;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        auth = FirebaseAuth.getInstance();

        // Init views
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogout = findViewById(R.id.btnLogout);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tVMsg = findViewById(R.id.tVMsg);
        tVStatus = findViewById(R.id.tVStatus);

        // Set onClick listeners
        btnLogin.setOnClickListener(v -> loginUser());
        btnRegister.setOnClickListener(v -> createUser());
        btnLogout.setOnClickListener(v -> auth.signOut());

        // AuthStateListener – יעדכן את הטקסט בזמן אמת
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                tVStatus.setText("משתמש פעיל: " + user.getEmail());
            } else {
                tVStatus.setText("משתמש לא מחובר");
            }
        };
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
                        tVMsg.setText("User created successfully\nEmail: " + user.getEmail());
                    } else {
                        Exception exp = task.getException();
                        if (exp instanceof FirebaseAuthWeakPasswordException) {
                            tVMsg.setText("Password too weak.");
                        } else if (exp instanceof FirebaseAuthUserCollisionException) {
                            tVMsg.setText("User already exists.");
                        } else if (exp instanceof FirebaseNetworkException) {
                            tVMsg.setText("Network error. Please check your connection.");
                        } else {
                            tVMsg.setText("An error occurred. Please try again later.");
                        }
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
                        tVMsg.setText("Logged in successfully\nEmail: " + user.getEmail());
                    } else {
                        Exception exp = task.getException();
                        if (exp instanceof FirebaseAuthInvalidUserException) {
                            tVMsg.setText("User does not exist.");
                        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                            tVMsg.setText("Invalid credentials.");
                        } else if (exp instanceof FirebaseNetworkException) {
                            tVMsg.setText("Network error. Please check your connection.");
                        } else {
                            tVMsg.setText("An error occurred. Please try again later.");
                        }
                    }
                });
    }
}
