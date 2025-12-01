package com.sagie.myfirstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileUserActivity extends AppCompatActivity {

    ImageButton btnHome;
    EditText nameText, ageText, emailText, addressText;
    ImageView editName, editAge, editEmail, editAddress;
    Button saveInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        initView();
        setupListeners();
    }

    private void initView() {
        // כפתור חזרה
        btnHome = findViewById(R.id.homeIcon);

        // שדות
        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);
        emailText = findViewById(R.id.emailText);
        addressText = findViewById(R.id.addressText);

        // כפתורי עריכה
        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        editEmail = findViewById(R.id.editEmail);
        editAddress = findViewById(R.id.editAddress);

        // כפתור שמירה
        saveInfo = findViewById(R.id.saveInfo);

        // השדות בתחילה נעולים לעריכה
        setEditable(nameText, false);
        setEditable(ageText, false);
        setEditable(emailText, false);
        setEditable(addressText, false);
    }

    private void setupListeners() {
        // חזרה לעמוד הראשי
        btnHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        // כפתורי עריכה
        editName.setOnClickListener(v -> setEditable(nameText, true));
        editAge.setOnClickListener(v -> setEditable(ageText, true));
        editEmail.setOnClickListener(v -> setEditable(emailText, true));
        editAddress.setOnClickListener(v -> setEditable(addressText, true));

        // שמירת הערכים
        saveInfo.setOnClickListener(v -> {
            // כאן אפשר לשמור את הערכים ל-Firebase או SharedPreferences
            String name = nameText.getText().toString();
            String age = ageText.getText().toString();
            String email = emailText.getText().toString();
            String address = addressText.getText().toString();

            // אחרי שמירה נעשה נעילה מחדש
            setEditable(nameText, false);
            setEditable(ageText, false);
            setEditable(emailText, false);
            setEditable(addressText, false);
        });
    }

    // פונקציה להפוך EditText לערוך או נעול
    private void setEditable(EditText editText, boolean editable) {
        editText.setEnabled(editable);       // מאפשר או חוסם עריכה
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        if (!editable) {
            editText.clearFocus();           // הסרת הפוקוס כאשר נעול
        }
    }
}
