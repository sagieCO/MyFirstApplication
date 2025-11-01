package com.sagie.myfirstapplication;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private Button btnLogin,btnRegister,btnLogout;
    private TextView tvMessage;
    private CheckBox isCheck;
    private EditText etEmail, etPassword;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // התאמה ל־EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        initView();

        // AuthStateListener – יעדכן את הטקסט בזמן אמת
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                tvMessage.setText("משתמש פעיל: " + user.getEmail());
            } else {
                tvMessage.setText("משתמש לא מחובר");
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);

        // בדיקה אם המשתמש כבר מחובר וה־CheckBox מסומן
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isChecked = sharedPref.getBoolean("stayConnect", false);

        if (refAuth.getCurrentUser() != null && isChecked) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish(); // סוגר את מסך ההתחברות
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    private void initView() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvMessage = findViewById(R.id.tvMessage);
        isCheck = findViewById(R.id.cbStayLoggedIn);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogout=findViewById(R.id.btnLogout);
        // כפתור התחברות – מפעיל loginUser()
        btnLogin.setOnClickListener(v -> loginUser());
        btnLogout.setOnClickListener(v -> auth.signOut());


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,Register.class);
                startActivity(intent);
            }
        });
    }


    // פונקציה לקריאה מ־XML (כפתור התחבר)
    public void loginUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            tvMessage.setText("נא למלא את כל השדות");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Connecting");
        pd.setMessage("Logging in user...");
        pd.show();

        refAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = refAuth.getCurrentUser();
                        tvMessage.setText("User logged in successfully");

                        // שמירה של הסטטוס של ה-CheckBox
                        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("stayConnect", isCheck.isChecked());
                        editor.apply();

                        // מעבר למסך הראשי
                        // מעבר למסך הראשי עם מידע על המשתמש
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("USER_EMAIL", user.getEmail()); // שולח את המייל
                        startActivity(intent);
                        finish();

                    } else {
                        Exception exp = task.getException();
                        if (exp instanceof FirebaseAuthInvalidUserException) {
                            tvMessage.setText("Invalid email address.");
                        } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                            tvMessage.setText("Password too weak.");
                        } else if (exp instanceof FirebaseAuthUserCollisionException) {
                            tvMessage.setText("User already exists.");
                        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                            tvMessage.setText("General authentication failure.");
                        } else if (exp instanceof FirebaseNetworkException) {
                            tvMessage.setText("Network error. Please check your connection.");
                        } else {
                            tvMessage.setText("An error occurred. Please try again later.");
                        }
                    }
                });
    }
}
