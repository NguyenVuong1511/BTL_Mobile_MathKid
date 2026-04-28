package com.example.mathkid.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

public class EditProfileActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    UserDAO userDAO;
    SessionManager sessionManager;
    UserDAO.UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);
        
        String currentUsername = sessionManager.getUsername();
        userData = userDAO.getUserData(currentUsername);

        initViews();
    }

    private void initViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        
        if (userData != null) {
            edtUsername.setText(userData.username);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            saveChanges();
        });
    }

    private void saveChanges() {
        String newUsername = edtUsername.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem tên mới có bị trùng không (nếu thay đổi tên)
        if (!newUsername.equals(userData.username) && userDAO.isUsernameExists(newUsername)) {
            Toast.makeText(this, "Tên người dùng đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        String passwordToUpdate = newPassword.isEmpty() ? null : newPassword;
        
        boolean success = userDAO.updateUser(userData.id, newUsername, passwordToUpdate);
        
        if (success) {
            // Cập nhật lại session nếu đổi tên
            if (!newUsername.equals(userData.username)) {
                sessionManager.setLogin(true, newUsername);
            }
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }
}
