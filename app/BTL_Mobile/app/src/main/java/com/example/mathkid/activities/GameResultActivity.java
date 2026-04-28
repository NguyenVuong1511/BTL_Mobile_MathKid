package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mathkid.R;

public class GameResultActivity extends AppCompatActivity {

    private TextView tvScoreValue, tvMessage;
    private Button btnPlayAgain, btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_result);
        


        int score = getIntent().getIntExtra("score", 0);

        tvScoreValue = findViewById(R.id.tvScoreValue);
        tvMessage = findViewById(R.id.tvMessage);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnBackHome = findViewById(R.id.btnBackHome);

        tvScoreValue.setText(String.valueOf(score));
        
        if (score >= 50) {
            tvMessage.setText("Tuyệt vời! Bé rất thông minh! ✨");
        } else if (score >= 20) {
            tvMessage.setText("Khá lắm! Cố gắng hơn nữa nhé! 👍");
        } else {
            tvMessage.setText("Hãy luyện tập thêm để tiến bộ nhé! ❤️");
        }

        btnPlayAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, MathGameActivity.class));
            finish();
        });

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
