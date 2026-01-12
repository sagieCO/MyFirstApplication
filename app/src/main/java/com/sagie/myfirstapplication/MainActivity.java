package com.sagie.myfirstapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {

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
        //setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        context = this;

        Button btnGoToCalendar = findViewById(R.id.btnGoToCalendar); // רק אחרי setContentLayout
        btnGoToCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MonthlyCalendarActivity.class);
            startActivity(intent);
        });

        Button btnCreateMechina = findViewById(R.id.btnCreateMechina); // רק אחרי setContentLayout
        btnCreateMechina.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateMechinaActivity.class);
            startActivity(intent);
        });


    };
    }


