package com.sagie.myfirstapplication;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class RegisterActivity extends BaseActivity {

    TextView tvStatus;
    Button btnCreateAccount, btnSaveExtra, btnSkip;
    EditText etEmail, etPassword, etName, etAge, etAddress;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        btnSkip = findViewById(R.id.btnSkip);

        // שדות אישיים נעולים בהתחלה
        setPersonalFieldsEnabled(false);

        // ---------------------------------------------------
        // כפתור 1 — צור חשבון (יוצר משתמש ואז מבקש פרטים אישיים)
        // ---------------------------------------------------
        btnCreateAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                tvStatus.setText("אנא מלא אימייל וסיסמה");
                return;
            }

            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("יוצר חשבון...");
            pd.show();

            refAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            tvStatus.setText("החשבון נוצר! השלם פרטים אישיים למטה.");

                            // ⭐ שמירת hasDetails = false
                            FirebaseUser firebaseUser = refAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                DatabaseReference ref =
                                        FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(firebaseUser.getUid());
                                ref.child("hasDetails").setValue(false);
                            }

                            // פותח את השדות האישיים
                            setPersonalFieldsEnabled(true);

                        } else {
                            handleError(task.getException());
                        }
                    });
        });

        // ---------------------------------------------------
        // כפתור 2 — Remind Me Later (יוצר משתמש בלי פרטים אישיים)
        // ---------------------------------------------------
        btnSkip.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                tvStatus.setText("אנא מלא אימייל וסיסמה");
                return;
            }

            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("יוצר חשבון...");
            pd.show();

            refAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = refAuth.getCurrentUser();
                            if (firebaseUser != null) {

                                // ⭐ שמירת hasDetails = false
                                DatabaseReference ref =
                                        FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(firebaseUser.getUid());
                                ref.child("hasDetails").setValue(false);
                            }

                            tvStatus.setText("חשבון נוצר! ניתן למלא פרטים אישיים בהמשך.");

                            // כאן אפשר להעביר למסך הראשי
                            // startActivity(new Intent(this, MainActivity.class));
                            // finish();
                        } else {
                            handleError(task.getException());
                        }
                    });
        });

        // ---------------------------------------------------
        // שמירת פרטים אישיים
        // ---------------------------------------------------
        btnSaveExtra.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() || ageStr.isEmpty() || address.isEmpty()) {
                tvStatus.setText("אנא מלא את כל הפרטים האישיים");
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (Exception e) {
                tvStatus.setText("גיל לא תקין");
                return;
            }

            FirebaseUser user = refAuth.getCurrentUser();
            if (user == null) {
                tvStatus.setText("שגיאה: אין משתמש מחובר");
                return;
            }

            // יצירת אובייקט המשתמש
            User newUser = new User(name, age, address, user.getUid());
            newUser.saveToFirebase();

            // ⭐ עדכון hasDetails = true
            DatabaseReference ref =
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid());
            ref.child("hasDetails").setValue(true);

            tvStatus.setText("הפרטים נשמרו בהצלחה!");
            setPersonalFieldsEnabled(false);
        });
    }

    // הפעלה/ניטרול של שדות אישיים
    private void setPersonalFieldsEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etAge.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        btnSaveExtra.setEnabled(enabled);
    }

    // טיפול בשגיאות
    private void handleError(Exception exp) {
        if (exp instanceof FirebaseAuthWeakPasswordException) {
            tvStatus.setText("הסיסמה חלשה מדי");
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            tvStatus.setText("משתמש קיים כבר");
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            tvStatus.setText("כתובת מייל לא תקינה");
        } else if (exp instanceof FirebaseNetworkException) {
            tvStatus.setText("בעיה ברשת");
        } else {
            tvStatus.setText("שגיאה לא מוכרת: " + exp.getMessage());
        }
    }
}
