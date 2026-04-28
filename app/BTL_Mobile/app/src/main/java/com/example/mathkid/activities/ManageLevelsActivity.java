package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Lesson;
import com.example.mathkid.model.Question;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageLevelsActivity extends AppCompatActivity {

    private RecyclerView rvLevels;
    private UserDAO userDAO;
    private LevelAdapter adapter;
    private FloatingActionButton fabAddLevel;
    private List<Lesson> allLevels = new ArrayList<>();

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_levels);

        userDAO = new UserDAO(this);
        rvLevels = findViewById(R.id.rvLevels);
        ImageView btnBack = findViewById(R.id.btnBack);
        fabAddLevel = findViewById(R.id.fabAddLevel);
        EditText edtSearch = findViewById(R.id.edtSearch);

        btnBack.setOnClickListener(v -> finish());
        fabAddLevel.setOnClickListener(v -> showLevelWithQuestionsDialog(null));

        rvLevels.setLayoutManager(new LinearLayoutManager(this));
        
        loadLevels();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLevels(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadLevels() {
        allLevels = userDAO.getAllActivitiesForAdmin();
        count = allLevels.size();
        adapter = new LevelAdapter(new ArrayList<>(allLevels));
        rvLevels.setAdapter(adapter);
    }

    private void filterLevels(String query) {
        List<Lesson> filteredList = new ArrayList<>();
        for (Lesson lesson : allLevels) {
            if (lesson.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(lesson);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void showLevelWithQuestionsDialog(Lesson lesson) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(lesson == null ? "Thêm cấp độ mới" : "Sửa cấp độ");
        
        View view = getLayoutInflater().inflate(R.layout.dialog_add_level_with_questions, null);
        EditText edtTitle = view.findViewById(R.id.edtLevelTitle);
        EditText edtOrder = view.findViewById(R.id.edtOrderIndex);
        Spinner spType = view.findViewById(R.id.spGameType);
        RecyclerView rvSelectQuestions = view.findViewById(R.id.rvSelectQuestions);

        // Setup Spinner
        String[] types = {"quiz", "drag", "matching", "comparison"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        // Questions Logic
        List<Question> displayQuestions = new ArrayList<>();
        List<Integer> initiallySelectedIds = new ArrayList<>();

        if (lesson != null) {
            edtTitle.setText(lesson.getTitle());
            edtOrder.setText(String.valueOf(lesson.getOrderIndex()));
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(lesson.getIcon())) {
                    spType.setSelection(i);
                    break;
                }
            }
            
            List<Question> currentQuestions = userDAO.getQuestions(lesson.getId());
            for (Question q : currentQuestions) {
                displayQuestions.add(q);
                initiallySelectedIds.add(q.getId());
            }
        }
        
        displayQuestions.addAll(userDAO.getUnassignedQuestions());

        QuestionSelectionAdapter qAdapter = new QuestionSelectionAdapter(displayQuestions, initiallySelectedIds);
        rvSelectQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvSelectQuestions.setAdapter(qAdapter);

        builder.setView(view);
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String title = edtTitle.getText().toString().trim();
            String orderStr = edtOrder.getText().toString().trim();
            String type = spType.getSelectedItem().toString();

            if(TextUtils.isEmpty(orderStr)) {
                orderStr = String.valueOf(count + 1);
            }

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(orderStr)) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int order = Integer.parseInt(orderStr);
            long activityId;
            if (lesson == null) {
                activityId = userDAO.addActivityAdmin(title, type, order);
            } else {
                userDAO.updateActivityAdmin(lesson.getId(), title, type, order);
                activityId = lesson.getId();
            }

            if (activityId != -1) {
                userDAO.updateQuestionsForActivity((int)activityId, qAdapter.getSelectedQuestionIds());
                Toast.makeText(this, "Thành công", Toast.LENGTH_SHORT).show();
                loadLevels();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private class QuestionSelectionAdapter extends RecyclerView.Adapter<QuestionSelectionAdapter.ViewHolder> {
        private List<Question> questions;
        private List<Integer> selectedIds;

        public QuestionSelectionAdapter(List<Question> questions, List<Integer> initialSelections) {
            this.questions = questions;
            this.selectedIds = new ArrayList<>(initialSelections);
        }

        public List<Integer> getSelectedQuestionIds() {
            return selectedIds;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Question q = questions.get(position);
            holder.text.setText(q.getText() + " [" + q.getType() + "]");
            
            updateBackground(holder.itemView, selectedIds.contains(q.getId()));

            holder.itemView.setOnClickListener(v -> {
                if (selectedIds.contains(q.getId())) {
                    selectedIds.remove(Integer.valueOf(q.getId()));
                    updateBackground(holder.itemView, false);
                } else {
                    selectedIds.add(q.getId());
                    updateBackground(holder.itemView, true);
                }
            });
        }

        private void updateBackground(View view, boolean isSelected) {
            view.setBackgroundColor(isSelected ? 0x334CAF50 : 0);
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    private class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {
        private List<Lesson> levelList;

        public LevelAdapter(List<Lesson> levelList) {
            this.levelList = levelList;
        }

        public void updateList(List<Lesson> newList) {
            this.levelList = newList;
            notifyDataSetChanged();
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
            holder.txtType.setText("Loại: " + lesson.getIcon());
            holder.txtOrder.setText("Thứ tự: " + lesson.getOrderIndex());

            holder.btnViewQuestions.setOnClickListener(v -> {
                Intent intent = new Intent(ManageLevelsActivity.this, ManageQuestionsActivity.class);
                intent.putExtra("ACTIVITY_ID", lesson.getId());
                intent.putExtra("ACTIVITY_TITLE", lesson.getTitle());
                startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageLevelsActivity.this)
                        .setTitle("Xóa cấp độ")
                        .setMessage("Bạn có chắc chắn muốn xóa cấp độ này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (userDAO.deleteActivityAdmin(lesson.getId())) {
                                loadLevels();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });

            holder.btnEdit.setOnClickListener(v -> showLevelWithQuestionsDialog(lesson));
        }

        @Override
        public int getItemCount() {
            return levelList.size();
        }

        class LevelViewHolder extends RecyclerView.ViewHolder {
            ImageView btnEdit, btnDelete, btnViewQuestions;
            TextView txtLevelTitle, txtType, txtOrder;

            public LevelViewHolder(@NonNull View itemView) {
                super(itemView);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnViewQuestions = itemView.findViewById(R.id.btnViewQuestions);
                txtLevelTitle = itemView.findViewById(R.id.txtLevelTitle);
                txtType = itemView.findViewById(R.id.txtType);
                txtOrder = itemView.findViewById(R.id.txtOrder);
            }
        }
    }
}
