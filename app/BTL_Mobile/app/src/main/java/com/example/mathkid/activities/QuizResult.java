package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

public class QuizResult extends AppCompatActivity {

    private TextView txtTitle, txtSub, txtXPEarned, txtResultCorrect, txtResultStars, txtHome;
    private ImageView imgTrophy;
    private LinearLayout layoutStars;
    private View btnNext, btnRetry;

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_result);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        initViews();
        displayResults();
        setupClickListeners();
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtSub = findViewById(R.id.txtSub);
        txtXPEarned = findViewById(R.id.txtXPEarned);
        txtResultCorrect = findViewById(R.id.txtResultCorrect);
        txtResultStars = findViewById(R.id.txtResultStars);

        layoutStars = findViewById(R.id.layoutStars);
        btnNext = findViewById(R.id.btnNext);
        btnRetry = findViewById(R.id.btnRetry);
        txtHome = findViewById(R.id.txtHome);
        imgTrophy = findViewById(R.id.imgTrophy);
    }

    private void displayResults() {
        Intent intent = getIntent();
        int correct = intent.getIntExtra("correct_count", 0);
        int total = intent.getIntExtra("total_questions", 0);
        int xp = intent.getIntExtra("xp_earned", 0);
        int activityId = intent.getIntExtra("activity_id", -1);

        // Hiển thị text chỉ số
        if (txtResultCorrect != null) txtResultCorrect.setText(String.valueOf(correct));
        if (txtXPEarned != null) txtXPEarned.setText("+" + xp + " XP ⚡");
        
        // Tính số sao
        int stars = 0;
        if (total > 0) {
            float ratio = (float) correct / total;
            if (ratio >= 1.0f) stars = 3;
            else if (ratio >= 0.6f) stars = 2;
            else if (ratio >= 0.3f) stars = 1;
        }
        
        if (txtResultStars != null) txtResultStars.setText(stars + "/" + (total > 0 ? "3" : "0"));

        // Cập nhật giao diện sao
        if (layoutStars != null) {
            for (int i = 0; i < layoutStars.getChildCount(); i++) {
                if (layoutStars.getChildAt(i) instanceof ImageView) {
                    ImageView star = (ImageView) layoutStars.getChildAt(i);
                    if (i < stars) star.setAlpha(1.0f);
                    else star.setAlpha(0.2f);
                }
            }
        }

        // Tùy biến thông điệp
        if (stars == 3) {
            txtTitle.setText("Hoàn hảo! 🎉");
            txtSub.setText("Bé làm đúng hết rồi! Bé là ngôi sao toán học.");
        } else if (stars >= 1) {
            txtTitle.setText("Giỏi lắm! 👏");
            txtSub.setText("Bé đã hoàn thành bài học rồi đấy.");
        } else {
            txtTitle.setText("Cố gắng lên! 💪");
            txtSub.setText("Bé hãy thử lại để đạt kết quả cao hơn nhé.");
        }

        // Lưu vào CSDL
        String username = sessionManager.getUsername();
        UserDAO.UserData user = userDAO.getUserData(username);
        if (user != null) {
            // Luôn cộng XP nếu có
            if (xp > 0) {
                userDAO.addXP(user.id, xp);
            }
            // Chỉ cập nhật progress nếu là bài học theo level (activityId != -1)
            if (activityId != -1) {
                userDAO.updateProgress(user.id, activityId, stars, correct, stars > 0);
            }
        }
    }

    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, ItemLevel.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnRetry.setOnClickListener(v -> {
            finish(); 
        });

        txtHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
