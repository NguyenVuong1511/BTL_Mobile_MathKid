package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mathkid.activities.MainActivity;
import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Achievement;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private ProgressBar achievementProgress;
    private TextView txtBadgeProgress;
    private LinearLayout navHome, navRanking, navProfile;
    
    // Mảng chứa các FrameLayout của huy hiệu để dễ quản lý
    private FrameLayout[] achievementContainers = new FrameLayout[6];
    private ImageView[] badgeImages = new ImageView[6];
    private TextView[] badgeTitles = new TextView[6];

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_achievements);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
        loadAchievementData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        achievementProgress = findViewById(R.id.achievementProgress);
        txtBadgeProgress = findViewById(R.id.txtBadgeProgress);
        
        navHome = findViewById(R.id.navHome);
        navRanking = findViewById(R.id.navRanking);
        navProfile = findViewById(R.id.navProfile);

        // Ánh xạ 6 ô huy hiệu từ XML
        for (int i = 0; i < 6; i++) {
            int containerId = getResources().getIdentifier("achievement" + (i + 1), "id", getPackageName());
            int imageId = getResources().getIdentifier("imgBadge" + (i + 1), "id", getPackageName());
            int titleId = getResources().getIdentifier("txtBadgeTitle" + (i + 1), "id", getPackageName());
            
            achievementContainers[i] = findViewById(containerId);
            badgeImages[i] = findViewById(imageId);
            badgeTitles[i] = findViewById(titleId);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navRanking.setOnClickListener(v -> {
            Toast.makeText(this, "Bảng xếp hạng đang cập nhật!", Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void loadAchievementData() {
        String currentUsername = sessionManager.getUsername();
        UserDAO.UserData userData = userDAO.getUserData(currentUsername);

        if (userData != null) {
            List<Achievement> achievements = userDAO.getAchievements(userData.id);
            int earnedCount = 0;

            for (int i = 0; i < achievements.size() && i < 6; i++) {
                Achievement a = achievements.get(i);
                
                // Cập nhật giao diện dựa trên trạng thái đã đạt được hay chưa
                if (a.isUnlocked) {
                    earnedCount++;
                    achievementContainers[i].setAlpha(1.0f);
                    // Nếu huy hiệu đã mở, giữ nguyên theme màu sắc
                } else {
                    // Nếu chưa mở, làm mờ và hiển thị icon khóa (logic này bạn có thể tùy biến thêm)
                    achievementContainers[i].setAlpha(0.5f);
                }
                
                if (badgeTitles[i] != null) badgeTitles[i].setText(a.title);
            }

            txtBadgeProgress.setText("Bé đã đạt " + earnedCount + " / " + achievements.size() + " huy hiệu");
            achievementProgress.setMax(achievements.size());
            achievementProgress.setProgress(earnedCount);
        }
    }
}
