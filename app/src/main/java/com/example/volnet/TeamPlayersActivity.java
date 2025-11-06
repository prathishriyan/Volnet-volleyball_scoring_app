package com.example.volnet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TeamPlayersActivity extends AppCompatActivity {

    private ImageView teamLogo, backButton;
    private TextView teamName;
    private RecyclerView playerRecyclerView;
    private PlayerAdapter playerAdapter;
    private Team selectedTeam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_players);

        // --- Initialize Views ---
        teamLogo = findViewById(R.id.team_logo);
        teamName = findViewById(R.id.team_name);
        playerRecyclerView = findViewById(R.id.recycler_players);
        backButton = findViewById(R.id.back_button);

        // --- Get Team from Intent ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("team")) {
            selectedTeam = (Team) intent.getSerializableExtra("team");

        }

        if (selectedTeam == null) {
            Toast.makeText(this, "Error: Team not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Set Team Info ---
        teamName.setText(selectedTeam.getName());

        if (selectedTeam.getLogo() != null && !selectedTeam.getLogo().isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(selectedTeam.getLogo()))
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(teamLogo);
        } else {
            teamLogo.setImageResource(R.drawable.logo);
        }

        // --- Setup RecyclerView ---
        List<String> playerList = selectedTeam.getPlayers();
        playerAdapter = new PlayerAdapter(playerList);
        playerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playerRecyclerView.setAdapter(playerAdapter);

        // --- Back Button ---
        backButton.setOnClickListener(v -> finish());
    }
}
