package com.sagie.myfirstapplication.Activities;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;

public class LoginActivity extends BaseActivity {

    private Button btnLogin, btnRegister, btnReset;
    private TextView tvMessage;
    private EditText etEmail, etPassword;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_login);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isChecked = sharedPref.getBoolean("stayConnect", false);

        FirebaseUser currentUser = refAuth.getCurrentUser();

        if (currentUser != null) {
            if (isChecked) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                refAuth.signOut();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            FBRef.refAuth.removeAuthStateListener(authListener);
        }
    }

    private void initView() {
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void resetFields() {
        etEmail.setText("");
        etPassword.setText("");
        tvMessage.setText("");
    }

    public void loginUser() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            tvMessage.setText("נא למלא את כל השדות");
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("התחברות");
        pd.setMessage("מתחבר למערכת, נא להמתין...");
        pd.setCancelable(false);
        pd.show();

        refAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = refAuth.getCurrentUser();
                            if (user != null) {
                                tvMessage.setText("התחברת בהצלחה!");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("USER_EMAIL", user.getEmail());
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Exception exp = task.getException();

                            if (exp instanceof FirebaseAuthInvalidUserException) {
                                tvMessage.setText("כתובת האימייל לא נמצאה במערכת.");
                            } else if (exp instanceof FirebaseAuthWeakPasswordException) {
                                tvMessage.setText("הסיסמה חלשה מדי.");
                            } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                tvMessage.setText("המשתמש כבר קיים במערכת.");
                            } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                tvMessage.setText("פרטי התחברות שגויים (אימייל או סיסמה).");
                            } else if (exp instanceof FirebaseNetworkException) {
                                tvMessage.setText("שגיאת רשת. נא לבדוק את החיבור לאינטרנט.");
                            } else {
                                if (exp != null) {
                                    tvMessage.setText("שגיאה: " + exp.getMessage());
                                } else {
                                    tvMessage.setText("אירעה שגיאה בלתי צפויה.");
                                }
                            }
                        }
                    }
                });
    }
}