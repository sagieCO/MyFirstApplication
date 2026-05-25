package com.sagie.myfirstapplication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;
import com.sagie.myfirstapplication.FBRef;
import com.sagie.myfirstapplication.R;
import com.sagie.myfirstapplication.models.NetworkChangeReceiver;

public class OpenPageActivity extends AppCompatActivity {

    Button btnLogin,btnRegister,btnGuest;

    // הגדרת המשתנה של הרסיבר שלנו
    private NetworkChangeReceiver networkReceiver;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_open_page);
        checkUserLoginStatus();

        requestPermissionsIfNeeded();

        initView();

        // אתחול הרסיבר
        networkReceiver = new NetworkChangeReceiver();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // רישום הרסיבר להאזנה לשינויי רשת ברגע שהמסך עולה
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    private void checkUserLoginStatus() {
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(OpenPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }
    private void initView(){
        btnLogin=findViewById(R.id.btnLogin);
        btnRegister=findViewById(R.id.btnRegister);
        btnGuest=findViewById(R.id.btnGuest);
        btnGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(OpenPageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenPageActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OpenPageActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // חובה לבטל את הרישום כשהמסך נסגר כדי למנוע זליגת זיכרון (Memory Leak)
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }
}