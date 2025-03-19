package com.sagie.myfirstapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final int START_GAME = 222, Accept_game = 111;
    Button b1, b2, linerPage, guessGame, spButton;
    TextView tv1;
    Context context;
    Switch s;
    SeekBar sb;
    ImageView image1, image2;
    ConstraintLayout mainLayout;

    // ה-BroadcastReceiver לקליטת עדכון סוללה
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            tv1.setText("Battery Level: " + level + "%");  // הצגת אחוז הסוללה ב-TextView
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        initviews();

        // רישום ה-BroadcastReceiver לעדכוני סוללה
        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryIntentFilter);
    }

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
        } else if (id == R.id.sigimItem) {  // כאן התווספה ההתייחסות לאייטם
            Intent intent = new Intent(this, sigim.class);  // יצירת Intent למעבר לדף sigim
            startActivity(intent);  // פתיחת דף sigim
            return true;  // החזרת true כי ה-Intent בוצע בהצלחה
        }

        return super.onOptionsItemSelected(item);  // החזרת תוצאה ברירת מחדל
    }

    private void initviews() {
        b1 = findViewById(R.id.btn1);
        b2 = findViewById(R.id.btn2);
        tv1 = findViewById(R.id.output);

        mainLayout = findViewById(R.id.mainLayout);

        s = findViewById(R.id.switch1);
        s.setOnCheckedChangeListener(this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv1.setText("cohen");
                tv1.setTextColor(0xFF0000FF);
                Log.d("sagie", "Button 1");
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv1.setText("sagie");
                tv1.setTextColor(0xFFFF3B4B);
                Log.d("sagie", "Button 2");
            }
        });

        sb = findViewById(R.id.sb);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float alfha = (float) i / 100;
                image1.setAlpha(alfha);
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

        linerPage = findViewById(R.id.linerPage);
        linerPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LinearActivity.class);
                startActivity(intent);
                finish();
            }
        });

        guessGame = findViewById(R.id.GuessGame);
        guessGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GuessNumber.class);
                startActivityForResult(intent, 222);
            }
        });

        spButton = findViewById(R.id.spButton);
        spButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, sp.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            mainLayout.setBackgroundColor(0xFF6E7191);
        } else {
            mainLayout.setBackgroundColor(0xFF5E94FD);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            int numGuesses = data.getIntExtra("num_guesses", -1);
            String userName = data.getStringExtra("user_name");

            Toast.makeText(this, "Game finished! Number of guesses: " + numGuesses + " , user: " + userName, Toast.LENGTH_SHORT).show();

            tv1.setText(userName + " won in " + numGuesses + " guesses.");
        } else {
            Toast.makeText(this, "Game was canceled or didn't finish successfully.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ביטול רישום ה-BroadcastReceiver כשאקטיביטי נהרסת
        unregisterReceiver(batteryReceiver);
    }
}
