package com.sagie.myfirstapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    // Views
    ImageButton btnProfile;
    TextView tv_snv;

    Context context;
    DrawerLayout drawerLayout;
    NavigationView nv_side;
    ActionBarDrawerToggle toggle;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener authListener;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        context = this;

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initViews();       // אתחול Views ו־Drawer
        setupListeners();  // חיבור כל ההאזנות לכפתורים ול־Navigation Drawer

        // Auth Listener
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateHeader(user);
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    // 🔹 אתחול Views, Drawer ו־ActionBar
    private void initViews() {
        btnProfile = findViewById(R.id.profileIcon);

        drawerLayout = findViewById(R.id.main);
        nv_side = findViewById(R.id.nv_side);
        tv_snv = findViewById(R.id.tv_snv);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if (nv_side.getHeaderCount() > 0) {
            View headerView = nv_side.getHeaderView(0);
            TextView userGreeting = headerView.findViewById(R.id.tvGreeting);

            String userName = getIntent().getStringExtra("user_name");
            if (userName != null && !userName.isEmpty()) {
                userGreeting.setText("שלום " + userName);
            } else {
                userGreeting.setText("שלום אורח");
            }
        }
    }

    // 🔹 חיבור כל ההאזנות לכפתורים ו־Navigation Items
    private void setupListeners() {
        nv_side.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            tv_snv.setText(item.getTitle());

            if (id == R.id.btnLogin) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else if (id == R.id.brnCalender) {
                startActivity(new Intent(MainActivity.this, CalenderActivity.class));
            } else if (id == R.id.btnRegister) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            } else if (id == R.id.btnProfile) {
                startActivity(new Intent(MainActivity.this, ProfileUserActivity.class));
            } else if (id == R.id.btnSetting) {
                Toast.makeText(context, "settings page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.btnLogout) {
                Toast.makeText(MainActivity.this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isOpen()) {
                    drawerLayout.closeDrawers();
                } else {
                    finishAffinity();
                }
            }
        });

        // כאן ניתן להוסיף גם OnClickListeners נוספים לכפתורים כמו btnProfile וכו'
    }

    // 🔹 עדכון כותרת המשתמש ב־Navigation Header
    private void updateHeader(FirebaseUser user) {
        if (nv_side.getHeaderCount() > 0) {
            View headerView = nv_side.getHeaderView(0);
            TextView userGreeting = headerView.findViewById(R.id.tvGreeting);

            if (user != null) {
                String uid = user.getUid();
                usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User currentUser = snapshot.getValue(User.class);
                            if (currentUser != null && currentUser.getName() != null) {
                                userGreeting.setText("שלום " + currentUser.getName());
                            } else {
                                userGreeting.setText("שלום אורח");
                            }
                        } else {
                            userGreeting.setText("שלום אורח");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        userGreeting.setText("שלום אורח");
                    }
                });
            } else {
                userGreeting.setText("שלום אורח");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
