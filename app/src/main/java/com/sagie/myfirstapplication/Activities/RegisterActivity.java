package com.sagie.myfirstapplication.Activities;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private TextView tvStatus;
    private Button btnRegister;
    private EditText etEmail, etPassword, etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת ה-Layout
        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_register);

        // הגדרת כיוון טקסט לימין (RTL)
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // אתחול רכיבי ה-UI
        initView();

        // הגדרת מאזין לכפתור הרישום
        btnRegister.setOnClickListener(v -> performRegistration());
    }

    private void initView() {
        tvStatus = findViewById(R.id.tvStatus);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnCreateAccount); // משתמש ב-ID הקיים מה-XML שלך
    }

    /**
     * לוגיקת הרישום המאוחדת
     */
    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        // 1. בדיקות תקינות קלט
        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            tvStatus.setText("נא למלא את כל השדות (שם, מייל וסיסמה)");
            return;
        }

        if (pass.length() < 6) {
            tvStatus.setText("הסיסמה חייבת להיות לפחות 6 תווים");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("יוצר חשבון ושומר פרטים...");
        pd.setCancelable(false);
        pd.show();

        // 2. יצירת משתמש ב-Firebase Auth (מייל וסיסמה)
        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // קבלת ה-UID הייחודי שנוצר ב-Auth
                        String uid = refAuth.getCurrentUser().getUid();

                        // 3. שמירת השם המלא ב-Realtime Database תחת users -> UID
                        saveUserToDatabase(uid, name, email, pd);
                    } else {
                        pd.dismiss();
                        handleError(task.getException());
                    }
                });
    }

    /**
     * שמירת הנתונים ב-Realtime Database
     */
    private void saveUserToDatabase(String uid, String name, String email, ProgressDialog pd) {
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("name", name);
        userValues.put("email", email);
        userValues.put("uid", uid);
        userValues.put("age", 0);      // ערך ברירת מחדל
        userValues.put("address", ""); // ערך ברירת מחדל

        // כתיבה לנתיב users/UID
        FBRef.usersRef.child(uid).setValue(userValues)
                .addOnCompleteListener(task -> {
                    pd.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "נרשמת בהצלחה!", Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        tvStatus.setText("שגיאה בשמירת הנתונים: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleError(Exception exp) {
        if (exp instanceof FirebaseAuthWeakPasswordException) {
            tvStatus.setText("הסיסמה חלשה מדי");
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            tvStatus.setText("האימייל כבר קיים במערכת");
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            tvStatus.setText("פורמט אימייל לא תקין");
        } else {
            tvStatus.setText("שגיאה: " + exp.getMessage());
        }
    }
}