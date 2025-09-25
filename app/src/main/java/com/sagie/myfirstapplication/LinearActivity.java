package com.sagie.myfirstapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LinearActivity extends AppCompatActivity {

    Button backHome, translateButton, chooseImageButton;
    EditText inputText;
    TextView translatedText;
    Translator translator;
    FirebaseFirestore db;
    Bitmap image; // לדוגמה אם התמונה היא ב- Bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear);

        initViews();
        setupTranslator();
        db = FirebaseFirestore.getInstance(); // יצירת אובייקט של Firestore
    }

    private void initViews() {
        backHome = findViewById(R.id.backHome);
        inputText = findViewById(R.id.inputText);
        translateButton = findViewById(R.id.translateButton);
        translatedText = findViewById(R.id.translatedText);
        chooseImageButton = findViewById(R.id.chooseImageButton);  // כפתור לבחור תמונה

        translateButton.setEnabled(false); // הכפתור ננעל עד שהמודל יורד

        backHome.setOnClickListener(view -> {
            Intent intent = new Intent(LinearActivity.this, MainActivity.class);
            startActivity(intent);
        });

        chooseImageButton.setOnClickListener(v -> {
            // כאן תוכל להוסיף את קוד בחירת התמונה מהמכשיר
            openFileChooser();
        });
    }

    private void setupTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HEBREW)
                .build();

        translator = Translation.getClient(options);

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
                                        uploadImageToFirestore(result); // העלאת תמונה ל-Firestore יחד עם התרגום
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

    private void uploadImageToFirestore(String translatedText) {
        // נניח שהתמונה שלך כבר נבחרה ושמורה במשתנה image כ-Bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos); // קומפרסיה של התמונה
        byte[] bytes = baos.toByteArray(); // קבלת המערך בתים של התמונה

        String documentId = db.collection("images").document().getId(); // יצירת ID ייחודי לתמונה

        // יצירת אובייקט של נתוני התמונה
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("imageName", documentId); // שם התמונה
        imageMap.put("imageData", bytes); // נתוני התמונה
        imageMap.put("translatedText", translatedText); // התרגום של הטקסט

        // העלאת הנתונים ל-Firestore
        db.collection("images")
                .document(documentId)
                .set(imageMap) // לא מחזיר DocumentReference, מחזיר Task<Void>
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(LinearActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LinearActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // פעולה לפתוח את דיאלוג הבחירה של תמונה
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); // הגדרת סוג הקובץ לתמונה בלבד
        startActivityForResult(intent, 1); // התחלת ה-Intent עם קוד הבחירה
    }

    // קבלת תוצאה מה-Intent אחרי שבחרת תמונה
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // כאן תוכל להמיר את ה-URI של התמונה ל-Bitmap
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (translator != null) {
            translator.close();
        }
    }
}
