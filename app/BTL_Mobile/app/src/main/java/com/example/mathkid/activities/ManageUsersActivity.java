package com.example.mathkid.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserDAO userDAO;
    private UserAdapter adapter;
    private List<UserDAO.UserData> allUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_users);

        userDAO = new UserDAO(this);
        rvUsers = findViewById(R.id.rvUsers);
        ImageView btnBack = findViewById(R.id.btnBack);
        EditText edtSearch = findViewById(R.id.edtSearch);

        btnBack.setOnClickListener(v -> finish());

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        
        loadUsers();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        allUsers = userDAO.getAllUsers();
        adapter = new UserAdapter(new ArrayList<>(allUsers));
        rvUsers.setAdapter(adapter);
    }

    private void filterUsers(String query) {
        List<UserDAO.UserData> filteredList = new ArrayList<>();
        for (UserDAO.UserData user : allUsers) {
            if (user.username.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(user);
            }
        }
        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<UserDAO.UserData> userList;

        public UserAdapter(List<UserDAO.UserData> userList) {
            this.userList = userList;
        }

        public void updateList(List<UserDAO.UserData> newList) {
            this.userList = newList;
            notifyDataSetChanged();
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
