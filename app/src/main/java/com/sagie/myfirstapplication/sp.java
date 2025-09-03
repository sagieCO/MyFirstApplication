package com.sagie.myfirstapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class sp extends AppCompatActivity {

    Button btnSave, btnRead, SpToHome,spToGuess;
    EditText etName, etAge;
    TextView tvDisplay;
    CheckBox checkBox;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp);

        btnSave = findViewById(R.id.submitButton);
        btnRead = findViewById(R.id.readButton);
        etName = findViewById(R.id.editTextString);
        etAge = findViewById(R.id.editTextInt);
        tvDisplay = findViewById(R.id.displayTextView);
        checkBox = findViewById(R.id.boolMusic);
        SpToHome = findViewById(R.id.SpToHome);
        spToGuess=findViewById(R.id.spToGuess);
        spToGuess.setEnabled(false);
        spToGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(sp.this,GuessNumber.class);
                startActivity(intent);
                finish();
            }
        });
        // שמירה ב-SharedPreferences
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            int age = etAge.getText().toString().isEmpty() ? -1 : Integer.parseInt(etAge.getText().toString());

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("name", name);
            editor.putInt("age", age);
            editor.putBoolean("music", checkBox.isChecked());
            editor.apply();
            spToGuess.setEnabled(true);

            etName.setEnabled(false);
            etAge.setEnabled(false);
            checkBox.setEnabled(false);
            btnSave.setEnabled(false);
        });

        // קריאה מ-SharedPreferences
        btnRead.setOnClickListener(v -> {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            String name = pref.getString("name", "No name found");
            int age = pref.getInt("age", -1);
            boolean music = pref.getBoolean("music", false);
            tvDisplay.setText("Name: " + name + "\nAge: " + (age == -1 ? "No age found" : age) +
                    "\nMusic: " + (music ? "Enabled" : "Disabled"));
        });

        // חזרה ל-MainActivity
        SpToHome.setOnClickListener(v -> {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            String name = pref.getString("name", "NoName");

            Intent intent = new Intent(sp.this, MainActivity.class);
            intent.putExtra("user_name", name); // שולח את שם המשתמש
            startActivity(intent);
        });
    }
}
