package com.example.mathkid.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mathkid.R;
import com.example.mathkid.adapter.AchievementAdapter;
import com.example.mathkid.database.UserDAO;
import com.example.mathkid.model.Achievement;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AchievementManagementActivity extends AppCompatActivity {

    private RecyclerView rvAchievements;
    private AchievementAdapter adapter;
    private UserDAO userDAO;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivCurrentPreview;
    private String selectedImageBase64 = "";
    private List<Achievement> allAchievements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_management);

        userDAO = new UserDAO(this);
        rvAchievements = findViewById(R.id.rvAchievements);
        rvAchievements.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAdd).setOnClickListener(v -> showAddEditDialog(null));

        EditText edtSearch = findViewById(R.id.edtSearch);
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAchievements(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadAchievements();
    }

    private void loadAchievements() {
        allAchievements = userDAO.getAllAchievementsAdmin();
        if (adapter == null) {
            adapter = new AchievementAdapter(this, new ArrayList<>(allAchievements), new AchievementAdapter.OnAchievementListener() {
                @Override
                public void onEdit(Achievement achievement) {
                    showAddEditDialog(achievement);
                }

                @Override
                public void onDelete(Achievement achievement) {
                    new AlertDialog.Builder(AchievementManagementActivity.this)
                            .setTitle("Xóa thành tích")
                            .setMessage("Bạn có chắc chắn muốn xóa thành tích này?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                if (userDAO.deleteAchievementAdmin(achievement.id)) {
                                    Toast.makeText(AchievementManagementActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                    loadAchievements();
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            });
            rvAchievements.setAdapter(adapter);
        } else {
            adapter.updateList(allAchievements);
        }
    }

    private void filterAchievements(String query) {
        List<Achievement> filteredList = new ArrayList<>();
        for (Achievement achievement : allAchievements) {
            if (achievement.title.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(achievement);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private void showAddEditDialog(@Nullable Achievement achievement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_achievement, null);
        builder.setView(view);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        ivCurrentPreview = view.findViewById(R.id.ivIconPreview);
        Button btnSelectIcon = view.findViewById(R.id.btnSelectIcon);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etDescription = view.findViewById(R.id.etDescription);
        Spinner spType = view.findViewById(R.id.spType);
        EditText etRequiredValue = view.findViewById(R.id.etRequiredValue);
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        String[] types = {"lesson_count", "total_stars", "xp_milestone"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(spinnerAdapter);

        if (achievement != null) {
            tvDialogTitle.setText("Sửa Thành Tích");
            etTitle.setText(achievement.title);
            etDescription.setText(achievement.description);
            etRequiredValue.setText(String.valueOf(achievement.requiredValue));
            selectedImageBase64 = achievement.icon;
            
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(achievement.type)) {
                    spType.setSelection(i);
                    break;
                }
            }

            if (achievement.icon != null && !achievement.icon.isEmpty()) {
                if (achievement.icon.startsWith("ic_")) {
                    int resId = getResources().getIdentifier(achievement.icon, "drawable", getPackageName());
                    if (resId != 0) ivCurrentPreview.setImageResource(resId);
                } else {
                    try {
                        byte[] decodedString = Base64.decode(achievement.icon, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivCurrentPreview.setImageBitmap(decodedByte);
                    } catch (Exception e) {}
                }
            }
        } else {
            selectedImageBase64 = "";
        }

        AlertDialog dialog = builder.create();

        btnSelectIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String type = spType.getSelectedItem().toString();
            String reqValStr = etRequiredValue.getText().toString().trim();

            if (title.isEmpty() || reqValStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            int reqVal = Integer.parseInt(reqValStr);

            boolean success;
            if (achievement == null) {
                success = userDAO.addAchievementAdmin(title, desc, selectedImageBase64, type, reqVal);
            } else {
                success = userDAO.updateAchievementAdmin(achievement.id, title, desc, selectedImageBase64, type, reqVal);
            }

            if (success) {
                Toast.makeText(this, "Thành công", Toast.LENGTH_SHORT).show();
                loadAchievements();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivCurrentPreview.setImageBitmap(bitmap);
                selectedImageBase64 = encodeImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
