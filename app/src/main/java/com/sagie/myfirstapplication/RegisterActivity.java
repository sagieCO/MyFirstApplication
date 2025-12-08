package com.sagie.myfirstapplication;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class RegisterActivity extends BaseActivity {

    TextView tvStatus;
    Button btnCreateAccount, btnSaveExtra;
    EditText etEmail, etPassword, etName, etAge, etAddress;
    LinearLayout layoutExtraFields;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_register);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // אתחול Views
        tvStatus = findViewById(R.id.tvStatus);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnSaveExtra = findViewById(R.id.btnSaveExtra);

        layoutExtraFields = findViewById(R.id.main); // אפשר להשתמש ב-main או בכל LinearLayout אחר

        // החלק השני נעול בהתחלה
        etName.setEnabled(false);
        etAge.setEnabled(false);
        etAddress.setEnabled(false);
        btnSaveExtra.setEnabled(false);

        // לחיצה על "צור חשבון" מראה את השדות הנוספים
        btnCreateAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                tvStatus.setText("Please fill all fields");
                return;
            }

            // לא יוצרים עדיין את המשתמש ב-Firebase, רק פותחים את החלק של הפרטים האישיים
            etName.setEnabled(true);
            etAge.setEnabled(true);
            etAddress.setEnabled(true);
            btnSaveExtra.setEnabled(true);
            tvStatus.setText("Fill your personal details below");
        });

        // לחיצה על "שמור פרטים" יוצרת את המשתמש ושומרת את המידע
        btnSaveExtra.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty() || address.isEmpty()) {
                tvStatus.setText("Please fill all personal details");
                return;
            }

            Integer age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                tvStatus.setText("Invalid age");
                return;
            }

            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Connecting");
            pd.setMessage("Creating user...");
            pd.show();

            // יצירת משתמש ב-Firebase Auth
            refAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.dismiss();

                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = refAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String uid = firebaseUser.getUid();

                                    // יצירת משתמש עם השדות האישיים
                                    User newUser = new User(name, age, address, uid);
                                    newUser.saveToFirebase();

                                    tvStatus.setText("User created successfully\nUid: " + uid);

                                    // נעילת השדות כדי למנוע עריכה לאחר השמירה
                                    etName.setEnabled(false);
                                    etAge.setEnabled(false);
                                    etAddress.setEnabled(false);
                                    btnSaveExtra.setEnabled(false);

                                }
                            } else {
                                Exception exp = task.getException();
                                if (exp instanceof FirebaseAuthWeakPasswordException) {
                                    tvStatus.setText("Password too weak.");
                                } else if (exp instanceof FirebaseAuthUserCollisionException) {
                                    tvStatus.setText("User already exists.");
                                } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
                                    tvStatus.setText("Invalid email address.");
                                } else if (exp instanceof FirebaseNetworkException) {
                                    tvStatus.setText("Network error. Please check your connection.");
                                } else {
                                    tvStatus.setText("An error occurred. Please try again later.");
                                }
                            }
                        }
                    });
        });
    }
}
