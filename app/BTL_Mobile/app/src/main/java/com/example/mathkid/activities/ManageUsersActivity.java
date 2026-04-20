package com.example.mathkid.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserDAO userDAO;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        userDAO = new UserDAO(this);
        rvUsers = findViewById(R.id.rvUsers);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        loadUsers();
    }

    private void loadUsers() {
        List<UserDAO.UserData> users = userDAO.getAllUsers();
        adapter = new UserAdapter(users);
        rvUsers.setAdapter(adapter);
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<UserDAO.UserData> userList;

        public UserAdapter(List<UserDAO.UserData> userList) {
            this.userList = userList;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserDAO.UserData user = userList.get(position);
            holder.txtUsername.setText(user.username);
            holder.txtDetails.setText("Level " + user.level + " | " + user.exp + " XP | Stars: " + user.totalStars);

            if (user.avatar != null && !user.avatar.isEmpty()) {
                int resId = getResources().getIdentifier(user.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) holder.imgAvatar.setImageResource(resId);
            }

            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageUsersActivity.this)
                        .setTitle("Xóa người dùng")
                        .setMessage("Bạn có chắc chắn muốn xóa user '" + user.username + "'? Dữ liệu tiến trình sẽ mất vĩnh viễn.")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (userDAO.deleteUser(user.id)) {
                                Toast.makeText(ManageUsersActivity.this, "Đã xóa user", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            ImageView imgAvatar, btnDelete;
            TextView txtUsername, txtDetails;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                imgAvatar = itemView.findViewById(R.id.imgAvatar);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                txtUsername = itemView.findViewById(R.id.txtUsername);
                txtDetails = itemView.findViewById(R.id.txtDetails);
            }
        }
    }
}
