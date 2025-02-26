package com.sagie.myfirstapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class sp extends AppCompatActivity {

    Button btnSave, btnRead;
    EditText etName, etAge;
    TextView tvDisplay;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sp);

        btnSave = findViewById(R.id.submitButton);
        btnRead = findViewById(R.id.readButton);
        etName = findViewById(R.id.editTextString);
        etAge = findViewById(R.id.editTextInt);
        tvDisplay = findViewById(R.id.displayTextView);  // Reference to the TextView for displaying the saved data
        checkBox = findViewById(R.id.boolMusic);

        // Save data when the "Submit" button is clicked
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                String ageStr = etAge.getText().toString();
                int age = -1;

                if (!ageStr.isEmpty()) {
                    age = Integer.parseInt(ageStr);
                }

                // Save data to SharedPreferences
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                SharedPreferences.Editor editor = pref.edit();

                editor.putString("name", name);
                editor.putInt("age", age);

                // Save the checkbox value
                if (checkBox.isChecked()) {
                    editor.putBoolean("music", true);
                } else {
                    editor.putBoolean("music", false);
                }

                // Apply changes to SharedPreferences
                editor.apply();  // Only call apply once after all the changes
            }
        });

        // Read data when the "Read" button is clicked
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Read data from SharedPreferences
                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
                String name = pref.getString("name", "No name found");
                int age = pref.getInt("age", -1);
                boolean music = pref.getBoolean("music", false);  // Read the music preference

                // Display the data
                tvDisplay.setText("Name: " + name + "\nAge: " + (age == -1 ? "No age found" : age) + "\nMusic: " + (music ? "Enabled" : "Disabled"));
            }
        });
    }
}
