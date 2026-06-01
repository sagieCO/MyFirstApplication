package com.sagie.myfirstapplication.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

        if (requiresAuthentication()) {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "עליך להתחבר כדי לגשת לדף זה", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, OpenPageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        }

        setContentView(R.layout.base_layout);
        setupMenu();
    }

    public void startActivityProtected(Intent intent) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "פעולה זו דורשת התחברות", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, OpenPageActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
        } else {
            startActivity(intent);
        }
    }

    protected boolean requiresAuthentication() {
        return false;
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

        View headerView = navigationView.getHeaderView(0);
        navProfileImage = headerView.findViewById(R.id.navHeaderProfileImage);
        navName = headerView.findViewById(R.id.tvGreeting);

        loadHeaderData();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.btnHome) {
                    intent = new Intent(BaseActivity.this, MainActivity.class);
                } else if (id == R.id.btnLogin) {
                    intent = new Intent(BaseActivity.this, LoginActivity.class);
                } else if (id == R.id.btnRegister) {
                    intent = new Intent(BaseActivity.this, RegisterActivity.class);
                } else if (id == R.id.btnFullMap) {
                    intent = new Intent(BaseActivity.this, FullMapActivity.class);
                } else if (id == R.id.btnCreateEvent) {
                    intent = new Intent(BaseActivity.this, CreateMechinaActivity.class);
                } else if (id == R.id.btnCalender) {
                    intent = new Intent(BaseActivity.this, MonthlyCalendarActivity.class);
                } else if (id == R.id.btnSetting) {
                    intent = new Intent(BaseActivity.this, SettingsActivity.class);
                } else if (id == R.id.btnLogout) {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(BaseActivity.this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }

                if (intent != null) {
                    if (id == R.id.btnHome || id == R.id.btnFullMap || id == R.id.btnLogin || id == R.id.btnRegister) {
                        startActivity(intent);
                    } else {
                        startActivityProtected(intent);
                    }
                }

                drawerLayout.closeDrawers();
                return true;
            }
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

        navigationView.post(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference("users").child(uid).child("name")
                        .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DataSnapshot snapshot = task.getResult();
                                    if (snapshot != null && snapshot.exists()) {
                                        String name = snapshot.getValue(String.class);
                                        if (name != null && !name.isEmpty()) {
                                            navName.setText("שלום, " + name);
                                        }
                                    }
                                }
                            }
                        });

                FirebaseFirestore.getInstance().collection("imageProfile").document(uid)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    String base64Image = documentSnapshot.getString("imageData");
                                    if (base64Image != null && !base64Image.isEmpty()) {
                                        try {
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
                            }
                        });
            }
        });
    }

    protected void setContentLayout(int layoutResID) {
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.main_content));
    }
}