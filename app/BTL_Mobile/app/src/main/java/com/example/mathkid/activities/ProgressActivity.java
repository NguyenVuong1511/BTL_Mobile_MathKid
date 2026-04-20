package com.example.mathkid.activities;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

import java.util.List;

public class ProgressActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private TextView txtCompletionPercent, txtSummaryText, txtTotalStars, txtAccuracy;
    private ProgressBar mainProgressBar;
    private LinearLayout layoutTopicStats;

    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        initViews();
        loadProgressData();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtCompletionPercent = findViewById(R.id.txtCompletionPercent);
        txtSummaryText = findViewById(R.id.txtSummaryText);
        txtTotalStars = findViewById(R.id.txtTotalStars);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        mainProgressBar = findViewById(R.id.mainProgressBar);
        layoutTopicStats = findViewById(R.id.layoutTopicStats);
    }

    private void loadProgressData() {
        String username = sessionManager.getUsername();
        UserDAO.UserData userData = userDAO.getUserData(username);

        if (userData != null) {
            UserDAO.ProgressSummary summary = userDAO.getUserProgressSummary(userData.id);

            // 1. Hiển thị tổng quan
            int percent = (summary.totalActivities > 0) ? (summary.completedActivities * 100 / summary.totalActivities) : 0;
            txtCompletionPercent.setText(percent + "%");
            txtSummaryText.setText("Bé đã hoàn thành " + summary.completedActivities + " / " + summary.totalActivities + " bài học");
            
            ObjectAnimator animator = ObjectAnimator.ofInt(mainProgressBar, "progress", 0, percent);
            animator.setDuration(1000);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            // 2. Các chỉ số phụ
            txtTotalStars.setText(String.valueOf(summary.totalStarsEarned));
            
            // Tính độ chính xác (dựa trên tỉ lệ sao đạt được)
            int accuracy = (summary.totalStarsPossible > 0) ? (summary.totalStarsEarned * 100 / summary.totalStarsPossible) : 0;
            txtAccuracy.setText(accuracy + "%");

            // 3. Hiển thị chi tiết theo chủ đề
            displayTopicStats(summary.topicStats);
        }
    }

    private void displayTopicStats(List<UserDAO.TopicProgress> stats) {
        layoutTopicStats.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (UserDAO.TopicProgress tp : stats) {
            View itemView = inflater.inflate(R.layout.item_topic_progress, layoutTopicStats, false);
            
            TextView txtTitle = itemView.findViewById(R.id.txtTopicTitle);
            TextView txtInfo = itemView.findViewById(R.id.txtTopicInfo);
            ProgressBar pb = itemView.findViewById(R.id.topicProgressBar);

            txtTitle.setText(tp.title);
            txtInfo.setText(tp.completed + "/" + tp.total + " bài");
            
            int p = (tp.total > 0) ? (tp.completed * 100 / tp.total) : 0;
            pb.setProgress(p);

            layoutTopicStats.addView(itemView);
        }
    }
}
