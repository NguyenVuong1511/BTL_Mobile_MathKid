package com.example.mathkid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cần phải gọi installSplashScreen TRƯỚC super.onCreate
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtRegister = findViewById(R.id.txtRegister);
        View btnBack = findViewById(R.id.btnBack);

        // Đổi màu chỉ chữ "Đăng ký ngay" thành màu cam
        if (txtRegister != null) {
            String fullText = "Bạn chưa có tài khoản? Đăng ký ngay";
            SpannableString spannableString = new SpannableString(fullText);

            int start = fullText.indexOf("Đăng ký ngay");
            int end = start + "Đăng ký ngay".length();

            if (start != -1) {
                // Màu cam (#FF9800)
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF9800")),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                // In đậm
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            txtRegister.setText(spannableString);

            txtRegister.setOnClickListener(v -> {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                // Xử lý logic đăng nhập ở đây
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}