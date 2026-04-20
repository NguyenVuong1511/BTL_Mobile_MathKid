package com.example.mathkid.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MathGameActivity extends AppCompatActivity {

    private TextView txtScore, txtLives, txtProblem, txtOption1, txtOption2, txtOption3, txtOption4;
    private FrameLayout btnOption1, btnOption2, btnOption3, btnOption4, btnExit;
    private ProgressBar timerProgressBar;

    private int score = 0;
    private int lives = 3;
    private int correctAnswer;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 10000; // 10 giây mỗi câu

    private Random random = new Random();
    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_game);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
        nextQuestion();
    }

    private void initViews() {
        txtScore = findViewById(R.id.txtScore);
        txtLives = findViewById(R.id.txtLives);
        txtProblem = findViewById(R.id.txtProblem);
        timerProgressBar = findViewById(R.id.timerProgressBar);

        txtOption1 = findViewById(R.id.txtOption1);
        txtOption2 = findViewById(R.id.txtOption2);
        txtOption3 = findViewById(R.id.txtOption3);
        txtOption4 = findViewById(R.id.txtOption4);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        btnExit = findViewById(R.id.btnExit);
    }

    private void setupClickListeners() {
        btnExit.setOnClickListener(v -> finish());

        View.OnClickListener optionClickListener = v -> {
            int selectedAnswer = 0;
            try {
                if (v.getId() == R.id.btnOption1) selectedAnswer = Integer.parseInt(txtOption1.getText().toString());
                else if (v.getId() == R.id.btnOption2) selectedAnswer = Integer.parseInt(txtOption2.getText().toString());
                else if (v.getId() == R.id.btnOption3) selectedAnswer = Integer.parseInt(txtOption3.getText().toString());
                else if (v.getId() == R.id.btnOption4) selectedAnswer = Integer.parseInt(txtOption4.getText().toString());
                checkAnswer(selectedAnswer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        btnOption1.setOnClickListener(optionClickListener);
        btnOption2.setOnClickListener(optionClickListener);
        btnOption3.setOnClickListener(optionClickListener);
        btnOption4.setOnClickListener(optionClickListener);
    }

    private void nextQuestion() {
        if (countDownTimer != null) countDownTimer.cancel();

        // Tạo phép tính ngẫu nhiên
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        boolean isPlus = random.nextBoolean();

        if (isPlus) {
            correctAnswer = a + b;
            txtProblem.setText(a + " + " + b + " = ?");
        } else {
            // Đảm bảo kết quả không âm
            if (a < b) { int temp = a; a = b; b = temp; }
            correctAnswer = a - b;
            txtProblem.setText(a + " - " + b + " = ?");
        }

        // Tạo danh sách đáp án
        List<Integer> options = new ArrayList<>();
        options.add(correctAnswer);
        while (options.size() < 4) {
            int wrong = correctAnswer + (random.nextInt(7) - 3);
            if (wrong >= 0 && !options.contains(wrong)) {
                options.add(wrong);
            }
        }
        Collections.shuffle(options);

        txtOption1.setText(String.valueOf(options.get(0)));
        txtOption2.setText(String.valueOf(options.get(1)));
        txtOption3.setText(String.valueOf(options.get(2)));
        txtOption4.setText(String.valueOf(options.get(3)));

        startTimer();
    }

    private void startTimer() {
        timeLeftInMillis = 10000;
        timerProgressBar.setProgress(100);

        countDownTimer = new CountDownTimer(timeLeftInMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int progress = (int) (millisUntilFinished * 100 / 10000);
                timerProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                timerProgressBar.setProgress(0);
                handleWrongAnswer();
            }
        }.start();
    }

    private void checkAnswer(int selected) {
        if (selected == correctAnswer) {
            score += 10;
            txtScore.setText("ĐIỂM: " + score);
            Toast.makeText(this, "Đúng rồi! +10", Toast.LENGTH_SHORT).show();
            nextQuestion();
        } else {
            handleWrongAnswer();
        }
    }

    private void handleWrongAnswer() {
        lives--;
        updateLivesUI();
        if (lives > 0) {
            Toast.makeText(this, "Sai rồi bé ơi! ❤️", Toast.LENGTH_SHORT).show();
            nextQuestion();
        } else {
            gameOver();
        }
    }

    private void updateLivesUI() {
        StringBuilder l = new StringBuilder();
        for (int i = 0; i < lives; i++) l.append("❤️");
        txtLives.setText(l.toString());
    }

    private void gameOver() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        // Cộng XP cho người dùng
        String username = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(username);
        if (data != null) {
            userDAO.addXP(data.id, score / 2); // Thưởng XP bằng nửa số điểm game
        }

        // Chuyển sang màn hình kết quả
        Intent intent = new Intent(this, GameResultActivity.class);
        intent.putExtra("score", score);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
