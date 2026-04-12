package com.example.mathkid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgProfileAvatar;
    TextView txtProfileName, txtProfileXP, txtProfileLevel, txtProfileStreak;
    View btnLogout; // Đổi từ Button thành View để tương thích với FrameLayout trong XML
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
        btnLogout = findViewById(R.id.btnLogout);
        
        navHome = findViewById(R.id.navHome);
        navRanking = findViewById(R.id.navRanking);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupClickListeners() {
        // Nút đăng xuất
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Điều hướng Bottom Nav
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

            if (data.avatar != null && !data.avatar.isEmpty()) {
                int resId = getResources().getIdentifier(data.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) {
                    imgProfileAvatar.setImageResource(resId);
                }
            }
        }
    }
}
