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

public class EditTeamAcitvity extends AppCompatActivity {

    private LinearLayout playerListContainer;
    private AppCompatButton btnAddPlayers, btnSaveChanges;
    private ShapeableImageView ivTeamLogo;
    private EditText etTeamName;

    private Uri teamLogoUri = null;
    private TeamDatabaseHelper dbHelper;
    private Team currentTeam;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_team_acitvity); // ✅ using same layout

        // Initialize views
        playerListContainer = findViewById(R.id.player_list_container);
        btnAddPlayers = findViewById(R.id.btn_add_players);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        ivTeamLogo = findViewById(R.id.iv_team_logo);
        etTeamName = findViewById(R.id.et_team_name);

        dbHelper = new TeamDatabaseHelper(this);

        // --- Get Team from Intent ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("team")) {
            currentTeam = (Team) intent.getSerializableExtra("team");

        }

        if (currentTeam == null) {
            Toast.makeText(this, "No team data received!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Pre-fill existing data ---
        etTeamName.setText(currentTeam.getName());
        if (currentTeam.getLogo() != null && !currentTeam.getLogo().isEmpty()) {
            teamLogoUri = Uri.parse(currentTeam.getLogo());
            ivTeamLogo.setImageURI(teamLogoUri);
        }

        populateExistingPlayers(currentTeam.getPlayers());

        // --- Image Picker ---
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        teamLogoUri = result.getData().getData();
                        if (teamLogoUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        teamLogoUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                Log.e("EditTeamActivity", "Failed to persist URI permission", e);
                            }
                            ivTeamLogo.setImageURI(teamLogoUri);
                        }
                    }
                });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) openGallery();
                    else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                });

        ivTeamLogo.setOnClickListener(v -> requestStoragePermission());
        btnAddPlayers.setOnClickListener(v -> addSubstitutePlayer());
        btnSaveChanges.setOnClickListener(v -> validateAndUpdateTeam());

        // --- Back & Cancel buttons ---
        ImageView backBtn = findViewById(R.id.backBtn);
        AppCompatButton btnCancel = findViewById(R.id.btn_cancel);

        View.OnClickListener cancelListener = v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Unsaved edits will be lost. Go back?")
                    .setPositiveButton("Yes", (d, w) -> finish())
                    .setNegativeButton("No", (d, w) -> d.dismiss())
                    .show();
        };

        backBtn.setOnClickListener(cancelListener);
        btnCancel.setOnClickListener(cancelListener);
    }

    // 🟢 Pre-fill existing player EditTexts
    private void populateExistingPlayers(List<String> players) {
        playerListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String player : players) {
            View row = inflater.inflate(R.layout.player_list_item, playerListContainer, false);
            EditText et = row.findViewById(R.id.et_player_name);
            et.setText(player);

            ImageView delete = row.findViewById(R.id.iv_delete_player);
            delete.setOnClickListener(v -> {
                if (playerListContainer.getChildCount() == 6) {
                    Toast.makeText(EditTeamAcitvity.this, "At least 6 players are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                playerListContainer.removeView(row);
                updateSubstituteHints();
                btnAddPlayers.setEnabled(true);
            });

            playerListContainer.addView(row);
        }
    }

    // 🟢 Request storage permission
    private void requestStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            openGallery();
        else
            requestPermissionLauncher.launch(permission);
    }

    // 🟢 Open gallery
    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e("EditTeamActivity", "Error opening gallery", e);
            Toast.makeText(this, "Unable to open gallery", Toast.LENGTH_SHORT).show();
        }
    }

    // 🟢 Gather all EditTexts recursively
    private List<EditText> getAllEditTexts(View parent) {
        List<EditText> list = new ArrayList<>();
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof EditText) list.add((EditText) child);
                else if (child instanceof ViewGroup) list.addAll(getAllEditTexts(child));
            }
        }
        return list;
    }

    // 🟢 Validate and save updates
    private void validateAndUpdateTeam() {
        String teamName = etTeamName.getText().toString().trim();
        if (teamName.isEmpty()) {
            etTeamName.setError("Team name required");
            return;
        }

        List<EditText> playerFields = getAllEditTexts(playerListContainer);
        List<String> playerNames = new ArrayList<>();

        for (EditText et : playerFields) {
            String name = et.getText().toString().trim();
            if (!name.isEmpty()) playerNames.add(name);
        }

        if (playerNames.size() < 6) {
            Toast.makeText(this, "At least 6 players required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            currentTeam.setName(teamName);
            currentTeam.setLogo(teamLogoUri != null ? teamLogoUri.toString() : currentTeam.getLogo());
            currentTeam.setPlayers(playerNames);

            dbHelper.updateTeam(currentTeam);
            Toast.makeText(this, "Team updated successfully!", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("team", currentTeam);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

        } catch (Exception e) {
            Log.e("EditTeamActivity", "Error updating team", e);
            Toast.makeText(this, "Failed to update team", Toast.LENGTH_SHORT).show();
        }
    }

    // 🟢 Add new substitute player
    private void addSubstitutePlayer() {
        int count = playerListContainer.getChildCount();
        if (count >= 12) {
            Toast.makeText(this, "Maximum 12 players allowed", Toast.LENGTH_SHORT).show();
            btnAddPlayers.setEnabled(false);
            return;
        }

        View row = LayoutInflater.from(this).inflate(R.layout.player_list_item, playerListContainer, false);
        ImageView delete = row.findViewById(R.id.iv_delete_player);

        delete.setOnClickListener(v -> {
            if (playerListContainer.getChildCount() == 6) {
                Toast.makeText(EditTeamAcitvity.this, "At least 6 players are required", Toast.LENGTH_SHORT).show();
                return;
            }
            playerListContainer.removeView(row);
            updateSubstituteHints();
            btnAddPlayers.setEnabled(true);
        });

        playerListContainer.addView(row);
        updateSubstituteHints();
    }

    private void updateSubstituteHints() {
        int i = 1;
        for (int j = 0; j < playerListContainer.getChildCount(); j++) {
            View row = playerListContainer.getChildAt(j);
            EditText et = row.findViewById(R.id.et_player_name);
            if (et != null) et.setHint("Player " + i++);
        }
    }
}
