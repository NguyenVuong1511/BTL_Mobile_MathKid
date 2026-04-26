package com.example.mathkid.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mathkid.R;
import com.example.mathkid.database.DatabaseContract;
import com.example.mathkid.database.DatabaseHelper;

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView txtTotalUsers, txtTotalLessons, txtTotalQuestions, txtTotalStars;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        loadStatistics();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        txtTotalUsers = findViewById(R.id.txtTotalUsers);
        txtTotalLessons = findViewById(R.id.txtTotalLessons);
        txtTotalQuestions = findViewById(R.id.txtTotalQuestions);
        txtTotalStars = findViewById(R.id.txtTotalStars);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadStatistics() {
        int userCount = dbHelper.getCount(DatabaseContract.UserEntry.TABLE_NAME);
        int lessonCount = dbHelper.getCount(DatabaseContract.ActivitiesEntry.TABLE_NAME);
        int questionCount = dbHelper.getCount(DatabaseContract.QuestionsEntry.TABLE_NAME);
        int totalStars = dbHelper.getTotalStars();

        txtTotalUsers.setText(String.valueOf(userCount));
        txtTotalLessons.setText(String.valueOf(lessonCount));
        txtTotalQuestions.setText(String.valueOf(questionCount));
        txtTotalStars.setText(String.valueOf(totalStars));
    }
}
