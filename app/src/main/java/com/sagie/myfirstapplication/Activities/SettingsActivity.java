package com.sagie.myfirstapplication.Activities;

import android.content.DialogInterface;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends BaseActivity {

    private EditText etName, etBirthDate, etAddress;
    private ShapeableImageView profileImage;
    private Button btnChangePhoto, btnSave, btnDeleteAccount;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupMenu();
        setContentLayout(R.layout.activity_settings);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        initView();
        setupLaunchers();
        loadUserData();
    }

    @Override
    protected boolean requiresAuthentication() {
        return true;
    }

    private void initView() {
        etName = findViewById(R.id.nameText);
        etBirthDate = findViewById(R.id.birthDateText);
        etAddress = findViewById(R.id.addressText);
        profileImage = findViewById(R.id.profileImage);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.saveInfo);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private void setupLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            handleImageUpload(result.getData().getData(), null);
                        }
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            if (extras != null) {
                                Bitmap photo = (Bitmap) extras.get("data");
                                handleImageUpload(null, photo);
                            }
                        }
                    }
                }
        );
    }

    private void loadUserData() {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        FBRef.usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etAddress.setText(snapshot.child("address").getValue(String.class));
                    String birthDate = snapshot.child("birthDate").getValue(String.class);
                    if (birthDate != null) etBirthDate.setText(birthDate);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        FBRef.refImages.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (doc.exists()) {
                    String base64 = doc.getString("imageData");
                    if (base64 != null) {
                        byte[] decoded = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        profileImage.setImageBitmap(bitmap);
                    }
                }
            }
        });
    }

    private void saveUserData() {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<String, Object>();
        updates.put("name", etName.getText().toString().trim());
        updates.put("address", etAddress.getText().toString().trim());
        updates.put("birthDate", etBirthDate.getText().toString().trim());

        FBRef.usersRef.child(user.getUid()).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "הפרופיל עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "שגיאה בעדכון הנתונים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showImageSourceDialog() {
        final String[] options = {"מצלמה", "גלריה"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר מקור תמונה");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(takePictureIntent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    imagePickerLauncher.launch(intent);
                }
            }
        });
        builder.show();
    }

    private void handleImageUpload(Uri fileUri, final Bitmap cameraBitmap) {
        FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user == null) return;

        try {
            final Bitmap bitmap;
            if (cameraBitmap != null) {
                bitmap = cameraBitmap;
            } else {
                InputStream stream = getContentResolver().openInputStream(fileUri);
                bitmap = BitmapFactory.decodeStream(stream);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();

            String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);

            Map<String, Object> imageMap = new HashMap<String, Object>();
            imageMap.put("imageData", base64Image);

            FBRef.refImages.document(user.getUid())
                    .set(imageMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            profileImage.setImageBitmap(bitmap);
                            Toast.makeText(SettingsActivity.this, "התמונה עודכנה", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("מחיקת חשבון");
        builder.setMessage("האם אתה בטוח שברצונך למחוק את החשבון לצמיתות? פעולה זו אינה ניתנת לביטול.");
        builder.setPositiveButton("מחק", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserAccount();
            }
        });
        builder.setNegativeButton("ביטול", null);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    private void deleteUserAccount() {
        final FirebaseUser user = FBRef.refAuth.getCurrentUser();
        if (user != null) {
            final String uid = user.getUid();

            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        FBRef.usersRef.child(uid).removeValue();
                        FBRef.refImages.document(uid).delete();

                        Toast.makeText(SettingsActivity.this, "החשבון נמחק בהצלחה", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SettingsActivity.this, OpenPageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, "שגיאה: עליך להתחבר מחדש כדי למצע פעולה זו", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}