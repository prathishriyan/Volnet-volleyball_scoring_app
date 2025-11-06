package com.example.volnet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
//import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class AddTeamActivity extends AppCompatActivity {

    private LinearLayout playerListContainer;
    private AppCompatButton btnAddPlayers, btnSaveChanges;
    private ShapeableImageView ivTeamLogo;
    private EditText etTeamName;

    private Uri teamLogoUri = null;
    private TeamDatabaseHelper dbHelper;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_team); // Make sure this matches your layout file name

        // 🟢 Initialize views
        playerListContainer = findViewById(R.id.player_list_container);
        btnAddPlayers = findViewById(R.id.btn_add_players);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        ivTeamLogo = findViewById(R.id.iv_team_logo);
        etTeamName = findViewById(R.id.et_team_name);

        dbHelper = new TeamDatabaseHelper(this);


        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        teamLogoUri = result.getData().getData();
                        if (teamLogoUri != null) {
                            // 🟢 Persist read permission for this image
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        teamLogoUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                Log.e("AddTeamActivity", "Failed to persist URI permission", e);
                            }


                            // 🟢 Display the image immediately
                            ivTeamLogo.setImageURI(teamLogoUri);
                        }
                    }
                }
        );

        // Permission launcher
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(this, "Permission denied to access images", Toast.LENGTH_SHORT).show();
                    }
                });

        // Listeners
        ivTeamLogo.setOnClickListener(v -> requestStoragePermission());
        btnAddPlayers.setOnClickListener(v -> addSubstitutePlayer());
        btnSaveChanges.setOnClickListener(v -> validateAndSaveTeam());


        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Team was not added. Do you want to go back?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finish(); // Close the activity
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Stay on the screen
                    })
                    .show();
        });
        AppCompatButton btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Team was not added. Do you want to go back?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });




    }
    // 🟢 Request storage permission for image selection
    private void requestStoragePermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    // 🟢 Open image gallery
    private void openGallery() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("image/*");
//        imagePickerLauncher.launch(intent);
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e("AddTeamActivity", "Error opening gallery", e);
            Toast.makeText(this, "Unable to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    // Recursive helper to get all EditTexts
    private List<EditText> getAllEditTexts(View parent) {
        List<EditText> editTexts = new ArrayList<>();

        if (parent instanceof ViewGroup) { // Make sure it's a ViewGroup
            ViewGroup parentGroup = (ViewGroup) parent;

            for (int i = 0; i < parentGroup.getChildCount(); i++) {
                View child = parentGroup.getChildAt(i);

                if (child instanceof EditText) {
                    editTexts.add((EditText) child);
                } else if (child instanceof ViewGroup) {
                    editTexts.addAll(getAllEditTexts(child)); // Recursive call
                }
            }
        }

        return editTexts;
    }

    // ===============================
    // 🧩 VALIDATION & DATABASE SAVE
    // ===============================
    private void validateAndSaveTeam() {
        LinearLayout playerListContainer = findViewById(R.id.player_list_container);
        List<EditText> playerFields = getAllEditTexts(playerListContainer);

        List<String> playerNames = new ArrayList<>();
        boolean allValid = true;

        for (EditText et : playerFields) {
            String name = et.getText().toString().trim();
            if (name.isEmpty()) {
                et.setError("Player name required");
                allValid = false;
            } else {
                playerNames.add(name);
            }
        }

        if (playerNames.size() < 6) {
            Toast.makeText(this, "At least 6 players are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allValid) {
            String teamName = etTeamName.getText().toString().trim(); // <--- add this
            // Save team and players
           // returns DB id

            try {
                long teamId = dbHelper.addTeam(teamName, teamLogoUri != null ? teamLogoUri.toString() : "");
                for (String playerName : playerNames) {
                    dbHelper.addPlayer(teamId, playerName);
                }

                Team newTeam = new Team((int)teamId, teamName,teamLogoUri != null ? teamLogoUri.toString() : "", playerNames);

                Toast.makeText(this, "Team saved successfully!", Toast.LENGTH_SHORT).show();
                // Go to Manage Team activity
//                Intent intent = new Intent(this, manage_team_activity.class);
//                startActivity(intent);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("team", newTeam);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } catch (Exception e) {
                Log.e("AddTeamActivity", "Error saving team", e);
                Toast.makeText(this, "Failed to save team", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 🟢 Add substitute player dynamically
    private void addSubstitutePlayer() {
        int playerCount = 0;
        //int currentPlayers = playerListContainer.getChildCount();

       // Count only rows that contain a player name field
        for (int i = 0; i < playerListContainer.getChildCount(); i++) {
            View row = playerListContainer.getChildAt(i);
            EditText etPlayer = row.findViewById(R.id.et_player_name);
            if (etPlayer != null) {
                playerCount++;
            }
        }
        if (playerCount >= 6) {
            Toast.makeText(this, "Maximum 12 players allowed (6 main + 6 substitutes)", Toast.LENGTH_SHORT).show();
            btnAddPlayers.setEnabled(false); // Disable add button
            btnAddPlayers.setEnabled(false); // will auto-turn gray
            return; // Stop adding more players
        }
        // Inflate new player row
        View row = LayoutInflater.from(this).inflate(R.layout.player_list_item, playerListContainer, false);
        ImageView ivDelete = row.findViewById(R.id.iv_delete_player);

        ivDelete.setOnClickListener(v -> {
            playerListContainer.removeView(row);
            updateSubstituteHints();
            btnAddPlayers.setEnabled(true); // Re-enable add button when player removed
            btnAddPlayers.setEnabled(true);  // will auto-turn dark grey
        });
        playerListContainer.addView(row, playerListContainer.getChildCount() - 1); // Insert before Add/Sub buttons
        updateSubstituteHints();
    }
    // 🟢 Update hints for player names
    private void updateSubstituteHints() {
        int count = 1;
        for (int i = 0; i < playerListContainer.getChildCount(); i++) {
            View row = playerListContainer.getChildAt(i);
            EditText et = row.findViewById(R.id.et_player_name);
            if (et != null) {
                et.setHint("Substitute " + count++);
            }
        }
    }
}
