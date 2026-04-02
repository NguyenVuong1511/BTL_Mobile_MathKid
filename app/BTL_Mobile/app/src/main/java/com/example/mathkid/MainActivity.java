package com.example.mathkid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
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

import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private TextView txtGreeting, txtName, txtXPBadge, txtLevelProgress, txtXPValue, txtStreakTitle;
    private ProgressBar progressBar;
    private ImageView imgAvatar;
    private FrameLayout btnLearn, btnPractice, btnExam, btnGames, btnAchievements, btnProgress;
    private LinearLayout navHome, navRanking, navProfile;

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Khởi tạo SessionManager và kiểm tra đăng nhập
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToWelcome();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo Database DAO
        userDAO = new UserDAO(this);

        initViews();
        setupClickListeners();
        updateGreeting();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dữ liệu mỗi khi quay lại màn hình chính
        loadUserData();
    }

    private void initViews() {
        txtGreeting = findViewById(R.id.txtGreeting);
        txtName = findViewById(R.id.txtName);
        txtXPBadge = findViewById(R.id.txtXPBadge);
        txtLevelProgress = findViewById(R.id.txtLevelProgress);
        txtXPValue = findViewById(R.id.txtXPValue);
        txtStreakTitle = findViewById(R.id.txtStreakTitle);
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
        // Mở hồ sơ
        imgAvatar.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Các chức năng học tập
        btnLearn.setOnClickListener(v -> {
            // Chuyển đến màn hình chọn cấp độ/bài học
            startActivity(new Intent(this, Item_level.class));
        });

        btnPractice.setOnClickListener(v -> {
            Toast.makeText(this, "Chế độ luyện tập đang phát triển!", Toast.LENGTH_SHORT).show();
        });

        btnExam.setOnClickListener(v -> {
            Toast.makeText(this, "Kỳ thi sẽ mở vào cuối tuần!", Toast.LENGTH_SHORT).show();
        });

        btnGames.setOnClickListener(v -> {
            Toast.makeText(this, "Trò chơi toán học đang được cập nhật!", Toast.LENGTH_SHORT).show();
        });

        btnAchievements.setOnClickListener(v -> {
            startActivity(new Intent(this, activity_achievements.class));
        });

        btnProgress.setOnClickListener(v -> {
            Toast.makeText(this, "Xem thống kê chi tiết tiến trình của bạn", Toast.LENGTH_SHORT).show();
        });

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            // Đang ở Home rồi, có thể cuộn lên đầu nếu cần
        });

        navRanking.setOnClickListener(v -> {
            Toast.makeText(this, "Bảng xếp hạng đang tải...", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserData() {
        String currentUsername = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(currentUsername);

        if (data != null) {
            txtName.setText(data.username);
            txtLevelProgress.setText("Tiến độ Cấp " + data.level);

            // Logic tính toán XP (ví dụ: mỗi 100 XP lên 1 level)
            int xpInCurrentLevel = data.exp % 100;
            int maxXPPerLevel = 100;
            
            txtXPValue.setText(xpInCurrentLevel + "/" + maxXPPerLevel + " XP");
            progressBar.setMax(maxXPPerLevel);
            progressBar.setProgress(xpInCurrentLevel);

            if (txtXPBadge != null) {
                txtXPBadge.setText(data.exp + " XP");
            }

            txtStreakTitle.setText("Chuỗi " + data.streak + " ngày!");

            // Hiển thị Avatar từ tài nguyên drawable
            if (data.avatar != null && !data.avatar.isEmpty()) {
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
        txtGreeting.setText(greeting);
    }

    private void redirectToWelcome() {
        Intent intent = new Intent(this, Welcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
