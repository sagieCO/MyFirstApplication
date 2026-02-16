package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sagie.myfirstapplication.R;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected TextView navName;
    protected ImageView navProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.base_layout);
        setupMenu();
    }

    protected void setupMenu() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // גישה ל-Header של התפריט
        View headerView = navigationView.getHeaderView(0);
        navProfileImage = headerView.findViewById(R.id.navHeaderProfileImage);
        navName = headerView.findViewById(R.id.tvGreeting);

        loadHeaderData();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.btnHome) {
                startActivity(new Intent(this, MainActivity.class));
            } else if (id == R.id.btnLogin) {
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.btnRegister) {
                startActivity(new Intent(this, RegisterActivity.class));
            }
            else if(id == R.id.btnFullMap){
                startActivity(new Intent(this, FullMapActivity.class));
            }
            else if (id == R.id.btnSetting) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.btnLogout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
                finish(); // סגירת האקטיביטי הנוכחי
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void loadHeaderData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navName.setText("אין משתמש מחובר");
            navProfileImage.setImageResource(R.drawable.account_icom);
            return;
        }

        String uid = user.getUid();
        navName.setText(user.getEmail());

        // שימוש ב-post כדי להבטיח שה-UI thread פנוי לעדכון
        navigationView.post(() -> {
            // משיכת השם מה-Realtime Database
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("name")
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String name = task.getResult().getValue(String.class);
                            if (name != null && !name.isEmpty()) {
                                navName.setText("שלום, " + name);
                            }
                        }
                    });

            // משיכת התמונה מ-Firestore
            FirebaseFirestore.getInstance().collection("imageProfile").document(uid)
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String base64Image = documentSnapshot.getString("imageData");
                            if (base64Image != null && !base64Image.isEmpty()) {
                                try {
                                    // המרה בטוחה מ-Base64 ל-Bitmap
                                    byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                    if (decodedByte != null) {
                                        navProfileImage.setImageBitmap(decodedByte);
                                    }
                                } catch (Exception e) {
                                    navProfileImage.setImageResource(R.drawable.account_icom);
                                }
                            }
                        }
                    });
        });
    }

    protected void setContentLayout(int layoutResID) {
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.main_content));
    }
}