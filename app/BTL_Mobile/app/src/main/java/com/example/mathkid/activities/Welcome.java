package com.example.mathkid.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mathkid.R;
import com.example.mathkid.database.SessionManager;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // KIỂM TRA ĐĂNG NHẬP TỰ ĐỘNG
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(Welcome.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);


        // Lấy Icon từ Layout
        ImageView iconStar = findViewById(R.id.icon_star);
        ImageView iconPencil = findViewById(R.id.icon_pencil);
        ImageView iconBallon = findViewById(R.id.icon_ballon);
        ImageView iconBook = findViewById(R.id.icon_book);
        ImageView iconPaint = findViewById(R.id.icon_paint);

        // Load Animation Floating (Hoạt ảnh bay bồng bềnh)
        Animation animationFloating = AnimationUtils.loadAnimation(this, R.anim.floating);
        Animation animationSpin = AnimationUtils.loadAnimation(this, R.anim.spin);


        // Bắt đầu chạy hoạt ảnh cho pencil
        if (iconPencil != null) iconPencil.startAnimation(animationFloating);
        if (iconBook != null) iconBook.startAnimation(animationFloating);
        if (iconBallon != null) iconBallon.startAnimation(animationFloating);
        if (iconPaint != null) iconPaint.startAnimation(animationFloating);
        if (iconStar != null) iconStar.startAnimation(animationSpin);



        // Tạo animation xoay quanh vòng tròn cho icon_star
        if (iconStar != null) {
            ValueAnimator orbitAnimator = ValueAnimator.ofFloat(0f, 360f);
            orbitAnimator.setDuration(10000); // 10 giây cho một vòng
            orbitAnimator.setRepeatCount(ValueAnimator.INFINITE);
            orbitAnimator.setInterpolator(new LinearInterpolator());
            orbitAnimator.addUpdateListener(animation -> {
                float angle = (float) animation.getAnimatedValue();
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) iconStar.getLayoutParams();
                layoutParams.circleAngle = angle;
                iconStar.setLayoutParams(layoutParams);
            });
            orbitAnimator.start();
        }

        TextView title = findViewById(R.id.titleWelcome);

        if (title != null) {
            SpannableString text = new SpannableString("CHÀO MỪNG ĐẾN VỚI MATHKIDS!");
            text.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")),
                    0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(Color.parseColor("#FF9800")),
                    18, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            title.setText(text);
        }

        // Xử lý sự kiện nhấn nút Login và Register
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                Intent intent = new Intent(Welcome.this, Login.class);
                startActivity(intent);
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                Intent intent = new Intent(Welcome.this, Register.class);
                startActivity(intent);
            });
        }
    }
}
