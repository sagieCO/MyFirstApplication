package com.sagie.myfirstapplication.Activities;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sagie.myfirstapplication.R;
import com.sagie.myfirstapplication.User;

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

        tvStatus = findViewById(R.id.tvStatus);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnSaveExtra = findViewById(R.id.btnSaveExtra);
        btnSkip = findViewById(R.id.btnSkip);

        setPersonalFieldsEnabled(false);

        // צור חשבון בלבד
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
                            FirebaseUser user = refAuth.getCurrentUser();

                            if (user != null) {
                                DatabaseReference ref =
                                        FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(user.getUid());
                                ref.child("hasDetails").setValue(false);
                            }

                            tvStatus.setText("החשבון נוצר! מילוי פרטים הוא אופציונלי");
                            setPersonalFieldsEnabled(true);
                        } else {
                            handleError(task.getException());
                        }
                    });
        });

        // שמירת פרטים אישיים (אופציונלי)
        btnSaveExtra.setOnClickListener(v -> {
            FirebaseUser user = refAuth.getCurrentUser();
            if (user == null) {
                tvStatus.setText("שגיאה: אין משתמש מחובר");
                return;
            }

            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            int age = 0;
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (Exception e) {
                    tvStatus.setText("גיל לא תקין");
                    return;
                }
            }

            User newUser = new User(name, age, address, user.getUid());
            newUser.saveToFirebase();

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("hasDetails")
                    .setValue(true);

            tvStatus.setText("הפרטים נשמרו בהצלחה");
        });

        // Remind Me Later
        btnSkip.setOnClickListener(v ->
                tvStatus.setText("ניתן להשלים פרטים אישיים בהמשך")
        );
    }

    private void setPersonalFieldsEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etAge.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        btnSaveExtra.setEnabled(enabled);
    }

    private void handleError(Exception exp) {
        if (exp instanceof FirebaseAuthWeakPasswordException) {
            tvStatus.setText("הסיסמה חלשה מדי");
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            tvStatus.setText("משתמש כבר קיים");
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            tvStatus.setText("אימייל לא תקין");
        }
         else {
            tvStatus.setText("שגיאה: " + exp.getMessage());
        }
    }
}
