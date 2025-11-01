package com.sagie.myfirstapplication;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    TextView tvStatus;
    Button btnCreateAccount;
    EditText etEmail, etPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initView();
    }

    private void initView() {
        tvStatus = findViewById(R.id.tvStatus);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnCreateAccount.setOnClickListener(v -> createUser());
    }

    private void createUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            tvStatus.setText("Please fill all fields");
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
                            FirebaseUser firebaseUser = refAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                tvStatus.setText("User created successfully\nUid: " + uid);

                                // יצירת משתמש עם שדות ראשוניים כ-null
                                User newUser = new User(null, null, null, uid);

                                // שמירה בעץ היררכי ב-Firebase Realtime Database
                                DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
                                refUsers.child(uid).setValue(newUser)
                                        .addOnCompleteListener(saveTask -> {
                                            if (saveTask.isSuccessful()) {
                                                Log.d("Register", "User saved to database successfully");
                                            } else {
                                                Log.e("Register", "Failed to save user to database", saveTask.getException());
                                            }
                                        });
                            }
                        } else {
                            Exception exp = task.getException();
                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                tvStatus.setText("Invalid email address.");
                            } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                tvStatus.setText("Password too weak.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                tvStatus.setText("User already exists.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                tvStatus.setText("General authentication failure.");
                            } else if (exp instanceof FirebaseNetworkException) {
                                tvStatus.setText("Network error. Please check your connection.");
                            } else {
                                tvStatus.setText("An error occurred. Please try again later.");
                            }
                        }
                    }
                });
    }
}
