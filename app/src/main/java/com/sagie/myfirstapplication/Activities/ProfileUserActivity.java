package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sagie.myfirstapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileUserActivity extends BaseActivity {

    // רכיבי UI
    private EditText nameText, ageText, addressText;
    private ImageButton editName, editAge, editAddress;
    private Button saveInfo, btnChangePhoto;
    private ShapeableImageView profileImage;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private FirebaseFirestore firestore;

    // משתנים לעיבוד תמונה (מוגדרים כאן כדי למנוע שגיאת final בלמדא)
    private Bitmap imageBitmap;
    private byte[] imageBytes;
    private int qual = 100;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_profile_user);

        // הגדרת כיווניות לימין לשמאל
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        firestore = FirebaseFirestore.getInstance();

        initView();
        setupImagePicker();
        loadUserData();
        setupListeners();
    }

    private void initView() {
        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);
        addressText = findViewById(R.id.addressText);
        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        editAddress = findViewById(R.id.editAddress);
        saveInfo = findViewById(R.id.saveInfo);
        profileImage = findViewById(R.id.profileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        lockAllFields();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        handleImageUpload(fileUri);
                    }
                }
        );
    }

    private void setupListeners() {
        editName.setOnClickListener(v -> enableEdit(nameText));
        editAge.setOnClickListener(v -> enableEdit(ageText));
        editAddress.setOnClickListener(v -> enableEdit(addressText));

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        saveInfo.setOnClickListener(v -> {
            saveUserData();
            lockAllFields();
            Toast.makeText(this, "הפרטים נשמרו בהצלחה", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleImageUpload(Uri fileUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        try {
            InputStream stream = getContentResolver().openInputStream(fileUri);
            imageBitmap = BitmapFactory.decodeStream(stream);

            // לוגיקת דחיסה (הקוד ששלחת)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = baos.toByteArray();

            qual = 100;
            // וידוא שהקובץ קטן מ-1MB עבור Firestore
            while (imageBytes.length > 1048500 && qual > 10) {
                qual -= 5;
                baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, qual, baos);
                imageBytes = baos.toByteArray();
            }

            // שינוי רזולוציה במידת הצורך
            while (imageBytes.length > 1048500) {
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap,
                        (int) (imageBitmap.getWidth() * 0.9),
                        (int) (imageBitmap.getHeight() * 0.9),
                        true);
                baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, qual, baos);
                imageBytes = baos.toByteArray();
            }

            // הכנת נתונים ל-Firestore (imageData כ-Base64)
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("email", user.getEmail());
            imageMap.put("imageData", android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT));

            // שימוש ב-UID של המשתמש כשם המסמך
            firestore.collection("imageProfile").document(user.getUid())
                    .set(imageMap)
                    .addOnSuccessListener(aVoid -> {
                        // עדכון התמונה במסך מיד לאחר ההעלאה
                        profileImage.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
                        Toast.makeText(ProfileUserActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileUserActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        // טעינה מה-Realtime Database
        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                nameText.setText(snapshot.child("name").getValue(String.class));
                Long age = snapshot.child("age").getValue(Long.class);
                if (age != null) ageText.setText(String.valueOf(age));
                addressText.setText(snapshot.child("address").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // טעינה מה-Firestore
        firestore.collection("imageProfile").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String base64Image = documentSnapshot.getString("imageData");
                if (base64Image != null) {
                    byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profileImage.setImageBitmap(decodedByte);
                }
            }
        });
    }

    private void saveUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nameText.getText().toString());
        try {
            updates.put("age", Long.parseLong(ageText.getText().toString()));
        } catch (NumberFormatException e) {
            updates.put("age", 0);
        }
        updates.put("address", addressText.getText().toString());
        updates.put("hasDetails", true);

        usersRef.child(uid).updateChildren(updates);
    }

    private void enableEdit(EditText editText) {
        lockAllFields();
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void lockAllFields() {
        lockField(nameText);
        lockField(ageText);
        lockField(addressText);
    }

    private void lockField(EditText editText) {
        editText.setEnabled(false);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setCursorVisible(false);
        editText.clearFocus();
    }
}