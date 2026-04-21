package com.example.mathkid.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Lesson;
import com.example.mathkid.model.Question;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AddQuestionActivity extends AppCompatActivity {

    private Spinner spinnerActivity, spinnerType;
    private EditText edtQuestionText, edtImage, edtAnswer, edtOptions;
    private UserDAO userDAO;
    private List<Lesson> activities;
    private int editingQuestionId = -1;
    private int presetActivityId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        userDAO = new UserDAO(this);

        spinnerActivity = findViewById(R.id.spinnerActivity);
        spinnerType = findViewById(R.id.spinnerType);
        edtQuestionText = findViewById(R.id.edtQuestionText);
        edtImage = findViewById(R.id.edtImage);
        edtAnswer = findViewById(R.id.edtAnswer);
        edtOptions = findViewById(R.id.edtOptions);
        Button btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtTitle = findViewById(R.id.txtTitle);

        btnBack.setOnClickListener(v -> finish());

        presetActivityId = getIntent().getIntExtra("PRESET_ACTIVITY_ID", -1);
        setupSpinners();

        // Check if editing
        if (getIntent().hasExtra("edit_question_id")) {
            editingQuestionId = getIntent().getIntExtra("edit_question_id", -1);
            loadQuestionData();
            btnSave.setText("Cập nhật");
            txtTitle.setText("Sửa câu hỏi");
        } else if (presetActivityId != -1) {
            txtTitle.setText("Thêm câu hỏi mới");
        }

        btnSave.setOnClickListener(v -> saveQuestion());
    }

    private void setupSpinners() {
        activities = userDAO.getAllActivitiesForAdmin();
        List<String> activityTitles = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < activities.size(); i++) {
            Lesson l = activities.get(i);
            activityTitles.add(l.getTitle());
            if (l.getId() == presetActivityId) {
                selectedIndex = i;
            }
        }
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activityTitles);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);
        
        if (presetActivityId != -1) {
            spinnerActivity.setSelection(selectedIndex);
        }

        String[] types = {"quiz", "drag", "comparison", "matching"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void loadQuestionData() {
        // Find the question in the list
        List<Question> allQuestions = userDAO.getAllQuestions();
        for (Question q : allQuestions) {
            if (q.getId() == editingQuestionId) {
                edtQuestionText.setText(q.getText());
                edtImage.setText(q.getImage());
                edtAnswer.setText(q.getAnswer());
                
                if (q.getOptions() != null) {
                    edtOptions.setText(String.join(", ", q.getOptions()));
                }

                // Select activity
                for (int i = 0; i < activities.size(); i++) {
                    if (activities.get(i).getId() == q.getActivityId()) {
                        spinnerActivity.setSelection(i);
                        break;
                    }
                }

                // Select type
                String[] types = {"quiz", "drag", "comparison", "matching"};
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equals(q.getType())) {
                        spinnerType.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void saveQuestion() {
        String text = edtQuestionText.getText().toString().trim();
        String image = edtImage.getText().toString().trim();
        String answer = edtAnswer.getText().toString().trim();
        String optionsStr = edtOptions.getText().toString().trim();

        if (text.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung và đáp án", Toast.LENGTH_SHORT).show();
            return;
        }

        int activityId = activities.get(spinnerActivity.getSelectedItemPosition()).getId();
        String type = spinnerType.getSelectedItem().toString();

        JSONArray optionsJson = new JSONArray();
        if (!optionsStr.isEmpty()) {
            String[] parts = optionsStr.split(",");
            for (String part : parts) {
                optionsJson.put(part.trim());
            }
        }

        boolean success;
        if (editingQuestionId == -1) {
            success = userDAO.addQuestion(activityId, type, text, image, answer, optionsJson.toString());
        } else {
            success = userDAO.updateQuestion(editingQuestionId, activityId, type, text, image, answer, optionsJson.toString());
        }

        if (success) {
            Toast.makeText(this, editingQuestionId == -1 ? "Đã thêm câu hỏi!" : "Đã cập nhật câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }
}
