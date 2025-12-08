package com.sagie.myfirstapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

public class CalenderActivity extends BaseActivity {

    private CalendarView calendarView;
    private EditText editJournal;
    private Button btnSave;
    private String selectedDate;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calender);
        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_calender);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);


        calendarView = findViewById(R.id.calendarView);
        editJournal = findViewById(R.id.editJournal);
        btnSave = findViewById(R.id.btnSave);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            editJournal.setEnabled(false);
            btnSave.setEnabled(false);
            Toast.makeText(this, "אנא התחבר כדי להשתמש ביומן", Toast.LENGTH_LONG).show();
            return;
        }

        String uid = currentUser.getUid();
        databaseRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("events");

        selectedDate = getDateFromMillis(calendarView.getDate());
        attachEventListener(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            attachEventListener(selectedDate);
        });

        btnSave.setOnClickListener(v -> {
            String text = editJournal.getText().toString().trim();
            if (!text.isEmpty()) {
                Event event = new Event(text, System.currentTimeMillis());
                databaseRef.child(selectedDate).setValue(event)
                        .addOnSuccessListener(aVoid -> Toast.makeText(CalenderActivity.this, "נשמר בהצלחה", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(CalenderActivity.this, "שגיאה בשמירה", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(CalenderActivity.this, "נא להקליד טקסט לפני שמירה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachEventListener(String date) {
        if (eventListener != null) {
            databaseRef.child(selectedDate).removeEventListener(eventListener);
        }

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Event event = snapshot.getValue(Event.class);
                    editJournal.setText(event != null ? event.text : "");
                } else {
                    editJournal.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalenderActivity.this, "שגיאה בטעינת האירוע", Toast.LENGTH_SHORT).show();
            }
        };

        databaseRef.child(date).addValueEventListener(eventListener);
    }

    private String getDateFromMillis(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            databaseRef.child(selectedDate).removeEventListener(eventListener);
        }
    }
}
