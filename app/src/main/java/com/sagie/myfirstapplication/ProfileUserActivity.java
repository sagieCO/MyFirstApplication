package com.sagie.myfirstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileUserActivity extends AppCompatActivity {

    ImageButton btnHome;
    EditText nameText, ageText, emailText, addressText;
    ImageView editName, editAge, editEmail, editAddress;
    Button saveInfo;

    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initView();
        loadUserData();    // טעינת פרטי המשתמש מהמסד
        setupListeners();
    }

    private void initView() {
        btnHome = findViewById(R.id.homeIcon);

        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);
        emailText = findViewById(R.id.emailText);
        addressText = findViewById(R.id.addressText);

        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        editEmail = findViewById(R.id.editEmail);
        editAddress = findViewById(R.id.editAddress);

        saveInfo = findViewById(R.id.saveInfo);

        // נעילה ראשונית של כל השדות (משתמש לא יכול לשנות)
        lockAllFields();
    }

    private void setupListeners() {
        // חזרה לעמוד הראשי
        btnHome.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));

        // לחיצה על אייקון העריכה פותחת את השדה לעריכה
        editName.setOnClickListener(v -> setEditable(nameText, true));
        editAge.setOnClickListener(v -> setEditable(ageText, true));
        editEmail.setOnClickListener(v -> setEditable(emailText, true));
        editAddress.setOnClickListener(v -> setEditable(addressText, true));

        // שמירה ושחרור נעילה
        saveInfo.setOnClickListener(v -> {
            saveUserData();
            lockAllFields();  // נעילה אחרי שמירה
        });
    }

    // נעילה של כל השדות
    private void lockAllFields() {
        setEditable(nameText, false);
        setEditable(ageText, false);
        setEditable(emailText, false);
        setEditable(addressText, false);
    }

    // הפיכת EditText לערוך או נעול
    private void setEditable(EditText editText, boolean editable) {
        editText.setEnabled(editable);
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        if (!editable) editText.clearFocus();
    }

    // טעינת פרטי המשתמש מה־Firebase
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            nameText.setText(user.getName());
                            ageText.setText(user.getAge());
                            emailText.setText(user.getEmail());
                            addressText.setText(user.getAddress());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // ניתן להוסיף טיפול בשגיאות כאן
                }
            });
        }
    }

    // שמירת הנתונים למסד
    private void saveUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            User updatedUser = new User();
            updatedUser.setName(nameText.getText().toString());
            updatedUser.setAge(ageText.getText().toString());
            updatedUser.setEmail(emailText.getText().toString());
            updatedUser.setAddress(addressText.getText().toString());

            usersRef.child(uid).setValue(updatedUser);
        }
    }
}
