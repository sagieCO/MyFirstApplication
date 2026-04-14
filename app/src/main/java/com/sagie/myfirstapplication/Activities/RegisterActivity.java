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

import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private TextView tvStatus;
    private Button btnRegister;
    private EditText etEmail, etPassword, etName, etBirth, etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_register);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initView();

        btnRegister.setOnClickListener(v -> performRegistration());
    }

    private void initView() {
        tvStatus = findViewById(R.id.tvStatus);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etBirth = findViewById(R.id.etBirth); // שדה תאריך הלידה
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnCreateAccount);
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String birthDate = etBirth.getText().toString().trim(); // קבלת תאריך הלידה
        String address = etAddress.getText().toString().trim();

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

        refAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = refAuth.getCurrentUser().getUid();
                        // שמירה עם שדה birthDate החדש
                        saveUserToDatabase(uid, name, email, birthDate, address, pd);
                    } else {
                        pd.dismiss();
                        handleError(task.getException());
                    }
                });
    }

    private void saveUserToDatabase(String uid, String name, String email, String birthDate, String address, ProgressDialog pd) {
        Map<String, Object> userValues = new HashMap<>();
        userValues.put("name", name);
        userValues.put("email", email);
        userValues.put("uid", uid);
        userValues.put("birthDate", birthDate.isEmpty() ? "" : birthDate); // שינוי המפתח ל-birthDate
        userValues.put("address", address.isEmpty() ? "" : address);

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