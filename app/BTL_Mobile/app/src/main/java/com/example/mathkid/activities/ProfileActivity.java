package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Achievement;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgProfileAvatar;
    TextView txtProfileName, txtProfileXP, txtProfileLevel, txtProfileStreak, txtProgressValue;
    ProgressBar progressLevel;
    LinearLayout layoutAchievements;
    View btnLogout, btnEditAvatar, btnEditInfo, btnSecurity, btnViewAllAchievements;
    LinearLayout navHome, navRanking, navProfile;

    UserDAO userDAO;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
        loadProfileData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileXP = findViewById(R.id.txtProfileXP);
        txtProfileLevel = findViewById(R.id.txtProfileLevel);
        txtProfileStreak = findViewById(R.id.txtProfileStreak);
        txtProgressValue = findViewById(R.id.txtProgressValue);
        progressLevel = findViewById(R.id.progressLevel);
        layoutAchievements = findViewById(R.id.layoutAchievements);
        
        btnViewAllAchievements = findViewById(R.id.btnViewAllAchievements);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        btnEditInfo = findViewById(R.id.btnEditInfo);
        btnSecurity = findViewById(R.id.btnSecurity);
        
        navHome = findViewById(R.id.navHome);
        navRanking = findViewById(R.id.navRanking);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupClickListeners() {
        // Xem tất cả thành tích
        btnViewAllAchievements.setOnClickListener(v -> {
            Intent intent = new Intent(this, AchievementsActivity.class);
            startActivity(intent);
        });

        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditAvatar.setOnClickListener(v -> Toast.makeText(this, "Tính năng đổi ảnh đại diện đang phát triển!", Toast.LENGTH_SHORT).show());
        btnEditInfo.setOnClickListener(v -> Toast.makeText(this, "Chỉnh sửa thông tin đang phát triển!", Toast.LENGTH_SHORT).show());
        btnSecurity.setOnClickListener(v -> Toast.makeText(this, "Cài đặt bảo mật đang phát triển!", Toast.LENGTH_SHORT).show());

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navRanking.setOnClickListener(v -> {
            Toast.makeText(this, "Bảng xếp hạng đang cập nhật!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadProfileData() {
        String username = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(username);

        if (data != null) {
            txtProfileName.setText(data.username);
            txtProfileXP.setText(String.valueOf(data.exp));
            txtProfileLevel.setText(String.valueOf(data.level));
            txtProfileStreak.setText(String.valueOf(data.streak));

            // Tiến trình cấp độ
            int nextLevelXP = 1000;
            int currentXP = data.exp % nextLevelXP;
            txtProgressValue.setText(currentXP + " / " + nextLevelXP + " XP");
            progressLevel.setProgress((currentXP * 100) / nextLevelXP);

            if (data.avatar != null && !data.avatar.isEmpty()) {
                int resId = getResources().getIdentifier(data.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) {
                    imgProfileAvatar.setImageResource(resId);
                }
            }

            // Load thành tích từ CSDL
            loadAchievements(data.id);
        }
    }

    private void loadAchievements(int userId) {
        layoutAchievements.removeAllViews();
        List<Achievement> achievements = userDAO.getAchievements(userId);
        
        // Chỉ hiển thị 5 thành tích đầu tiên ở màn hình profile
        int count = 0;
        for (Achievement ach : achievements) {
            if (count >= 5) break;
            addAchievementView(ach);
            count++;
        }
    }

    private void addAchievementView(Achievement ach) {
        // Tạo View thành tích động theo phong cách Soft UI
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(110), dpToPx(130));
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        frame.setLayoutParams(params);
        frame.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        // Set theme/màu dựa trên trạng thái mở khóa
        if (ach.isUnlocked) {
            frame.setBackgroundResource(R.drawable.bg_soft_button);
            // Chọn màu ngẫu nhiên hoặc theo loại (ví dụ dùng theme Orange)
            frame.setContextClickable(true);
        } else {
            frame.setBackgroundResource(R.drawable.bg_soft_button);
            // Gray theme cho cái chưa khóa
        }

        LinearLayout inner = new LinearLayout(this);
        inner.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        inner.setBackgroundResource(R.drawable.bg_soft_inner);

        ImageView img = new ImageView(this);
        int iconRes = getResources().getIdentifier(ach.icon, "drawable", getPackageName());
        img.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_star);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(45), dpToPx(45));
        img.setLayoutParams(imgParams);
        
        if (!ach.isUnlocked) {
            img.setAlpha(0.3f);
        }

        TextView title = new TextView(this);
        title.setText(ach.title);
        title.setTextColor(getResources().getColor(android.R.color.white));
        title.setTextSize(12);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dpToPx(8), 0, 0);
        
        if (!ach.isUnlocked) {
            title.setAlpha(0.5f);
        }

        inner.addView(img);
        inner.addView(title);
        frame.addView(inner);
        layoutAchievements.addView(frame);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
