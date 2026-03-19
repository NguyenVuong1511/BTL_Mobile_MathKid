package com.example.mathkid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.mathkid.R;

public class Register extends AppCompatActivity {

    ImageView imgFox, imgDog, imgRabbit, imgPanda, imgPig, imgCat;
    TextView txtSelected, txtLogin;
    EditText edtUsername, edtPassword, edtConfirm;
    Button btnRegister;

    String selectedAvatar = "Fox";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cài đặt Splash Screen trước khi gọi super.onCreate
        SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ
        imgFox = findViewById(R.id.imgFox);
        imgDog = findViewById(R.id.imgDog);
        imgRabbit = findViewById(R.id.imgRabbit);
        imgPanda = findViewById(R.id.imgPanda);
        imgPig = findViewById(R.id.imgPig);
        imgCat = findViewById(R.id.imgCat);


        txtSelected = findViewById(R.id.txtSelected);
        txtLogin = findViewById(R.id.txtLogin);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);

        btnRegister = findViewById(R.id.btnRegister);

        // Đổi màu chỉ chữ "Đăng nhập"
        if (txtLogin != null) {
            String fullText = "Bạn đã có tài khoản? Đăng nhập";
            SpannableString spannableString = new SpannableString(fullText);
            
            // Tìm vị trí của "Đăng nhập"
            int start = fullText.indexOf("Đăng nhập");
            int end = start + "Đăng nhập".length();
            
            if (start != -1) {
                // Màu xanh lá cây
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#2E7D32")), 
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // In đậm (tùy chọn)
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            txtLogin.setText(spannableString);
        }

        // ===== CHỌN AVATAR =====
        if (imgFox != null) imgFox.setOnClickListener(v -> selectAvatar("Fox", imgFox));
        if (imgDog != null) imgDog.setOnClickListener(v -> selectAvatar("Dog", imgDog));
        if (imgRabbit != null) imgRabbit.setOnClickListener(v -> selectAvatar("Rabbit", imgRabbit));
        if (imgPanda != null) imgPanda.setOnClickListener(v -> selectAvatar("Panda", imgPanda));
        if (imgPig != null) imgPig.setOnClickListener(v -> selectAvatar("Pig", imgPig));
        if (imgCat != null) imgCat.setOnClickListener(v -> selectAvatar("Cat", imgCat));


        // ===== ĐĂNG KÝ =====
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> register());
        }

        // Chuyển sang trang đăng nhập
        if (txtLogin != null) {
            txtLogin.setOnClickListener(v -> {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish(); // Đóng trang đăng ký
            });
        }
        
        // Nút quay lại
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void selectAvatar(String name, ImageView selectedImg) {
        // Reset background về viền nhạt mặc định cho tất cả
        if (imgFox != null) imgFox.setBackgroundResource(R.drawable.avatar_normal);
        if (imgDog != null) imgDog.setBackgroundResource(R.drawable.avatar_normal);
        if (imgRabbit != null) imgRabbit.setBackgroundResource(R.drawable.avatar_normal);
        if (imgPanda != null) imgPanda.setBackgroundResource(R.drawable.avatar_normal);
        if (imgCat != null) imgCat.setBackgroundResource(R.drawable.avatar_normal);
        if (imgPig != null) imgPig.setBackgroundResource(R.drawable.avatar_normal);

        // Set viền cam đậm cho avatar được chọn
        selectedImg.setBackgroundResource(R.drawable.avatar_selected);

        selectedAvatar = name;
        if (txtSelected != null) {
            txtSelected.setText("Selected: " + name);
        }
    }

    private void register() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirm.getText().toString().trim();

        if (username.isEmpty()) {
            edtUsername.setError("Nhập username");
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Nhập password");
            return;
        }

        if (!password.equals(confirm)) {
            edtConfirm.setError("Mật khẩu không khớp");
            return;
        }

        // Thành công
        Toast.makeText(this,
                "Đăng ký thành công với avatar: " + selectedAvatar,
                Toast.LENGTH_LONG).show();
    }
}
