package com.sagie.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // Views
    Button linerPage, guessGame, spButton, btnFrame, btnCalender;
    ImageButton btnProfile;
    TextView playerScore, welcomeUser,btnLogin;

    Context context;

    private static final int START_GAME = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        initViews();
        setupListeners();


    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void initViews() {
        linerPage = findViewById(R.id.linerPage);
        guessGame = findViewById(R.id.GuessGame);
        spButton = findViewById(R.id.spButton);
        btnFrame = findViewById(R.id.framePage);
        btnLogin = findViewById(R.id.btnLogin);
        btnCalender = findViewById(R.id.btnCalender);
        playerScore = findViewById(R.id.playerScore);
        welcomeUser = findViewById(R.id.welcomeUser);
        btnProfile=findViewById(R.id.profileIcon);
    }

    private void setupListeners() {
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
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            welcomeUser.setText("Welcome " + currentUser.getEmail());
        } else {
            welcomeUser.setText("Welcome Guest");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_GAME && resultCode == RESULT_OK && data != null) {
            int numGuesses = data.getIntExtra("num_guesses", -1);
            String userName = data.getStringExtra("user_name");
            playerScore.setText(userName + " won in " + numGuesses + " guesses.");
        }
    }
}

