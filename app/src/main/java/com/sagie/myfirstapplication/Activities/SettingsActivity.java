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
import com.google.firebase.firestore.FirebaseFirestore;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private Button btnDeleteAccount, btnEditProfile;
    private FirebaseFirestore firestore;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    // משתנה זמני כדי להחזיק את ה-ImageView של הדיאלוג
    private ShapeableImageView dialogProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_settings);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        firestore = FirebaseFirestore.getInstance();

        initView();
        setupListeners();
        setupImagePicker(); // הגדרת בוחר התמונות
    }

    private void initView() {
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void setupListeners() {
        btnDeleteAccount.setOnClickListener(v -> deleteUserLogic());
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    // הגדרת הלוגיקה שקולטת את התמונה שנבחרה מהגלריה
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

        // טעינת נתונים קיימים (טקסט ותמונה)
        loadDataIntoDialog(user.getUid(), dialogName, dialogAge, dialogAddress, dialogProfileImage);

        // כפתור החלפת תמונה - פותח גלריה
        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // כפתור שמירת טקסט
        btnSave.setOnClickListener(v -> {
            saveTextData(user.getUid(), dialogName, dialogAge, dialogAddress);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void handleImageUpload(Uri fileUri) {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user == null) return;

        try {
            InputStream stream = getContentResolver().openInputStream(fileUri);
            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            // לוגיקת דחיסה (בדיוק כמו ב-ProfileUserActivity)
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

            firestore.collection("imageProfile").document(user.getUid())
                    .set(imageMap)
                    .addOnSuccessListener(aVoid -> {
                        if (dialogProfileImage != null) {
                            dialogProfileImage.setImageBitmap(bitmap);
                        }
                        Toast.makeText(this, "התמונה הועלתה בהצלחה!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "שגיאה בהעלאת תמונה", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDataIntoDialog(String uid, EditText name, EditText age, EditText addr, ShapeableImageView imgView) {
        // טעינה מ-Realtime
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

        // טעינת תמונה מ-Firestore
        firestore.collection("imageProfile").document(uid).get().addOnSuccessListener(doc -> {
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

    private void deleteUserLogic() {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "לא נמצא משתמש מחובר", Toast.LENGTH_SHORT).show();
            return;
        }

        // שמירת ה-UID בנפרד כדי להבטיח גישה לנתונים במסד
        final String userIdToDelete = user.getUid();

        new AlertDialog.Builder(this)
                .setTitle("מחיקת חשבון")
                .setMessage("האם אתה בטוח שברצונך למחוק את החשבון וכל הנתונים המקושרים אליו?")
                .setPositiveButton("מחק לצמיתות", (dialog, which) -> {

                    // 1. מחיקה מה-Realtime Database תחת המבנה: mechinot -> users -> UID
                    // אני משתמש ב-FBRef.usersRef כפי שמוגדר אצלך, ומוודא שהנתיב נכון
                    FBRef.usersRef.child(userIdToDelete).removeValue()
                            .addOnCompleteListener(taskRTDB -> {
                                if (taskRTDB.isSuccessful()) {

                                    // 2. מחיקת תמונת הפרופיל מ-Firestore (אוסף imageProfile)
                                    firestore.collection("imageProfile").document(userIdToDelete).delete()
                                            .addOnCompleteListener(taskFirestore -> {

                                                // 3. מחיקת המשתמש מה-Authentication (השלב האחרון)
                                                user.delete().addOnCompleteListener(taskAuth -> {
                                                    if (taskAuth.isSuccessful()) {
                                                        Toast.makeText(SettingsActivity.this, "החשבון וכל המידע נמחקו בהצלחה", Toast.LENGTH_SHORT).show();
                                                        finish(); // חזרה למסך הקודם או סגירה
                                                    } else {
                                                        // אם ה-Token פג תוקף, Firebase יבקש Re-authentication
                                                        Toast.makeText(SettingsActivity.this, "שגיאה במחיקת החשבון: " + taskAuth.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            });
                                } else {
                                    Toast.makeText(SettingsActivity.this, "שגיאה במחיקת הנתונים מהמסד", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
}