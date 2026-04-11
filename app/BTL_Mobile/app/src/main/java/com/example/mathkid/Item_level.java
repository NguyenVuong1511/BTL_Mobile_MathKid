package com.example.mathkid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathkid.database.Lesson;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

import java.util.List;

public class Item_level extends AppCompatActivity {

    private RecyclerView recyclerViewLessons;
    private LessonAdapter adapter;
    private UserDAO userDAO;
    private SessionManager sessionManager;
    private TextView txtProgressSummary;
    private View btnBack; // Thay đổi từ CardView thành View để tránh lỗi ép kiểu
    private android.widget.LinearLayout navHome, navRanking, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_level);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        // Tạo dữ liệu mẫu bài học nếu chưa có
        userDAO.seedDataIfNeeded();

        initViews();
        setupClickListeners();
        setupRecyclerView();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        recyclerViewLessons = findViewById(R.id.recyclerViewLessons);
        txtProgressSummary = findViewById(R.id.txtProgressSummary);
        btnBack = findViewById(R.id.btnBack);
        navHome = findViewById(R.id.navHome);
        navRanking = findViewById(R.id.navRanking);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupRecyclerView() {
        recyclerViewLessons.setLayoutManager(new LinearLayoutManager(this));
        
        String username = sessionManager.getUsername();
        UserDAO.UserData userData = userDAO.getUserData(username);
        
        if (userData != null) {
            List<Lesson> lessons = userDAO.getLessonsWithProgress(userData.id);
            if (lessons.isEmpty()) {
                // Nếu vẫn trống, thử seed lại lần nữa
                userDAO.seedDataIfNeeded();
                lessons = userDAO.getLessonsWithProgress(userData.id);
            }
            
            adapter = new LessonAdapter(lessons, this);
            recyclerViewLessons.setAdapter(adapter);

            long completedCount = 0;
            for (Lesson l : lessons) if (l.isComplete) completedCount++;
            txtProgressSummary.setText("Hoàn thành " + completedCount + " trên " + lessons.size() + " cấp độ");
        } else {
            // Nếu không tìm thấy user (do DB đã bị reset), yêu cầu đăng nhập lại
            Toast.makeText(this, "Phiên làm việc hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show();
            sessionManager.logout();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        navRanking.setOnClickListener(v -> {
            Toast.makeText(this, "Bảng xếp hạng đang cập nhật!", Toast.LENGTH_SHORT).show();
        });
    }
}
