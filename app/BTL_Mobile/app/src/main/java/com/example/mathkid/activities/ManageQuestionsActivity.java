package com.example.mathkid.activities;

import android.content.Intent;
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
import com.example.mathkid.model.Question;

import java.util.List;

public class ManageQuestionsActivity extends AppCompatActivity {

    private RecyclerView rvQuestions;
    private UserDAO userDAO;
    private QuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_questions);

        userDAO = new UserDAO(this);
        rvQuestions = findViewById(R.id.rvQuestions);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnAddQuestion = findViewById(R.id.btnAddQuestion);

        btnBack.setOnClickListener(v -> finish());
        btnAddQuestion.setOnClickListener(v -> {
            startActivity(new Intent(this, AddQuestionActivity.class));
        });

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuestions();
    }

    private void loadQuestions() {
        List<Question> questions = userDAO.getAllQuestions();
        adapter = new QuestionAdapter(questions);
        rvQuestions.setAdapter(adapter);
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
        private List<Question> questionList;

        public QuestionAdapter(List<Question> questionList) {
            this.questionList = questionList;
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
            ImageView btnDelete;
            TextView txtQuestionText, txtType, txtAnswer;

            public QuestionViewHolder(@NonNull View itemView) {
                super(itemView);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                txtQuestionText = itemView.findViewById(R.id.txtQuestionText);
                txtType = itemView.findViewById(R.id.txtType);
                txtAnswer = itemView.findViewById(R.id.txtAnswer);
            }
        }
    }
}
