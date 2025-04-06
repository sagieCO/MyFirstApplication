package com.sagie.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GuessNumber extends AppCompatActivity {

    private Context context;
    private LinearLayout guessPart;
    private Button okBtn, backHome1, btnRange, resetGame;
    private EditText numMin, numMax, yourGuess, et1, et2;
    private TextView result;
    private int numGuesses = 1, rand;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_number);
        context = this;

        initviews();
        startGame();

        mediaPlayer = MediaPlayer.create(this, R.raw.brazil);  // יש לשים את השם הזה לפי השם שלך ב־raw
        mediaPlayer.setLooping(true);  // אם אתה רוצה שהמוזיקה תחזור בלולאה
        mediaPlayer.start();
    }

    private void initviews() {
        okBtn = findViewById(R.id.okBtn);
        et1 = findViewById(R.id.etGuess1);
        et2 = findViewById(R.id.etGuess2);
        yourGuess = findViewById(R.id.guessYour);
        result = findViewById(R.id.result);
        btnRange = findViewById(R.id.btnRange);
        guessPart = findViewById(R.id.guessPartLinear);
        resetGame = findViewById(R.id.resetGame);
        backHome1 = findViewById(R.id.backHome1);

        btnRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateRange()) {
                    startGuessing();
                } else {
                    Toast.makeText(context, "Please fill the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        backHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // הוספת Intent לחזרה לפעילות הראשית (MainActivity)
                Intent intent = new Intent(GuessNumber.this, MainActivity.class);

                // התחלת פעילות עם startActivityForResult והעברת קוד בקשה (למשל 100)
                startActivityForResult(intent, 222);
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guessText = yourGuess.getText().toString();
                int guess = Integer.parseInt(guessText);

                checkGuess(guess);
            }
        });

        resetGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }

    private boolean validateRange() {
        String minText = et1.getText().toString();
        String maxText = et2.getText().toString();
        if (!minText.isEmpty() && !maxText.isEmpty()) {
            int min = Integer.parseInt(minText);
            int max = Integer.parseInt(maxText);
            if (max > min) {
                rand = min + (int) ((max - min + 1) * Math.random());
                numGuesses = 1;
                return true;
            }
        }
        return false;
    }

    private void checkGuess(int yourGuess) {
        if (yourGuess == rand) {
            result.setText("Correct. You won in: " + numGuesses + " tries");

            // יצירת Intent עם המידע
            Intent resultIntent = new Intent();
            resultIntent.putExtra("num_guesses", numGuesses);  // מספר הניחושים
            resultIntent.putExtra("user_name", "bbb");  // שם המשתמש (או כל שם אחר שתרצה לשלוח)

            // מחזיר את התוצאה לפעילות הקוראת (MainActivity)
            setResult(RESULT_OK, resultIntent);
            finish();
            // אל נסיים את הפעילות עדיין – Activity הראשית תסיים אותה לאחר קבלת התוצאה
        } else if (yourGuess > rand) {
            numGuesses++;
            result.setText("Wrong. The answer is smaller than your guess");
        } else {
            numGuesses++;
            result.setText("Wrong. The answer is bigger than your guess");
        }
    }

    private void startGame() {
        et1.setText("");
        et2.setText("");
        yourGuess.setText("");

        numGuesses = 0;
        guessPart.setVisibility(View.GONE);

        et1.setEnabled(true);
        et2.setEnabled(true);
        btnRange.setEnabled(true);
    }

    private void startGuessing() {
        et1.setEnabled(false);
        et2.setEnabled(false);
        btnRange.setEnabled(false);
        guessPart.setVisibility(View.VISIBLE);
    }

    // עצירת המוזיקה כשהפעילות יוצאת מהמצב הפעיל
    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();  // עוצר את המוזיקה
            mediaPlayer.release();  // משחרר את המשאבים של המוזיקה
        }
    }

    // הפעלת המוזיקה מחדש אם היא לא מנוגנת
    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();  // מתחיל את המוזיקה אם היא לא מנוגנת
        }
    }
}
