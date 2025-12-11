package com.sagie.myfirstapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.ActionBarDrawerToggle;

public class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        setContentView(R.layout.base_layout);
        setupMenu();
    }


    protected void setupMenu() {

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // ✔️ זה יעבוד עכשיו
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Header
        View headerView = navigationView.getHeaderView(0);
        ImageView navProfileImage = headerView.findViewById(R.id.navHeaderProfileImage);
        TextView navName = headerView.findViewById(R.id.tvGreeting);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getEmail();   // ✔️ כאן תיקון
            }

            navName.setText(name);

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.account_icom)
                        .into(navProfileImage);
            } else {
                navProfileImage.setImageResource(R.drawable.account_icom);
            }
        } else {
            navName.setText("אין משתמש מחובר");
            navProfileImage.setImageResource(R.drawable.account_icom);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.btnHome) {
                startActivity(new Intent(this, MainActivity.class));
            }
            else if (id == R.id.btnLogin) {
                startActivity(new Intent(this, LoginActivity.class));
            } else if (id == R.id.brnCalender) {
                startActivity(new Intent(this, CalenderActivity.class));
            } else if (id == R.id.btnRegister) {
                startActivity(new Intent(this, RegisterActivity.class));
            } else if (id == R.id.btnProfile) {
                startActivity(new Intent(this, ProfileUserActivity.class));
            } else if (id == R.id.btnSetting) {
                Toast.makeText(this, "settings page", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.btnLogout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "התנתקת בהצלחה", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    protected void setContentLayout(int layoutResID) {
        getLayoutInflater().inflate(layoutResID, findViewById(R.id.main_content));
    }
}
