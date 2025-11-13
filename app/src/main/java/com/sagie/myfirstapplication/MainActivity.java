package com.sagie.myfirstapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    Button linerPage, guessGame, spButton, btnFrame, btnCalender;
    ImageButton btnProfile;
    TextView playerScore, welcomeUser, btnLogin, tv_snv;

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

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);//right to left


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        context = this;
        initViews();
        //setupListeners();

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateHeader(user);
        };

    }


    private void updateHeader(FirebaseUser user) {
        if (nv_side.getHeaderCount() > 0) {
            View headerView = nv_side.getHeaderView(0);
            TextView userNameText = headerView.findViewById(R.id.tvUsername);
            TextView userGreeting = headerView.findViewById(R.id.tvGreeting);

            if (user != null) {
                String uid = user.getUid();

                // 🔹 Fetch user data from Firebase Realtime Database
                usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User currentUser = snapshot.getValue(User.class);
                            if (currentUser != null && currentUser.getName() != null) {
                                userNameText.setText(currentUser.getName());
                                userGreeting.setText("שלום " + currentUser.getName());
                            } else {
                                userNameText.setText("משתמש");
                                userGreeting.setText("שלום משתמש");
                            }
                        } else {
                            userNameText.setText("משתמש");
                            userGreeting.setText("שלום משתמש");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        userNameText.setText("משתמש");
                        userGreeting.setText("שלום משתמש");
                    }
                });

            } else {
                // 🔹 אין משתמש מחובר
                userNameText.setText("אורח");
                userGreeting.setText("שלום אורח");
            }
        }
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



    private void initViews() {
    /*linerPage = findViewById(R.id.linerPage);
    guessGame = findViewById(R.id.GuessGame);
    spButton = findViewById(R.id.spButton);
    btnFrame = findViewById(R.id.framePage);
    btnLogin = findViewById(R.id.btnLogin);
    btnCalender = findViewById(R.id.btnCalender);
    playerScore = findViewById(R.id.playerScore);
    welcomeUser = findViewById(R.id.welcomeUser);*/
        btnProfile = findViewById(R.id.profileIcon);

        drawerLayout = findViewById(R.id.main);
        nv_side = findViewById(R.id.nv_side);
        tv_snv = findViewById(R.id.tv_snv);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // 🔹 כאן אנחנו מתחברים ל-header ומעדכנים את שם המשתמש
        if (nv_side.getHeaderCount() > 0) {
            View headerView = nv_side.getHeaderView(0);
            TextView userNameText = headerView.findViewById(R.id.tvUsername);
            TextView userGreeting = headerView.findViewById(R.id.tvGreeting);

            // כאן תוכל להחליף ב-Firebase או SharedPreferences לפי הצורך
            String userName = getIntent().getStringExtra("user_name"); // או קבלת שם ממשתמש מחובר
            if (userName != null && !userName.isEmpty()) {
                userNameText.setText(userName);
                userGreeting.setText("שלום " + userName);
            } else {
                userNameText.setText("אורח");
                userGreeting.setText("שלום אורח");
            }
        }

        // 🔹 Navigation item selection
        nv_side.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            tv_snv.setText(item.getTitle());

            if (id == R.id.btnLogin) {
                startActivity(new Intent(MainActivity.this, Login.class));
            } else if (id == R.id.brnCalender) {
                startActivity(new Intent(MainActivity.this, calender.class));
            }else if (id == R.id.btnRegister) {
                startActivity(new Intent(MainActivity.this, Register.class));
            } else if (id == R.id.btnProfile) {
                startActivity(new Intent(MainActivity.this, profileUser.class));
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
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*private void setupListeners() {
        btnCalender.setOnClickListener(v ->
                startActivity(new Intent(this, calender.class)));
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, profileUser.class)));
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, Login.class)));

        btnFrame.setOnClickListener(v ->
                startActivity(new Intent(this, FrameActivity.class)));

        linerPage.setOnClickListener(v ->
                startActivity(new Intent(this, LinearActivity.class)));

        spButton.setOnClickListener(v ->
                startActivity(new Intent(this, sp.class)));

        guessGame.setOnClickListener(v -> {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            boolean musicAllowed = pref.getBoolean("music", false);

            if (musicAllowed) {
                Intent intent = new Intent(MainActivity.this, GuessNumber.class);
                String userName = getIntent().getStringExtra("user_name");
                if (userName != null) {
                    intent.putExtra("user_name", userName);
                }
                startActivityForResult(intent, START_GAME);
            } else {
                Toast.makeText(this, "You must approve music first!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/




}
