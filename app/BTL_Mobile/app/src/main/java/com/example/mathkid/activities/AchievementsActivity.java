package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private FrameLayout[] achievementContainers = new FrameLayout[9];
    private ImageView[] badgeImages = new ImageView[9];
    private TextView[] badgeTitles = new TextView[9];
    private TextView[] badgeDescs = new TextView[9];
    private LinearLayout[] innerLayouts = new LinearLayout[9];

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

        // Ánh xạ tối đa 9 ô huy hiệu từ XML
        for (int i = 0; i < 9; i++) {
            int containerId = getResources().getIdentifier("achievement" + (i + 1), "id", getPackageName());
            if (containerId != 0) {
                achievementContainers[i] = findViewById(containerId);
                innerLayouts[i] = (LinearLayout) achievementContainers[i].getChildAt(0);
                badgeImages[i] = (ImageView) innerLayouts[i].getChildAt(0);
                badgeTitles[i] = (TextView) innerLayouts[i].getChildAt(1);
                badgeDescs[i] = (TextView) innerLayouts[i].getChildAt(2);
            }
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

            // Ẩn tất cả trước
            for (int i = 0; i < 9; i++) {
                if (achievementContainers[i] != null) achievementContainers[i].setVisibility(View.GONE);
            }

            for (int i = 0; i < achievements.size() && i < 9; i++) {
                Achievement a = achievements.get(i);
                if (achievementContainers[i] == null) continue;

                achievementContainers[i].setVisibility(View.VISIBLE);
                badgeTitles[i].setText(a.title);
                badgeDescs[i].setText(a.description);

                if (a.isUnlocked) {
                    earnedCount++;
                    applyThemeToAchievement(i, R.style.OrangeButtonTheme, false);
                    
                    // Set icon thành tích (nếu có trong drawable)
                    int iconRes = getResources().getIdentifier(a.icon, "drawable", getPackageName());
                    if (iconRes != 0) {
                        badgeImages[i].setImageResource(iconRes);
                        badgeImages[i].setColorFilter(null);
                    }
                } else {
                    applyThemeToAchievement(i, R.style.GrayButtonTheme, true);
                    badgeImages[i].setImageResource(R.drawable.ic_lock);
                    badgeImages[i].setColorFilter(android.graphics.Color.parseColor("#9E9E9E"));
                }
            }

            txtBadgeProgress.setText("Bé đã đạt " + earnedCount + " / " + achievements.size() + " huy hiệu");
            achievementProgress.setMax(achievements.size());
            achievementProgress.setProgress(earnedCount);
        }
    }

    private void applyThemeToAchievement(int index, int themeResId, boolean isLocked) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, themeResId);
        achievementContainers[index].setBackground(AppCompatResources.getDrawable(wrapper, R.drawable.bg_soft_button));
        innerLayouts[index].setBackground(AppCompatResources.getDrawable(wrapper, R.drawable.bg_soft_inner));
        
        int textColor = isLocked ? android.graphics.Color.parseColor("#9E9E9E") : android.graphics.Color.WHITE;
        badgeTitles[index].setTextColor(textColor);
        badgeDescs[index].setTextColor(isLocked ? android.graphics.Color.parseColor("#BDBDBD") : android.graphics.Color.parseColor("#E0E0E0"));
    }
}
