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
        setContentView(R.layout.base_layout);
        setupMenu();
        setContentLayout(R.layout.activity_main);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        context = this;

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupListeners();

        // Auth listener
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateHeader(user);
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);

        // ⭐ בודק תזכורת פרטים אישיים
        checkUserDetailsReminder();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    // -----------------------------------------------------------------------
    // ⭐ מערכת תזכורת למילוי פרטים – פעם ב־3 כניסות
    // -----------------------------------------------------------------------

    private void checkUserDetailsReminder() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // מגדיל מונה כניסות
        PrefsHelper.incrementLoginCount(this);
        int count = PrefsHelper.getLoginCount(this);

        // אם זה לא כניסה שלישית → לא מזכירים
        if (count % 3 != 0) return;

        // בודק אם יש פרטים אישיים
        usersRef.child(uid).child("hasDetails")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Boolean hasDetails = snapshot.getValue(Boolean.class);

                        // אם אין פרטים → קופץ דיאלוג
                        if (hasDetails == null || !hasDetails) {
                            showDetailsReminderDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // דיאלוג תזכורת
    private void showDetailsReminderDialog() {
        new AlertDialog.Builder(this)
                .setTitle("תזכורת")
                .setMessage("היי! נראה שלא מילאת את הפרטים האישיים שלך.\nזה ייקח פחות מדקה וישפר את החוויה שלך באפליקציה 😊")
                .setPositiveButton("עבור לפרטים", (d, w) -> {
                    startActivity(new Intent(MainActivity.this, ProfileUserActivity.class));
                })
                .setNegativeButton("אחר כך", null)
                .show();
    }

    // -----------------------------------------------------------------------
    // 🔹 אתחול של כל ה-Views ותפריט הצד
    // -----------------------------------------------------------------------

    private void initViews() {
        btnProfile = findViewById(R.id.profileIcon);

        drawerLayout = findViewById(R.id.main);
        nv_side = findViewById(R.id.nv_side);
        tv_snv = findViewById(R.id.tv_snv);

        toggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Greeting Header
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

    // -----------------------------------------------------------------------
    // 🔹 האזנות לניווט
    // -----------------------------------------------------------------------

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

        // back button
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

    // -----------------------------------------------------------------------
    // 🔹 עדכון ברכה בתפריט
    // -----------------------------------------------------------------------

    private void updateHeader(FirebaseUser user) {
        if (nv_side.getHeaderCount() == 0) return;

        View headerView = nv_side.getHeaderView(0);
        TextView userGreeting = headerView.findViewById(R.id.tvGreeting);

        if (user != null) {
            String uid = user.getUid();

            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User currentUser = snapshot.getValue(User.class);

                    if (currentUser != null && currentUser.getName() != null) {
                        // userGreeting.setText("שלום " + currentUser.getName());
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

    // -----------------------------------------------------------------------
    // תפריט למעלה
    // -----------------------------------------------------------------------

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }
}
