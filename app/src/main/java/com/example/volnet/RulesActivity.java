package com.example.volnet;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RulesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure you use the correct layout name here
        setContentView(R.layout.activity_rules);

        // 1. Setup the Back Arrow listener
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            // Create an intent to navigate back to MainActivity
            Intent intent = new Intent(RulesActivity.this, MainActivity.class);
            // Start MainActivity
            startActivity(intent);
            // Optionally, finish the current activity to remove it from the back stack
            finish();
        });
        // 2. You can optionally load the rules dynamically here,
        // but for now, the rules are hardcoded in the XML.

        // 3. (Optional) Set up Bottom Navigation here if you include it.
        // If the bottom nav is included in this layout, ensure it's selected.

        // ----------------------------------
// BOTTOM NAVIGATION BAR HANDLER
// ----------------------------------
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

// Highlight the current tab (Rules)
        bottomNavigationView.setSelectedItemId(R.id.nav_rules);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(RulesActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_teams) {
                    startActivity(new Intent(RulesActivity.this, manage_team_activity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_history) {
                    startActivity(new Intent(RulesActivity.this, HistoryActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_rules) {
                    // Already in Rules page
                    return true;
                }

                return false;
            }
        });

    }
}

