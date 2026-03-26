package com.example.mathkid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.mathkid.database.UserDAO;

public class Register extends AppCompatActivity {

    ImageView imgFox, imgDog, imgRabbit, imgPanda, imgPig, imgCat;
    TextView txtSelected, txtLogin;
    EditText edtUsername, edtPassword, edtConfirm;
    Button btnRegister;
    UserDAO userDAO;

    String selectedAvatar = "Fox";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cài đặt Splash Screen trước khi gọi super.onCreate
        SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDAO = new UserDAO(this);

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
        // Danh sách tất cả các ImageView avatar
        ImageView[] avatars = {imgFox, imgDog, imgRabbit, imgPanda, imgCat, imgPig};

        for (ImageView img : avatars) {
            if (img != null) {
                // Reset về trạng thái bình thường
                img.setBackgroundResource(R.drawable.avatar_normal);
                img.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
            }
        }

        // Làm nổi bật ảnh được chọn
        selectedImg.setBackgroundResource(R.drawable.avatar_selected);
        // Phóng to ảnh lên 1.2 lần mượt mà
        selectedImg.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();

        selectedAvatar = name;
        if (txtSelected != null) {
            txtSelected.setText("Đã chọn: " + name);
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

        // Kiểm tra username tồn tại
        if (userDAO.isUsernameExists(username)) {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu vào database
        boolean success = userDAO.registerUser(username, password, selectedAvatar);

        if (success) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            // Chuyển sang màn hình Login
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại. Thử lại sau!", Toast.LENGTH_SHORT).show();
        }
    }
    // Ẩn bàn phím khi chạm ra ngoài EditText
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE)
                && view instanceof android.widget.EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int[] scrcoords = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
