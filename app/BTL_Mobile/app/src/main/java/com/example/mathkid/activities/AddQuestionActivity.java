package com.example.mathkid.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Lesson;
import com.example.mathkid.model.Question;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddQuestionActivity extends AppCompatActivity {

    private Spinner spinnerActivity, spinnerType;
    private EditText edtQuestionText, edtImage, edtAnswer, edtOptions;
    private ImageView imgPreview;
    private View btnSelectImage;
    private Button btnSave;
    private UserDAO userDAO;
    private List<Lesson> activities;
    private int editingQuestionId = -1;
    private int presetActivityId = -1;
    private String base64Image = "";

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (imgPreview != null) imgPreview.setImageBitmap(bitmap);
                        
                        // Chuyển sang Base64 NO_WRAP để SQLite lưu trữ gọn sạch
                        base64Image = encodeImage(bitmap);
                        if (edtImage != null) edtImage.setText("image_uploaded");
                        Toast.makeText(this, "Đã chọn ảnh thành công!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("AddQuestion", "Lỗi khi xử lý ảnh", e);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_question);

        userDAO = new UserDAO(this);

        spinnerActivity = findViewById(R.id.spinnerActivity);
        spinnerType = findViewById(R.id.spinnerType);
        edtQuestionText = findViewById(R.id.edtQuestionText);
        edtImage = findViewById(R.id.edtImage);
        imgPreview = findViewById(R.id.imgPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        edtAnswer = findViewById(R.id.edtAnswer);
        edtOptions = findViewById(R.id.edtOptions);
        btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView txtTitle = findViewById(R.id.txtTitle);

        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        presetActivityId = getIntent().getIntExtra("PRESET_ACTIVITY_ID", -1);
        setupSpinners();

        if (getIntent().hasExtra("edit_question_id")) {
            editingQuestionId = getIntent().getIntExtra("edit_question_id", -1);
            loadQuestionData();
            if (btnSave != null) btnSave.setText("Cập nhật câu hỏi");
            if (txtTitle != null) txtTitle.setText("Sửa câu hỏi");
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveQuestion());
        }
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    private void setupSpinners() {
        activities = userDAO.getAllActivitiesForAdmin();
        List<String> activityTitles = new ArrayList<>();
        int selectedIndex = 0;
        for (int i = 0; i < activities.size(); i++) {
            Lesson l = activities.get(i);
            activityTitles.add(l.getTitle());
            if (l.getId() == presetActivityId) selectedIndex = i;
        }
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activityTitles);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerActivity != null) {
            spinnerActivity.setAdapter(activityAdapter);
            if (presetActivityId != -1) spinnerActivity.setSelection(selectedIndex);
        }

        String[] types = {"quiz", "drag", "comparison", "matching"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinnerType != null) spinnerType.setAdapter(typeAdapter);
    }

    private void loadQuestionData() {
        List<Question> allQuestions = userDAO.getAllQuestions();
        for (Question q : allQuestions) {
            if (q.getId() == editingQuestionId) {
                if (edtQuestionText != null) edtQuestionText.setText(q.getText());
                if (edtAnswer != null) edtAnswer.setText(q.getAnswer());
                if (edtOptions != null && q.getOptions() != null) {
                    edtOptions.setText(String.join(", ", q.getOptions()));
                }
                
                if (q.getImage() != null && !q.getImage().isEmpty()) {
                    base64Image = q.getImage();
                    if (base64Image.length() > 100) {
                        try {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            if (imgPreview != null) imgPreview.setImageBitmap(decodedByte);
                        } catch (Exception e) { e.printStackTrace(); }
                    } else {
                        int resId = getResources().getIdentifier(base64Image, "drawable", getPackageName());
                        if (resId != 0 && imgPreview != null) imgPreview.setImageResource(resId);
                    }
                }

                for (int i = 0; i < activities.size(); i++) {
                    if (activities.get(i).getId() == q.getActivityId()) {
                        if (spinnerActivity != null) spinnerActivity.setSelection(i);
                        break;
                    }
                }
                String[] types = {"quiz", "drag", "comparison", "matching"};
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equals(q.getType())) {
                        if (spinnerType != null) spinnerType.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void saveQuestion() {
        String text = edtQuestionText != null ? edtQuestionText.getText().toString().trim() : "";
        String answer = edtAnswer != null ? edtAnswer.getText().toString().trim() : "";
        String optionsStr = edtOptions != null ? edtOptions.getText().toString().trim() : "";

        if (text.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ nội dung!", Toast.LENGTH_SHORT).show();
            return;
        }

        int activityId = activities.get(spinnerActivity.getSelectedItemPosition()).getId();
        String type = spinnerType.getSelectedItem().toString();

        JSONArray optionsJson = new JSONArray();
        if (!optionsStr.isEmpty()) {
            String[] parts = optionsStr.split(",");
            for (String part : parts) optionsJson.put(part.trim());
        }

        boolean success;
        if (editingQuestionId == -1) {
            success = userDAO.addQuestion(activityId, type, text, base64Image, answer, optionsJson.toString());
        } else {
            success = userDAO.updateQuestion(editingQuestionId, activityId, type, text, base64Image, answer, optionsJson.toString());
        }

        if (success) {
            Toast.makeText(this, "Thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
        }
    }
}
