package com.example.volnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class manage_team_activity extends AppCompatActivity
        implements TeamAdapter.OnTeamDeleteListener,
        TeamAdapter.OnTeamClickListener ,
        TeamAdapter.OnTeamExploreListener,
        TeamAdapter.OnTeamEditListener {

    private RecyclerView teamRecyclerView;
    private TeamAdapter adapter;
    private List<Team> displayedTeamList; // List shown in adapter
    private List<Team> fullTeamList;      // Master list of all teams
    private TeamDatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> teamModificationLauncher;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_team);

        // --- Initialize Views ---
        ImageView backButton = findViewById(R.id.back_arrow);
        ImageView sortButton = findViewById(R.id.sort_button);
        FloatingActionButton fabAddTeam = findViewById(R.id.fab_add_team);
        teamRecyclerView = findViewById(R.id.history_recycler_view);
        searchBox = findViewById(R.id.search_box);

        // --- Initialize Database Helper ---
        dbHelper = new TeamDatabaseHelper(this);

        // --- Load Teams from Database ---
        fullTeamList = dbHelper.getAllTeams();
        displayedTeamList = new ArrayList<>(fullTeamList);

        // Setup adapter with the displayed list
        adapter = new TeamAdapter(displayedTeamList, this, this,this,this);
        teamRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        teamRecyclerView.setAdapter(adapter);

        // Setup launcher to handle results from the add team activity
        teamModificationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
//
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Team updatedTeam = (Team) result.getData().getSerializableExtra("team");
                        if (updatedTeam != null) {
                            dbHelper.updateTeam(updatedTeam);
                            refreshTeams();
                        } else {
                            refreshTeams(); // refresh list anyway after adding
                        }
                    }
                });

        // Setup search functionality
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        // Setup button clicks
        backButton.setOnClickListener(v -> {
            // Create an intent to navigate back to MainActivity
            Intent intent = new Intent(manage_team_activity.this, MainActivity.class);
            // Start MainActivity
            startActivity(intent);
            // Optionally, finish the current activity to remove it from the back stack
            finish();
        });
        sortButton.setOnClickListener(this::showSortMenu);
        fabAddTeam.setOnClickListener(v -> {
            Intent intent = new Intent(manage_team_activity.this, AddTeamActivity.class);
            teamModificationLauncher.launch(intent);
        });

        // -------------------------------
// BOTTOM NAVIGATION BAR HANDLER
// -------------------------------
        // ----------------------------------
// BOTTOM NAVIGATION BAR HANDLER
// ----------------------------------
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

// Highlight the current tab (Teams)
        bottomNavigationView.setSelectedItemId(R.id.nav_teams);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(manage_team_activity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_teams) {
                    // Already in Teams page
                    return true;

                } else if (id == R.id.nav_history) {
                    startActivity(new Intent(manage_team_activity.this, HistoryActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                else if (id == R.id.nav_rules) {
                    startActivity(new Intent(manage_team_activity.this, RulesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });

    }


    // 🔍 Filter teams by name or player
        private void filter(String text) {
        ArrayList<Team> filteredList = new ArrayList<>();
        String searchText = text.toLowerCase();
        for (Team item : fullTeamList) {
            // Check team name
            if (item.getName().toLowerCase().contains(searchText)) {
                filteredList.add(item);
                continue; // Skip to next team if already added
            }
            // Check player names
            for (String player : item.getPlayers()) {
                if (player.toLowerCase().contains(searchText)) {
                    filteredList.add(item);
                    break; // Move to next team once a player match is found
                }
            }
        }
        displayedTeamList.clear();
        displayedTeamList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    // 🔁 Refresh after add/edit/delete
    private void refreshTeams() {
        fullTeamList = dbHelper.getAllTeams();
        filter(searchBox.getText().toString());
    }

    // 🔠 Sort menu
    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_sorts_team, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_a_to_z) {
                sortTeams(true);
                return true;
            } else if (itemId == R.id.sort_z_to_a) {
                sortTeams(false);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void sortTeams(boolean ascending) {
        // Sort both lists to maintain consistency
        Collections.sort(fullTeamList, (t1, t2) ->
                ascending ? t1.getName().compareToIgnoreCase(t2.getName())
                        : t2.getName().compareToIgnoreCase(t1.getName()));
//        Collections.sort(displayedTeamList, (t1, t2) -> ascending ? t1.getName().compareToIgnoreCase(t2.getName()) : t2.getName().compareToIgnoreCase(t1.getName()));
//        adapter.notifyDataSetChanged();
//        Toast.makeText(this, "Sorted " + (ascending ? "A-Z" : "Z-A"), Toast.LENGTH_SHORT).show();
        filter(searchBox.getText().toString());
        Toast.makeText(this, "Sorted " + (ascending ? "A–Z" : "Z–A"), Toast.LENGTH_SHORT).show();
    }

    // 🗑️ Delete team with confirmation
    @Override
    public void onDelete(int position, Team team) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customLayout = getLayoutInflater().inflate(R.layout.dailog_custom_delete_confirmation, null);
        builder.setView(customLayout);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        Button deleteButton = customLayout.findViewById(R.id.dialog_delete_button);
        Button cancelButton = customLayout.findViewById(R.id.dialog_cancel_button);
        ImageView closeButton = customLayout.findViewById(R.id.dialog_close_button);

        deleteButton.setOnClickListener(v -> {
            boolean deleted = dbHelper.deleteTeam(team.getId()); // <-- delete from DB
            if (deleted) {
                fullTeamList.remove(team);   // remove from memory list
                filter(searchBox.getText().toString());
                Toast.makeText(this, "Team " + team.getName() + " deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete team", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });


        cancelButton.setOnClickListener(v -> dialog.dismiss());
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    // 👆 Regular click (optional)
    @Override
    public void onTeamClick(Team team) {
        Toast.makeText(this, "Selected: " + team.getName(), Toast.LENGTH_SHORT).show();
    }

    // 📂 Explore team players
   @Override
   public void onExplore(Team team) {
//        Intent intent = new Intent(this, TeamPlayersActivity.class);
//        intent.putExtra("team", team);
//        startActivity(intent);
   }

    // ✏️ Edit team or players
    @Override
    public void onEdit(Team team) {
        Intent intent = new Intent(this, EditTeamAcitvity.class);
        intent.putExtra("team", team);
        teamModificationLauncher.launch(intent);
    }
}