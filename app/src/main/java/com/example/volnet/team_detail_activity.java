package com.example.volnet;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class team_detail_activity extends AppCompatActivity {

    private ImageView teamLogo;
    private TextView teamName;
    private TextView teamPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        // --- Find Views ---
        teamLogo = findViewById(R.id.team_logo);
        teamName = findViewById(R.id.team_name);
        teamPlayers = findViewById(R.id.team_players);

        // --- Get Team from Intent ---
        Team team = getIntent().getParcelableExtra("team");
        if (team != null) {
            teamName.setText(team.getName());

            // Display team logo
            if (team.getLogo() != null && !team.getLogo().isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(team.getLogo()))
                        .placeholder(R.drawable.logo)
                        .error(R.drawable.logo)
                        .into(teamLogo);
            } else {
                teamLogo.setImageResource(R.drawable.logo);
            }

            // Display players as comma-separated string
            if (team.getPlayers() != null && !team.getPlayers().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String player : team.getPlayers()) {
                    sb.append(player).append("\n");
                }
                teamPlayers.setText(sb.toString().trim());
            } else {
                teamPlayers.setText("No players added yet");
            }
        }
    }
}
