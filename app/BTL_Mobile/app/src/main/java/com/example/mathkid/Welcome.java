package com.example.mathkid;

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

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        iconPencil.startAnimation(animationFloating);
        iconBook.startAnimation(animationFloating);
        iconBallon.startAnimation(animationFloating);
        iconPaint.startAnimation(animationFloating);
        iconStar.startAnimation(animationSpin);



        // Tạo animation xoay quanh vòng tròn cho icon_star
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

        TextView title = findViewById(R.id.titleWelcome);

        SpannableString text = new SpannableString("CHÀO MỪNG ĐẾN VỚI MATHKIDS!");

        text.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")),
                0, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setSpan(new ForegroundColorSpan(Color.parseColor("#FF9800")),
                18, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        title.setText(text);

        // Xử lý sự kiện nhấn nút Login và Register
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, Login.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, Register.class);
            startActivity(intent);
        });
    }
}
