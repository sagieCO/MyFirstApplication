package com.sagie.myfirstapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class sigim extends AppCompatActivity {
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sigim);  // ה-XML של Activity

        // אתחול הכפתור והגדרת מאזין
        btnLogin = findViewById(R.id.btnDialogLogin); // הפנייה נכונה לכפתור
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // קריאה ליצירת הדיאלוג
                createLoginDialog();
            }
        });
    }

    public void createLoginDialog() {
        // יצירת דיאלוג חדש
        Dialog d = new Dialog(this);

        // ניתן לדיאלוג את ה-layout שיצרנו
        //d.setContentView(R.layout.activity_custom);  // כאן אתה משתמש ב-XML של הדיאלוג

        // כותרת הדיאלוג
        d.setTitle("Login");

        // להציג מחוץ לדיאלוג כשהוא פתוח
        d.setCancelable(true);

        // הפניות לאובייקטים של EditText ו-Button מתוך הדיאלוג
        EditText etUserName = d.findViewById(R.id.etUserName);
        EditText etPass = d.findViewById(R.id.etPassword);
        Button btnDialogLogin = d.findViewById(R.id.btnDialogLogin);  // תיקון ההפניה לכפתור

        // הגדרת פעולה לכפתור "Login"
        btnDialogLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUserName.getText().toString();
                String password = etPass.getText().toString();

                // אם התחברות מוצלחת
                if (username.equals("correctUsername") && password.equals("correctPassword")) {
                    // ביצוע פעולה לאחר התחברות מוצלחת
                } else {
                    // הצגת הודעה או טיפול בכשלון
                }
            }
        });

        // הצגת הדיאלוג
        d.show();
    }
}
