package com.sagie.myfirstapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sagie.myfirstapplication.MonthlyCalendarActivity;
import com.sagie.myfirstapplication.R;

public class MainActivity extends BaseActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        context = this;

        Button btnGoToCalendar = findViewById(R.id.btnGoToCalendar);
        Button btnCreateMechina = findViewById(R.id.btnCreateMechina);

        // עיצוב ראשוני של הכפתור בהתאם למצב החיבור
        updateButtonUI(btnCreateMechina);

        btnGoToCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MonthlyCalendarActivity.class);
            startActivity(intent);
        });

        btnCreateMechina.setOnClickListener(v -> {
            // בדיקה האם המשתמש מחובר ברגע הלחיצה
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                // משתמש מחובר - עוברים מסך
                Intent intent = new Intent(MainActivity.this, CreateMechinaActivity.class);
                startActivity(intent);
            } else {
                // משתמש לא מחובר - מציגים הודעה
                Toast.makeText(context, "עליך להתחבר למערכת כדי ליצור מכינה", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * פונקציה אופציונלית לשינוי המראה של הכפתור כדי שייראה "חסום"
     */
    private void updateButtonUI(Button btn) {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            btn.setAlpha(0.5f); // הופך את הכפתור לחצי שקוף
            // אפשר גם לשנות צבע רקע לאפור אם תרצה:
            // btn.setBackgroundColor(Color.GRAY);
        } else {
            btn.setAlpha(1.0f); // כפתור רגיל וברור
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // מעדכנים את הנראות בכל פעם שחוזרים למסך (למקרה שהתחברו בינתיים)
        updateButtonUI(findViewById(R.id.btnCreateMechina));
    }
}


