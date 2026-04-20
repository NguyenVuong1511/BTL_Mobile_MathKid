package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.mathkid.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        CardView cardUsers = findViewById(R.id.cardUsers);
        CardView cardLessons = findViewById(R.id.cardLessons);
        CardView cardQuestions = findViewById(R.id.cardQuestions);
        CardView cardStats = findViewById(R.id.cardStats);
        ImageView btnLogout = findViewById(R.id.btnLogout);

        cardUsers.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageUsersActivity.class));
        });

        cardLessons.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageLevelsActivity.class));
        });

        cardQuestions.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboardActivity.this, ManageQuestionsActivity.class));
        });

        cardStats.setOnClickListener(v -> {
            // Intent to Statistics
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
