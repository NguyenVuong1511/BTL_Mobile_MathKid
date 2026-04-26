package com.example.mathkid.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.DatabaseContract;
import com.example.mathkid.database.DatabaseHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView txtTotalUsers, txtTotalLessons, txtTotalQuestions, txtTotalStars;
    private ImageView btnBack;
    private PieChart pieChart;
    private BarChart barChart;

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
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
    }

    private void loadStatistics() {
        int userCount = dbHelper.getCount(DatabaseContract.UserEntry.TABLE_NAME);
        int lessonCount = dbHelper.getCount(DatabaseContract.ActivitiesEntry.TABLE_NAME);
        int questionCount = dbHelper.getCount(DatabaseContract.QuestionsEntry.TABLE_NAME);
        int totalStars = dbHelper.getTotalStars();

        // Cập nhật text đơn giản
        txtTotalUsers.setText(String.valueOf(userCount));
        txtTotalLessons.setText(String.valueOf(lessonCount));
        txtTotalQuestions.setText(String.valueOf(questionCount));
        txtTotalStars.setText(String.valueOf(totalStars));

        // Thiết lập biểu đồ
        setupPieChart(lessonCount, questionCount);
        setupBarChart(userCount, totalStars);
    }

    private void setupPieChart(int lessons, int questions) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(lessons, "Bài học"));
        entries.add(new PieEntry(questions, "Câu hỏi"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#1976D2"), Color.parseColor("#F57C00")});
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Nội dung");
        pieChart.setHoleRadius(40f);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupBarChart(int users, int stars) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) users));
        entries.add(new BarEntry(1f, (float) stars));

        BarDataSet dataSet = new BarDataSet(entries, "Số lượng");
        dataSet.setColors(new int[]{Color.parseColor("#2E7D32"), Color.parseColor("#FBC02D")});
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        
        // Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Người dùng", "Tổng sao"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }
}
