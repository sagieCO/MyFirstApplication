package com.sagie.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class LinearActivity extends AppCompatActivity {

    Button backHome, translateButton;
    EditText inputText;
    TextView translatedText;
    Translator translator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear);

        initViews();
        setupTranslator();
    }

    private void initViews() {
        backHome = findViewById(R.id.backHome);
        inputText = findViewById(R.id.inputText);

        translateButton = findViewById(R.id.translateButton);
        translatedText = findViewById(R.id.translatedText);

        translateButton.setEnabled(false); // הכפתור ננעל עד שהמודל יורד

        backHome.setOnClickListener(view -> {
            Intent intent = new Intent(LinearActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void setupTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HEBREW)
                .build();

        translator = Translation.getClient(options);

        // ביטול הדרישה ל־Wi-Fi רק לבדיקת תקינות
        DownloadConditions conditions = new DownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    translateButton.setEnabled(true);
                    Toast.makeText(this, "מודל תרגום ירד בהצלחה", Toast.LENGTH_SHORT).show();

                    translateButton.setOnClickListener(v -> {
                        String text = inputText.getText().toString().trim();
                        if (!text.isEmpty()) {
                            Toast.makeText(this, "מתרגם...", Toast.LENGTH_SHORT).show();
                            translator.translate(text)
                                    .addOnSuccessListener(result -> {
                                        translatedText.setText(result);
                                        Log.d("TranslateSuccess", "Result: " + result);
                                    })
                                    .addOnFailureListener(e -> {
                                        translatedText.setText("שגיאה בתרגום");
                                        Log.e("TranslateError", "שגיאה בתרגום", e);
                                    });
                        } else {
                            Toast.makeText(this, "הכנס טקסט לתרגום", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בהורדת מודל תרגום", Toast.LENGTH_SHORT).show();
                    Log.e("ModelDownloadError", "מודל לא ירד", e);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (translator != null) {
            translator.close();
        }
    }
}
