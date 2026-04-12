package com.sagie.myfirstapplication.Activities;

import static com.sagie.myfirstapplication.FBRef.refAuth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.sagie.myfirstapplication.R;
import com.sagie.myfirstapplication.models.User;

public class RegisterActivity extends BaseActivity {

    private TextView tvStatus;
    private Button btnCreateAccount, btnSaveExtra, btnSkip;
    private EditText etEmail, etPassword, etName, etAge, etAddress;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הגדרת ה-Layout דרך ה-BaseActivity כפי שעשינו ב-LoginActivity
        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_register);

        // הגדרת כיוון טקסט לימין (RTL)
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // אתחול רכיבי ה-UI
        initView();

        // הגדרת מאזינים לכפתורים (Listeners)
        setupClickListeners();
    }

    /**
     * קישור משתני ה-Java לרכיבי ה-XML ואתחול מצב ראשוני
     */
    private void initView() {
        tvStatus = findViewById(R.id.tvStatus);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnSaveExtra = findViewById(R.id.btnSaveExtra);
        btnSkip = findViewById(R.id.btnSkip);

        // כיבוי שדות הפרטים האישיים עד ליצירת המשתמש
        setPersonalFieldsEnabled(false);
    }

    /**
     * הגדרת כל הלוגיקה של לחיצות הכפתורים
     */
    private void setupClickListeners() {

        // כפתור יצירת חשבון (שלב 1)
        btnCreateAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                tvStatus.setText("אנא מלא אימייל וסיסמה");
                return;
            }

            if (pass.length() < 6) {
                tvStatus.setText("הסיסמה חייבת להכיל לפחות 6 תווים");
                return;
            }

            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("יוצר חשבון...");
            pd.show();

            refAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            tvStatus.setText("החשבון נוצר! ניתן למלא פרטים או לדלג");
                            setPersonalFieldsEnabled(true);
                        } else {
                            handleError(task.getException());
                        }
                    });
        });

        // כפתור שמירת פרטים אישיים (שלב 2 - אופציונלי)
        btnSaveExtra.setOnClickListener(v -> {
            FirebaseUser user = refAuth.getCurrentUser();
            if (user == null) {
                tvStatus.setText("שגיאה: אין משתמש מחובר");
                return;
            }

            String name = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if (name.isEmpty() && ageStr.isEmpty() && address.isEmpty()) {
                tvStatus.setText("נא למלא לפחות שדה אחד לשמירה");
                return;
            }

            int age = 0;
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (Exception e) {
                    tvStatus.setText("גיל לא תקין");
                    return;
                }
            }

            // שמירה ל-Firebase Database
            User newUser = new User(name, age, address, user.getUid());
            newUser.saveToFirebase();

            tvStatus.setText("הפרטים נשמרו! נכנס למערכת...");

            // מעבר אוטומטי למסך הראשי
            navigateToMain();
        });

        // כפתור דילוג (Remind Me Later)
        btnSkip.setOnClickListener(v -> navigateToMain());
    }

    /**
     * פונקציה למעבר למסך הראשי וסגירת מסך ההרשמה
     */
    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * הפעלה או כיבוי של שדות הפרטים האופציונליים
     */
    private void setPersonalFieldsEnabled(boolean enabled) {
        etName.setEnabled(enabled);
        etAge.setEnabled(enabled);
        etAddress.setEnabled(enabled);
        btnSaveExtra.setEnabled(enabled);
    }

    /**
     * טיפול בשגיאות נפוצות מ-Firebase Auth
     */
    private void handleError(Exception exp) {
        if (exp instanceof FirebaseAuthWeakPasswordException) {
            tvStatus.setText("הסיסמה חלשה מדי");
        } else if (exp instanceof FirebaseAuthUserCollisionException) {
            tvStatus.setText("משתמש כבר קיים במערכת");
        } else if (exp instanceof FirebaseAuthInvalidCredentialsException) {
            tvStatus.setText("פורמט אימייל לא תקין");
        } else {
            tvStatus.setText("שגיאה: " + exp.getMessage());
        }
    }
}