package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView txtGreeting, txtName, txtXPBadge, txtLevelProgress, txtXPValue;
    private ProgressBar progressBar;
    private ImageView imgAvatar;
    private FrameLayout btnLearn, btnPractice, btnExam, btnGames, btnAchievements, btnProgress;
    private LinearLayout navHome, navRanking, navProfile;

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToWelcome();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        userDAO = new UserDAO(this);

        initViews();
        setupClickListeners();
        updateGreeting();


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void initViews() {
        txtGreeting = findViewById(R.id.txtGreeting);
        txtName = findViewById(R.id.txtName);
        txtXPBadge = findViewById(R.id.txtXPBadge);
        txtLevelProgress = findViewById(R.id.txtLevelProgress);
        txtXPValue = findViewById(R.id.txtXPValue);
        progressBar = findViewById(R.id.progressBar);
        imgAvatar = findViewById(R.id.imgAvatar);

        btnLearn = findViewById(R.id.btnLearn);
        btnPractice = findViewById(R.id.btnPractice);
        btnExam = findViewById(R.id.btnExam);
        btnGames = findViewById(R.id.btnGames);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnProgress = findViewById(R.id.btnProgress);

        navHome = findViewById(R.id.navHome);
        navRanking = findViewById(R.id.navRanking);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupClickListeners() {
        if (imgAvatar != null) imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        if (btnLearn != null) {
            btnLearn.setOnClickListener(v -> {
                startActivity(new Intent(this, ItemLevel.class));
            });
        }

        if (btnPractice != null) {
            btnPractice.setOnClickListener(v -> {
                startActivity(new Intent(this, PracticeActivity.class));
            });
        }

        if (btnExam != null) {
            btnExam.setOnClickListener(v -> {
                startActivity(new Intent(this, ExamActivity.class));
            });
        }

        if (btnGames != null) {
            btnGames.setOnClickListener(v -> {
                startActivity(new Intent(this, MathGameActivity.class));
            });
        }

        if (btnAchievements != null) {
            btnAchievements.setOnClickListener(v -> {
                startActivity(new Intent(this, AchievementsActivity.class));
            });
        }

        if (btnProgress != null) {
            btnProgress.setOnClickListener(v -> {
                startActivity(new Intent(this, ProgressActivity.class));
            });
        }

        if (navRanking != null) {
            navRanking.setOnClickListener(v -> {
                startActivity(new Intent(this, RankingActivity.class));
            });
        }
    }

    private void loadUserData() {
        String currentUsername = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(currentUsername);

        if (data != null) {
            if (txtName != null) txtName.setText(data.username);
            
            int xpPerLevel = 500;
            int currentLevel = (data.exp / xpPerLevel) + 1;
            int xpInCurrentLevel = data.exp % xpPerLevel;
            
            if (txtLevelProgress != null) txtLevelProgress.setText("Tiến độ Cấp " + currentLevel);
            if (txtXPValue != null) txtXPValue.setText(xpInCurrentLevel + "/" + xpPerLevel + " XP");
            if (progressBar != null) {
                progressBar.setMax(xpPerLevel);
                progressBar.setProgress(xpInCurrentLevel);
            }

            if (txtXPBadge != null) {
                txtXPBadge.setText(data.exp + " XP");
            }

            if (data.avatar != null && !data.avatar.isEmpty() && imgAvatar != null) {
                int resId = getResources().getIdentifier(data.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) {
                    imgAvatar.setImageResource(resId);
                }
            }
        }
    }

    private void updateGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Chào buổi sáng! 👋";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = "Chào buổi trưa! ☀️";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            greeting = "Chào buổi chiều! 🌅";
        } else {
            greeting = "Chào buổi tối! 🌙";
        }
        if (txtGreeting != null) txtGreeting.setText(greeting);
    }

    private void redirectToWelcome() {
        Intent intent = new Intent(this, Welcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
