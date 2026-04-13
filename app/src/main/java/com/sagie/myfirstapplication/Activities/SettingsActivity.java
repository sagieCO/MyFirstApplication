package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private Button btnEditProfile;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher; // לאנצ'ר חדש למצלמה

    private ShapeableImageView dialogProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_settings);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initView();
        setupListeners();
        setupLaunchers();
    }

    private void initView() {
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void setupLaunchers() {
        // לאנצ'ר לגלריה
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        handleImageUpload(fileUri, null); // שולחים URI
                    }
                }
        );

        // לאנצ'ר למצלמה
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        handleImageUpload(null, photo); // שולחים Bitmap
                    }
                }
        );
    }

    private void showEditProfileDialog() {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText dialogName = dialogView.findViewById(R.id.nameText);
        EditText dialogAge = dialogView.findViewById(R.id.ageText);
        EditText dialogAddress = dialogView.findViewById(R.id.addressText);
        Button btnSave = dialogView.findViewById(R.id.saveInfo);
        Button btnChangePhoto = dialogView.findViewById(R.id.btnChangePhoto);
        dialogProfileImage = dialogView.findViewById(R.id.profileImage);

        AlertDialog alertDialog = builder.create();
        loadDataIntoDialog(user.getUid(), dialogName, dialogAge, dialogAddress, dialogProfileImage);

        // יצירת תפריט בחירה: מצלמה או גלריה
        btnChangePhoto.setOnClickListener(v -> {
            String[] options = {"מצלמה", "גלריה"};
            new AlertDialog.Builder(this)
                    .setTitle("בחר מקור תמונה")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) { // מצלמה
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraLauncher.launch(takePictureIntent);
                        } else { // גלריה
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            imagePickerLauncher.launch(intent);
                        }
                    }).show();
        });

        btnSave.setOnClickListener(v -> {
            saveTextData(user.getUid(), dialogName, dialogAge, dialogAddress);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    // פונקציית העלאה מאוחדת
    private void handleImageUpload(Uri fileUri, Bitmap cameraBitmap) {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user == null) return;

        try {
            Bitmap bitmap;
            if (cameraBitmap != null) {
                bitmap = cameraBitmap; // אם באנו מהמצלמה
            } else {
                InputStream stream = getContentResolver().openInputStream(fileUri);
                bitmap = BitmapFactory.decodeStream(stream); // אם באנו מהגלריה
            }

            // דחיסה (הלוגיקה הקיימת שלך)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            int quality = 100;
            while (imageBytes.length > 1048500 && quality > 10) {
                quality -= 5;
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                imageBytes = baos.toByteArray();
            }

            String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);

            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("email", user.getEmail());
            imageMap.put("imageData", base64Image);

            FBRef.refImages.document(user.getUid())
                    .set(imageMap)
                    .addOnSuccessListener(aVoid -> {
                        if (dialogProfileImage != null) {
                            dialogProfileImage.setImageBitmap(bitmap);
                        }
                        Toast.makeText(this, "התמונה עודכנה בהצלחה!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בהעלאת תמונה", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // שאר הפונקציות (loadDataIntoDialog, saveTextData, deleteUserLogic) נשארות כפי שהן
    private void loadDataIntoDialog(String uid, EditText name, EditText age, EditText addr, ShapeableImageView imgView) {
        FBRef.usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    name.setText(snapshot.child("name").getValue(String.class));
                    addr.setText(snapshot.child("address").getValue(String.class));
                    Object ageVal = snapshot.child("age").getValue();
                    if (ageVal != null) age.setText(String.valueOf(ageVal));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        FBRef.refImages.document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String base64 = doc.getString("imageData");
                if (base64 != null) {
                    byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    imgView.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void saveTextData(String uid, EditText name, EditText age, EditText addr) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name.getText().toString().trim());
        updates.put("address", addr.getText().toString().trim());
        try {
            updates.put("age", Long.parseLong(age.getText().toString().trim()));
        } catch (Exception e) {
            updates.put("age", 0);
        }

        FBRef.usersRef.child(uid).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "הפרטים עודכנו", Toast.LENGTH_SHORT).show();
            }
        });
    }
}