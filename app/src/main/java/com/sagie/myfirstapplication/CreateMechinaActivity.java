package com.sagie.myfirstapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CreateMechinaActivity extends BaseActivity {

    // הגדרת משתנים לרכיבי המסך
    private EditText etMechinaName, etBranch, etAddress;
    private Button btnPickDate, btnSave;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // שימוש ב-BaseActivity כפי שהגדרת
        setupMenu();
        setContentLayout(R.layout.activity_create_mechina); // וודא שזה שם ה-XML שלך

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        // אתחול רכיבים
        etMechinaName = findViewById(R.id.etMechinaName);
        etBranch = findViewById(R.id.etBranch);
        etAddress = findViewById(R.id.etAddress);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);

        // כפתור לבחירת תאריך
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // כפתור שמירה
        btnSave.setOnClickListener(v -> saveEventToFirebase());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            btnPickDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveEventToFirebase() {
        // 1. בדיקה האם המשתמש בכלל מחובר (חובה כדי למנוע קריסה ושיוך שגוי)
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "עליך להיות מחובר כדי להוסיף אירוע", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. קבלת ה-UID האמיתי של המשתמש המחובר כרגע
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 3. קליטת הנתונים מהשדות
        String name = etMechinaName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || selectedDate.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. הפניה לנתיב המדויק תחת ה-UID המחובר
        DatabaseReference userEventsRef = FirebaseDatabase.getInstance()
                .getReference("users") // וודא שזה u קטנה כמו בתמונה שלך
                .child(uid)
                .child("my_events");

        // 5. יצירת מזהה ייחודי לאירוע (Push ID)
        String eventId = userEventsRef.push().getKey();

        // 6. יצירת האובייקט (שימוש במחלקה שבנינו)
        MechinaEvent newEvent = new MechinaEvent(eventId, name, branch, selectedDate, address, 0.0, 0.0);

        // 7. שמירה ל-Firebase
        if (eventId != null) {
            userEventsRef.child(eventId).setValue(newEvent)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "המיון נשמר בהצלחה!", Toast.LENGTH_SHORT).show();
                        finish(); // חזרה למסך הקודם
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בשמירה: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}