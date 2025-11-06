package com.example.volnet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // UI components
    LinearLayout btnCreateMatch, btnTeamManagement, btnMatchHistory, btnRules, btnScoring, btnMatches;
    FloatingActionButton fabAdd;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ----------------------------------
        // Initialize all buttons
        // ----------------------------------
        btnCreateMatch = findViewById(R.id.btn_create_match);
        btnTeamManagement = findViewById(R.id.btn_team_management);
        btnMatchHistory = findViewById(R.id.btn_match_history);
        btnRules = findViewById(R.id.btn_rules);
//        btnScoring = findViewById(R.id.btn_scoring);
//        btnMatches = findViewById(R.id.btn_matches);
        fabAdd = findViewById(R.id.fab_add);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // ----------------------------------
        // CARD BUTTON CLICKS
        // ----------------------------------

        // Create Match
        btnCreateMatch.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, create_match_activity.class);
            startActivity(intent);
        });

        // Team Management
        btnTeamManagement.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, manage_team_activity.class);
            startActivity(intent);
        });

        // Match History
        btnMatchHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Rules
        btnRules.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RulesActivity.class);
            startActivity(intent);
        });

//        // ✅ Scoring Button
//        btnScoring.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, createMatch2.class);
//            startActivity(intent);
//        });

        // ✅ Matches Button
//        btnMatches.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, Matches_activity.class);
//            startActivity(intent);
//        });

        // ----------------------------------
        // FLOATING BUTTON CLICK
        // ----------------------------------
        fabAdd.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, AddTeamActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // smooth animation
            } catch (Exception e) {
                Log.e("MainActivity", "Error opening AddTeamActivity", e);
            }
        });

        // ----------------------------------
        // BOTTOM NAVIGATION BAR HANDLER
        // ----------------------------------
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Default selected

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Already in home
                    return true;
                } else if (id == R.id.nav_teams) {
                    startActivity(new Intent(MainActivity.this, manage_team_activity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_history) {
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_rules) {
                    startActivity(new Intent(MainActivity.this, RulesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }
}
