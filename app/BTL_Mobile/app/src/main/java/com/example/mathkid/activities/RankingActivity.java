package com.example.mathkid.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mathkid.R;
import com.example.mathkid.database.UserDAO;

import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private FrameLayout btnBack;
    private LinearLayout layoutRankingList;
    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        userDAO = new UserDAO(this);

        btnBack = findViewById(R.id.btnBack);
        layoutRankingList = findViewById(R.id.layoutRankingList);

        btnBack.setOnClickListener(v -> finish());

        loadRankingData();
    }

    private void loadRankingData() {
        List<UserDAO.UserData> topUsers = userDAO.getTopUsers(10);
        layoutRankingList.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < topUsers.size(); i++) {
            UserDAO.UserData user = topUsers.get(i);
            View itemView = inflater.inflate(R.layout.item_ranking, layoutRankingList, false);

            TextView txtRank = itemView.findViewById(R.id.txtRank);
            ImageView imgAvatar = itemView.findViewById(R.id.imgAvatar);
            TextView txtName = itemView.findViewById(R.id.txtName);
            TextView txtXP = itemView.findViewById(R.id.txtXP);
            TextView txtXpLabel = itemView.findViewById(R.id.txtXpLabel);
            ImageView imgMedal = itemView.findViewById(R.id.imgMedal);

            int rank = i + 1;
            txtRank.setText(String.valueOf(rank));
            txtName.setText(user.username);
            txtXP.setText(String.valueOf(user.exp));

            // Set rank title
            if (rank == 1) {
                txtXpLabel.setText("Quán quân");
            } else if (rank <= 3) {
                txtXpLabel.setText("Hạng " + rank);
            } else {
                txtXpLabel.setText("Siêu nhân nhí");
            }

            // Load Avatar
            if (user.avatar != null && !user.avatar.isEmpty()) {
                int resId = getResources().getIdentifier(user.avatar.toLowerCase(), "drawable", getPackageName());
                if (resId != 0) {
                    imgAvatar.setImageResource(resId);
                }
            }

            // Medal for top 3
            if (rank == 1) {
                imgMedal.setImageResource(R.drawable.ic_gold_medal);
                imgMedal.setVisibility(View.VISIBLE);
                txtRank.setVisibility(View.GONE);
            } else if (rank == 2) {
                imgMedal.setImageResource(R.drawable.ic_silver_medal);
                imgMedal.setVisibility(View.VISIBLE);
                txtRank.setVisibility(View.GONE);
            } else if (rank == 3) {
                imgMedal.setImageResource(R.drawable.ic_bronze_medal);
                imgMedal.setVisibility(View.VISIBLE);
                txtRank.setVisibility(View.GONE);
            } else {
                imgMedal.setVisibility(View.GONE);
                txtRank.setVisibility(View.VISIBLE);
            }

            layoutRankingList.addView(itemView);
        }
    }
}
