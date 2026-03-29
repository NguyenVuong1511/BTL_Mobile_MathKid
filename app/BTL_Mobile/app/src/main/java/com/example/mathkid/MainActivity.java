package com.example.mathkid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    TextView txtName, txtXPBadge, txtLevelProgress, txtXPValue, txtStreakTitle;
    ProgressBar progressBar;
    ImageView imgAvatar;

    UserDAO userDAO;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo
        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Ánh xạ View
        txtName = findViewById(R.id.txtName);
        txtXPBadge = findViewById(R.id.txtXPBadge); 
        txtLevelProgress = findViewById(R.id.txtLevelProgress);
        txtXPValue = findViewById(R.id.txtXPValue);
        txtStreakTitle = findViewById(R.id.txtStreakTitle);
        progressBar = findViewById(R.id.progressBar);
        imgAvatar = findViewById(R.id.imgAvatar); 

        loadUserData();

        // Sự kiện Đăng xuất khi click vào Avatar
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> {
                sessionManager.logout();
                Toast.makeText(MainActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        String currentUsername = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(currentUsername);

        if (data != null) {
            txtName.setText(data.username);
            txtLevelProgress.setText("Tiến độ Cấp " + data.level);

            // Tính toán EXP (Giả sử 100 EXP lên 1 level)
            int currentExpInLevel = data.exp % 100;
            txtXPValue.setText(currentExpInLevel + "/100 XP");
            progressBar.setProgress(currentExpInLevel);

            if (txtXPBadge != null) txtXPBadge.setText(data.exp + " XP");

            txtStreakTitle.setText("Chuỗi " + data.streak + " ngày!");

            // Đổ Avatar
            if (imgAvatar != null) {
                int resId = getResources().getIdentifier(data.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) imgAvatar.setImageResource(resId);
            }
        }
    }
}
