package com.example.mathkid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.database.SessionManager;
import com.example.mathkid.database.UserDAO;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgProfileAvatar;
    TextView txtProfileName, txtProfileXP, txtProfileLevel, txtProfileStreak;
    Button btnLogout;

    UserDAO userDAO;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        txtProfileName = findViewById(R.id.txtProfileName);
        txtProfileXP = findViewById(R.id.txtProfileXP);
        txtProfileLevel = findViewById(R.id.txtProfileLevel);
        txtProfileStreak = findViewById(R.id.txtProfileStreak);
        btnLogout = findViewById(R.id.btnLogout);

        loadProfileData();

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProfileData() {
        String username = sessionManager.getUsername();
        UserDAO.UserData data = userDAO.getUserData(username);

        if (data != null) {
            txtProfileName.setText(data.username);
            txtProfileXP.setText(String.valueOf(data.exp));
            txtProfileLevel.setText(String.valueOf(data.level));
            txtProfileStreak.setText(String.valueOf(data.streak));

            int resId = getResources().getIdentifier(data.avatar.toLowerCase(), "drawable", getPackageName());
            if (resId != 0) {
                imgProfileAvatar.setImageResource(resId);
            }
        }
    }
}
