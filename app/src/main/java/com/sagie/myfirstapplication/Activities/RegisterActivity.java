package com.sagie.myfirstapplication.Activities;

import static com.sagie.myfirstapplication.FBRef.refAuth;
import com.sagie.myfirstapplication.models.User;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sagie.myfirstapplication.R;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;


public class RegisterActivity extends BaseActivity {

    private TextView tvStatus;
    private Button btnRegister, btnSaveExtra, btnSkip;
    private EditText etEmail, etPassword, etName, etBirth, etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_register);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initView();
    }

    private void initView() {
        tvStatus = findViewById(R.id.tvStatus);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etBirth = findViewById(R.id.etBirth);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnCreateAccount);
        btnSaveExtra = findViewById(R.id.btnSaveExtra);
        btnSkip = findViewById(R.id.btnSkip);

        etBirth.setEnabled(false);
        etAddress.setEnabled(false);
        btnSaveExtra.setEnabled(false);
        btnSkip.setEnabled(false);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegistration();
            }
        });

        btnSaveExtra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateExtraDetails();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterActivity.this, "תוכל לעדכן פרטים אלו בהמשך פרופיל המשתמש", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String birthDate = etBirth.getText().toString().trim();
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
                        if(refAuth.getCurrentUser() != null) {
                            String uid = refAuth.getCurrentUser().getUid();
                            saveUserToDatabase(uid, name, email, birthDate, address, pd);
                        }
                    } else {
                        pd.dismiss();
                        handleError(task.getException());
                    }
                });
    }

    private void saveUserToDatabase(String uid, String name, String email, String birthDate, String address, ProgressDialog pd) {
        User user = new User(name, email, birthDate, address, uid);

        user.saveToFirebase().addOnCompleteListener(task -> {
            pd.dismiss();

            if (task.isSuccessful()) {
                Toast.makeText(this, "החשבון נוצר בהצלחה! כעת באפשרותך להוסיף פרטים אישיים.", Toast.LENGTH_LONG).show();

                etBirth.setEnabled(true);
                etAddress.setEnabled(true);
                btnSaveExtra.setEnabled(true);
                btnSkip.setEnabled(true);

                etName.setEnabled(false);
                etEmail.setEnabled(false);
                etPassword.setEnabled(false);
                btnRegister.setEnabled(false);

            } else {
                tvStatus.setText("שגיאה בשמירת משתמש");
            }
        });
    }

    private void updateExtraDetails() {
        if (refAuth.getCurrentUser() == null) return;

        String uid = refAuth.getCurrentUser().getUid();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String birthDate = etBirth.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("מעדכן פרטים מורחבים...");
        pd.show();

        User updatedUser = new User(name, email, birthDate, address, uid);
        updatedUser.saveToFirebase().addOnCompleteListener(task -> {
            pd.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                tvStatus.setText("שגיאה בעדכון הפרטים");
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