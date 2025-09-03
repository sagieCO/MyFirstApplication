package com.sagie.myfirstapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GuessNumber extends AppCompatActivity {

    private LinearLayout guessPart;
    private Button okBtn, btnRange, resetGame,btnHome;
    private EditText et1, et2, yourGuess;
    private TextView result;
    private int numGuesses = 0, rand;
    private MediaPlayer mediaPlayer;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess_number);
        userName = getIntent().getStringExtra("user_name"); // קבלת שם המשתמש מ-MainActivity

        okBtn = findViewById(R.id.okBtn);
        okBtn.setVisibility(View.GONE);

        et1 = findViewById(R.id.etGuess1);
        et2 = findViewById(R.id.etGuess2);
        yourGuess = findViewById(R.id.guessYour);
        result = findViewById(R.id.result);
        btnRange = findViewById(R.id.btnRange);
        guessPart = findViewById(R.id.guessPartLinear);
        resetGame = findViewById(R.id.resetGame);
        btnHome=findViewById(R.id.backHome1);

        //mediaPlayer = MediaPlayer.create(this, R.raw.brazil);
        //mediaPlayer.setLooping(true);
        //mediaPlayer.start();

        btnRange.setOnClickListener(v -> {
            if (validateRange()) startGuessing();
            else Toast.makeText(this, "Please fill the fields correctly", Toast.LENGTH_SHORT).show();
        });

        okBtn.setOnClickListener(v -> {
            int guess = Integer.parseInt(yourGuess.getText().toString());
            checkGuess(guess);
        });

        resetGame.setOnClickListener(v -> startGame());

     btnHome.setOnClickListener(v -> {
       Intent intent = new Intent(GuessNumber.this, MainActivity.class);
       startActivity(intent);});

    }

    private boolean validateRange() {
        if (!et1.getText().toString().isEmpty() && !et2.getText().toString().isEmpty()) {
            int min = Integer.parseInt(et1.getText().toString());
            int max = Integer.parseInt(et2.getText().toString());
            if (max > min) {
                rand = min + (int)((max - min + 1) * Math.random());
                numGuesses = 1;
                return true;
            }
        }
        return false;
    }

    private void checkGuess(int guess) {
        if (guess == rand) {
            result.setText("Correct! You won in " + numGuesses + " tries");

            // מחזיר את התוצאה ל-MainActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("num_guesses", numGuesses);
            resultIntent.putExtra("user_name", userName);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (guess > rand) {
            numGuesses++;
            result.setText("Wrong. Answer is smaller.");
        } else {
            numGuesses++;
            result.setText("Wrong. Answer is bigger.");
        }
    }

    private void startGuessing() {
        et1.setEnabled(false);
        et2.setEnabled(false);
        btnRange.setEnabled(false);
        guessPart.setVisibility(View.VISIBLE);

        okBtn.setOnClickListener(v -> {
            String guessText = yourGuess.getText().toString();
            if (!guessText.isEmpty()) {
                int guess = Integer.parseInt(guessText);
                checkGuess(guess);
            } else {
                Toast.makeText(this, "enter the inputs", Toast.LENGTH_LONG).show();
            }
        });
        okBtn.setVisibility(View.VISIBLE);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
