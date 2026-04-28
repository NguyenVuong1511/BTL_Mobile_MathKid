package com.example.mathkid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.mathkid.model.Question;

import java.util.ArrayList;
import java.util.List;

public class ManageQuestionsActivity extends AppCompatActivity {

    private RecyclerView rvQuestions;
    private UserDAO userDAO;
    private QuestionAdapter adapter;
    private int filterActivityId = -1;
    private List<Question> allQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_questions);

        userDAO = new UserDAO(this);
        rvQuestions = findViewById(R.id.rvQuestions);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnAddQuestion = findViewById(R.id.btnAddQuestion);
        TextView txtTitle = findViewById(R.id.txtTitle);
        EditText edtSearch = findViewById(R.id.edtSearch);

        filterActivityId = getIntent().getIntExtra("ACTIVITY_ID", -1);
        String activityTitle = getIntent().getStringExtra("ACTIVITY_TITLE");

        if (filterActivityId != -1 && activityTitle != null) {
            txtTitle.setText("Câu hỏi: " + activityTitle);
        }

        btnBack.setOnClickListener(v -> finish());
        btnAddQuestion.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddQuestionActivity.class);
            if (filterActivityId != -1) {
                intent.putExtra("PRESET_ACTIVITY_ID", filterActivityId);
            }
            startActivity(intent);
        });

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterQuestions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuestions();
    }

    private void loadQuestions() {
        if (filterActivityId != -1) {
            allQuestions = userDAO.getQuestions(filterActivityId);
        } else {
            allQuestions = userDAO.getAllQuestions();
        }
        adapter = new QuestionAdapter(new ArrayList<>(allQuestions));
        rvQuestions.setAdapter(adapter);
    }

    private void filterQuestions(String query) {
        List<Question> filteredList = new ArrayList<>();
        for (Question q : allQuestions) {
            if (q.getText().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(q);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
        private List<Question> questionList;

        public QuestionAdapter(List<Question> questionList) {
            this.questionList = questionList;
        }

        public void updateList(List<Question> newList) {
            this.questionList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_question, parent, false);
            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            Question question = questionList.get(position);
            holder.txtQuestionText.setText(question.getText());
            holder.txtType.setText(question.getType());
            holder.txtAnswer.setText("Đáp án: " + question.getAnswer());

            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ManageQuestionsActivity.this, AddQuestionActivity.class);
                intent.putExtra("edit_question_id", question.getId());
                startActivity(intent);
            });

            holder.btnPreview.setOnClickListener(v -> {
                Intent intent = new Intent(ManageQuestionsActivity.this, QuizActivity.class);
                intent.putExtra("IS_PREVIEW", true);
                intent.putExtra("PREVIEW_QUESTION_ID", question.getId());
                intent.putExtra("activity_id", question.getActivityId());
                startActivity(intent);
            });

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageQuestionsActivity.this)
                        .setTitle("Xóa câu hỏi")
                        .setMessage("Bạn có chắc chắn muốn xóa câu hỏi này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (userDAO.deleteQuestion(question.getId())) {
                                Toast.makeText(ManageQuestionsActivity.this, "Đã xóa câu hỏi", Toast.LENGTH_SHORT).show();
                                loadQuestions();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return questionList.size();
        }

        class QuestionViewHolder extends RecyclerView.ViewHolder {
            ImageView btnEdit, btnDelete, btnPreview;
            TextView txtQuestionText, txtType, txtAnswer;

            public QuestionViewHolder(@NonNull View itemView) {
                super(itemView);
                btnPreview = itemView.findViewById(R.id.btnPreview);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                txtQuestionText = itemView.findViewById(R.id.txtQuestionText);
                txtType = itemView.findViewById(R.id.txtType);
                txtAnswer = itemView.findViewById(R.id.txtAnswer);
            }
        }
    }
}
