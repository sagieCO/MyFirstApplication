package com.sagie.myfirstapplication;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileUserActivity extends BaseActivity {

    EditText nameText, ageText, addressText;
    ImageButton editName, editAge, editAddress;
    Button saveInfo;

    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_profile_user);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initView();
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

        lockAllFields();
    }

    private void setupListeners() {
        editName.setOnClickListener(v -> enableEdit(nameText));
        editAge.setOnClickListener(v -> enableEdit(ageText));
        editAddress.setOnClickListener(v -> enableEdit(addressText));

        saveInfo.setOnClickListener(v -> {
            saveUserData();
            lockAllFields();
            Toast.makeText(this, "הפרטים נשמרו בהצלחה", Toast.LENGTH_SHORT).show();
        });
    }

    // ===== טעינת פרטים מה־Firebase =====
    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        usersRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                nameText.setText(snapshot.child("name").getValue(String.class));

                Long age = snapshot.child("age").getValue(Long.class);
                if (age != null) {
                    ageText.setText(String.valueOf(age));
                }

                addressText.setText(snapshot.child("address").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // ===== שמירה =====
    private void saveUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nameText.getText().toString());

        // שמירה כ-LONG אם אפשר
        try {
            updates.put("age", Long.parseLong(ageText.getText().toString()));
        } catch (NumberFormatException e) {
            updates.put("age", 0);
        }

        updates.put("address", addressText.getText().toString());


        updates.put("hasDetails", true);


        usersRef.child(uid).updateChildren(updates);
    }

    // ===== הפיכת EditText לערוך =====
    private void enableEdit(EditText editText) {
        lockAllFields(); // נעילה של שאר השדות
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setCursorVisible(true);
        editText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // ===== נעילה של כל השדות =====
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
