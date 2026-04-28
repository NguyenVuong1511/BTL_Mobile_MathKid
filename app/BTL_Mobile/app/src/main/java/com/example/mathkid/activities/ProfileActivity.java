package com.example.mathkid.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Achievement;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgProfileAvatar;
    TextView txtProfileName, txtProfileXP, txtProfileLevel, txtProgressValue;
    ProgressBar progressLevel;
    LinearLayout layoutAchievements;
    View btnLogout, btnEditAvatar, btnEditInfo, btnSecurity, btnViewAllAchievements;
    LinearLayout navHome, navRanking, navProfile;

    UserDAO userDAO;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void initViews() {
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileXP = findViewById(R.id.txtProfileXP);
        txtProfileLevel = findViewById(R.id.txtProfileLevel);
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
        btnViewAllAchievements.setOnClickListener(v -> {
            Intent intent = new Intent(this, AchievementsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditAvatar.setOnClickListener(v -> Toast.makeText(this, "Tính năng đổi ảnh đại diện đang phát triển!", Toast.LENGTH_SHORT).show());
        
        btnEditInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnSecurity.setOnClickListener(v -> Toast.makeText(this, "Cài đặt bảo mật đang phát triển!", Toast.LENGTH_SHORT).show());

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navRanking.setOnClickListener(v -> {
            Intent intent = new Intent(this, RankingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void loadProfileData() {
        String username = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(username);

        if (data != null) {
            txtProfileName.setText(data.username);
            txtProfileXP.setText(String.valueOf(data.exp));

            int xpPerLevel = 500;
            int currentLevel = (data.exp / xpPerLevel) + 1;
            int xpInCurrentLevel = data.exp % xpPerLevel;
            
            txtProfileLevel.setText(String.valueOf(currentLevel));
            txtProgressValue.setText(xpInCurrentLevel + " / " + xpPerLevel + " XP");
            progressLevel.setMax(xpPerLevel);
            progressLevel.setProgress(xpInCurrentLevel);

            if (data.avatar != null && !data.avatar.isEmpty()) {
                int resId = getResources().getIdentifier(data.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) {
                    imgProfileAvatar.setImageResource(resId);
                }
            }

            loadAchievements(data.id);
        }
    }

    private void loadAchievements(int userId) {
        layoutAchievements.removeAllViews();
        List<Achievement> achievements = userDAO.getAchievements(userId);
        
        int count = 0;
        if (achievements != null) {
            for (Achievement ach : achievements) {
                if (count >= 5) break;
                addAchievementView(ach);
                count++;
            }
        }
    }

    private void addAchievementView(Achievement ach) {
        FrameLayout frame = new FrameLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(110), dpToPx(130));
        params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        frame.setLayoutParams(params);
        frame.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        frame.setBackgroundResource(R.drawable.bg_soft_button);

        LinearLayout inner = new LinearLayout(this);
        inner.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setGravity(Gravity.CENTER);
        inner.setBackgroundResource(R.drawable.bg_soft_inner);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(45), dpToPx(45));
        img.setLayoutParams(imgParams);

        if (ach.icon != null && !ach.icon.isEmpty()) {
            if (ach.icon.startsWith("ic_") || !ach.icon.contains("/") && !ach.icon.contains("+")) {
                int iconRes = getResources().getIdentifier(ach.icon, "drawable", getPackageName());
                img.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_star);
            } else {
                try {
                    byte[] decodedString = Base64.decode(ach.icon, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    img.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    img.setImageResource(R.drawable.ic_star);
                }
            }
        } else {
            img.setImageResource(R.drawable.ic_star);
        }
        
        TextView title = new TextView(this);
        title.setText(ach.title);
        title.setTextColor(android.graphics.Color.WHITE);
        title.setTextSize(12);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dpToPx(8), 0, 0);

        if (!ach.isUnlocked) {
            img.setAlpha(0.3f);
            img.setColorFilter(android.graphics.Color.GRAY);
            title.setAlpha(0.5f);
        } else {
            img.setAlpha(1.0f);
            img.setColorFilter(null);
            title.setAlpha(1.0f);
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
