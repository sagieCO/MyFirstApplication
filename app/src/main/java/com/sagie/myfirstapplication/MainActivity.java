package com.sagie.myfirstapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final int START_GAME = 222, Accept_game = 111;

    // Views
    Button b1, b2, linerPage, guessGame, spButton, btnFarme,RegisterPage;
    TextView output, playerScore, welomeUser;
    Switch s, musicBtn;
    SeekBar sb;
    ImageView image1, image2;
    ConstraintLayout mainLayout;

    // Others
    Context context;
    Boolean isPlaying;

    // Battery receiver
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            output.setText("Battery Level: " + level + "%");
        }
    };

    // -------- Lifecycle Methods --------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        initViews();

        guessGame.setEnabled(false); // נעול כברירת מחדל

        // רישום ה-BroadcastReceiver לעדכוני סוללה
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryIntentFilter);

        // שמירת תלמידים לדוגמה
        Student student1 = new Student(67, 3, "Sagie Cohen", "1");
        student1.saveToFirebase();
        Student student2 = new Student(80, 4, "Ori sivlem", "2");
        student2.saveToFirebase();
        Student student3 = new Student(99, 1, "Sagie Cohen", "3");
        student3.saveToFirebase();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FBRef.refAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMusicButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }

    // -------- Menu Methods --------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.loginPage) {
            Toast.makeText(this, "You selected login", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.registerPage) {
            Toast.makeText(this, "You selected register", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.settingPage) {
            Toast.makeText(this, "You selected setting", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.mainPage) {
            Toast.makeText(this, "You selected main", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.sigimItem) {
            Intent intent = new Intent(this, sigim.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // -------- Init Views & Listeners --------

    private void initViews() {
        // חיבור ל־XML
        b1 = findViewById(R.id.btn1);
        b2 = findViewById(R.id.btn2);
        output = findViewById(R.id.output);
        musicBtn = findViewById(R.id.musicBtn);
        playerScore = findViewById(R.id.playerScore);
        btnFarme = findViewById(R.id.framePage);
        mainLayout = findViewById(R.id.mainLayout);
        s = findViewById(R.id.switch1);
        sb = findViewById(R.id.sb);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        linerPage = findViewById(R.id.linerPage);
        guessGame = findViewById(R.id.GuessGame);
        spButton = findViewById(R.id.spButton);
        welomeUser = findViewById(R.id.welomeUser);
    RegisterPage=findViewById(R.id.RegisterPage);
    RegisterPage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        }
    });
        // מאזינים
        s.setOnCheckedChangeListener(this);

        b1.setOnClickListener(v -> {
            output.setText("cohen");
            output.setTextColor(0xFF0000FF);
            Log.d("sagie", "Button 1");
        });

        b2.setOnClickListener(v -> {
            output.setText("sagie");
            output.setTextColor(0xFFFF3B4B);
            Log.d("sagie", "Button 2");
        });

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float alpha = (float) i / 100;
                image1.setAlpha(alpha);
                float beta = 1 - (float) i / 100;
                image2.setAlpha(beta);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnFarme.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FrameActivity.class);
            startActivity(intent);
            finish();
        });

        linerPage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LinearActivity.class);
            startActivity(intent);
            finish();
        });

        Intent intentFromSP = getIntent();
        String userName = intentFromSP.getStringExtra("user_name");

        guessGame.setOnClickListener(v -> {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            boolean musicAllowed = pref.getBoolean("music", false);

            if (musicAllowed) {
                Intent intent = new Intent(MainActivity.this, GuessNumber.class);
                if (userName != null) {
                    intent.putExtra("user_name", userName);
                }
                startActivityForResult(intent, START_GAME);
            } else {
                Toast.makeText(MainActivity.this, "You must approve music first!", Toast.LENGTH_SHORT).show();
            }
        });

        spButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, sp.class);
            startActivity(intent);
        });

        musicBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent intent = new Intent(MainActivity.this, MusicService.class);
            if (isChecked) {
                intent.putExtra("action", "start");
            } else {
                intent.putExtra("action", "stop");
            }
            startService(intent);
        });
    }

    // -------- Logic Methods --------

    private void updateUI(FirebaseUser currentUser) {
        Toast.makeText(context, "Hello !", Toast.LENGTH_LONG).show();
    }

    private void updateMusicButtonState() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        boolean musicAllowed = pref.getBoolean("music", false);
        guessGame.setEnabled(musicAllowed);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            mainLayout.setBackgroundColor(0xFF6E7191);
        } else {
            mainLayout.setBackgroundColor(0xFF5E94FD);
        }
    }

    // -------- Activity Result --------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            int numGuesses = data.getIntExtra("num_guesses", -1);
            String userName = data.getStringExtra("user_name");

            Toast.makeText(this, "Game finished! Number of guesses: " + numGuesses + " , user: " + userName, Toast.LENGTH_SHORT).show();
            playerScore.setText(userName + " won in " + numGuesses + " guesses.");
        } else {
            Toast.makeText(this, "Game was canceled or didn't finish successfully.", Toast.LENGTH_SHORT).show();
        }
    }




}


