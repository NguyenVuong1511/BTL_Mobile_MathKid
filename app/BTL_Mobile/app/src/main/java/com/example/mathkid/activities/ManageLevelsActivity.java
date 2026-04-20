package com.example.mathkid.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Lesson;

import java.util.List;

public class ManageLevelsActivity extends AppCompatActivity {

    private RecyclerView rvLevels;
    private UserDAO userDAO;
    private LevelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_levels);

        userDAO = new UserDAO(this);
        rvLevels = findViewById(R.id.rvLevels);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvLevels.setLayoutManager(new LinearLayoutManager(this));
        loadLevels();
    }

    private void loadLevels() {
        // Using getLessonsWithProgress with a dummy user ID or creating a new method in DAO
        // For admin, we can just get all activities
        List<Lesson> levels = userDAO.getAllActivitiesForAdmin();
        adapter = new LevelAdapter(levels);
        rvLevels.setAdapter(adapter);
    }

    private class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {
        private List<Lesson> levelList;

        public LevelAdapter(List<Lesson> levelList) {
            this.levelList = levelList;
        }

        @NonNull
        @Override
        public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_level, parent, false);
            return new LevelViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
            Lesson lesson = levelList.get(position);
            holder.txtLevelTitle.setText(lesson.getTitle());
            holder.txtType.setText("ID: " + lesson.getId());
            holder.txtOrder.setText("Thứ tự: " + (position + 1));

            holder.btnDelete.setOnClickListener(v -> {
                Toast.makeText(ManageLevelsActivity.this, "Tính năng xóa cấp độ đang được phát triển", Toast.LENGTH_SHORT).show();
            });

            holder.btnEdit.setOnClickListener(v -> {
                Toast.makeText(ManageLevelsActivity.this, "Tính năng sửa cấp độ đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return levelList.size();
        }

        class LevelViewHolder extends RecyclerView.ViewHolder {
            ImageView btnEdit, btnDelete;
            TextView txtLevelTitle, txtType, txtOrder;

            public LevelViewHolder(@NonNull View itemView) {
                super(itemView);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                txtLevelTitle = itemView.findViewById(R.id.txtLevelTitle);
                txtType = itemView.findViewById(R.id.txtType);
                txtOrder = itemView.findViewById(R.id.txtOrder);
            }
        }
    }
}
