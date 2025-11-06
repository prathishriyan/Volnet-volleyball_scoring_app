package com.example.volnet;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class TeamScoringActivity extends AppCompatActivity {

    private TextView team1Name, team2Name, score1, score2;
    private Button plus1, minus1, plus2, minus2, endMatchBtn, timeout1Btn, timeout2Btn;
    private TextView[] setViews;
    private CountDownTimer countDownTimer;

    private int points1 = 0, points2 = 0;
    private int currentSet = 1;
    private final int[] setWins = {0, 0}; // sets won by team1 and team2
    private int timeoutsTeam1 = 2, timeoutsTeam2 = 2;

    private long matchId;
    private MatchDatabaseHelper matchDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_scoring);

        team1Name = findViewById(R.id.tvTeamA);
        team2Name = findViewById(R.id.tvTeamB);
        score1 = findViewById(R.id.tvScoreA);
        score2 = findViewById(R.id.tvScoreB);
        ImageView  backButton = findViewById(R.id.backBtn);

        ImageView imgTeamA = findViewById(R.id.imgTeamA);
        ImageView imgTeamB = findViewById(R.id.imgTeamB);

        plus1 = findViewById(R.id.btnPlusA);
        minus1 = findViewById(R.id.btnMinusA);
        plus2 = findViewById(R.id.btnPlusB);
        minus2 = findViewById(R.id.btnMinusB);
        timeout1Btn = findViewById(R.id.btnTimeoutTeamA);
        timeout2Btn = findViewById(R.id.btnTimeoutTeamB);

        endMatchBtn = findViewById(R.id.btnNextSet);

        // Set views for coloring
        setViews = new TextView[]{
                findViewById(R.id.set1),
                findViewById(R.id.set2),
                findViewById(R.id.set3),
                findViewById(R.id.set4),
                findViewById(R.id.set5)
        };

        matchDbHelper = new MatchDatabaseHelper(this);
        matchId = getIntent().getLongExtra("MATCH_ID", -1);

        loadMatch();

        plus1.setOnClickListener(v -> updateScore(1, true));
        minus1.setOnClickListener(v -> updateScore(1, false));
        plus2.setOnClickListener(v -> updateScore(2, true));
        minus2.setOnClickListener(v -> updateScore(2, false));

        timeout1Btn.setOnClickListener(v -> takeTimeout(1));
        timeout2Btn.setOnClickListener(v -> takeTimeout(2));


        backButton.setOnClickListener(v -> {
            new AlertDialog.Builder(TeamScoringActivity.this)
                    .setTitle("Leave Match?")
                    .setMessage("Scoring will NOT be saved. Do you want to create a new match?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Optional: delete unsaved match from database
                        matchDbHelper.deleteMatchById(matchId);

                        // Go to Create Match screen
                        Intent intent = new Intent(TeamScoringActivity.this, create_match_activity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
        endMatchBtn.setEnabled(false);
        endMatchBtn.setEnabled(false);
        endMatchBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.textGray));

        endMatchBtn.setOnClickListener(v -> {
            if (setWins[0] == 3 || setWins[1] == 3 || currentSet == 5) {
                endMatch();
            } else {
                goToNextSet();
            }
        });

    }

    private void loadMatch() {
        Cursor cursor = matchDbHelper.getMatchById(matchId);
        if (cursor.moveToFirst()) {
            team1Name.setText(cursor.getString(cursor.getColumnIndexOrThrow("team_a_name")));
            team2Name.setText(cursor.getString(cursor.getColumnIndexOrThrow("team_b_name")));
            ImageView team1Logo = findViewById(R.id.imgTeamA);
            ImageView team2Logo = findViewById(R.id.imgTeamB);



            String logo1 = cursor.getString(cursor.getColumnIndexOrThrow("team_a_logo"));
            String logo2 = cursor.getString(cursor.getColumnIndexOrThrow("team_b_logo"));

            // Load logos (from URI or fallback drawable)
            loadTeamLogo(team1Logo, logo1);
            loadTeamLogo(team2Logo, logo2);

            points1 = cursor.getInt(cursor.getColumnIndexOrThrow("points_team1"));
            points2 = cursor.getInt(cursor.getColumnIndexOrThrow("points_team2"));
            currentSet = cursor.getInt(cursor.getColumnIndexOrThrow("current_set"));
            timeoutsTeam1 = cursor.getInt(cursor.getColumnIndexOrThrow("timeouts_team1"));
            timeoutsTeam2 = cursor.getInt(cursor.getColumnIndexOrThrow("timeouts_team2"));

            score1.setText(String.valueOf(points1));
            score2.setText(String.valueOf(points2));

            // Highlight current set
            // --- Fix: Make all sets visible and set default color ---
            for (TextView tv : setViews) {
                tv.setBackgroundResource(R.drawable.set_incomplete); // all sets gray
            }

            // Highlight the current set
            if (currentSet >= 1 && currentSet <= 5) {
                setViews[currentSet - 1].setBackgroundResource(R.drawable.set_current);
            }
        }
        cursor.close();
    }
    private void loadTeamLogo(ImageView imageView, String logoPath) {
        if (logoPath == null || logoPath.isEmpty()) {
            imageView.setImageResource(R.drawable.logo); // default
            return;
        }

        try {
            Uri uri = Uri.parse(logoPath);
            imageView.setImageURI(uri);
            // fallback if URI didn't load
            imageView.post(() -> {
                if (imageView.getDrawable() == null) {
                    imageView.setImageResource(R.drawable.logo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.logo); // fallback
        }
    }

    private void updateScore(int team, boolean increase) {
        if (team == 1) {
            if (increase) points1++;
            else if (points1 > 0) confirmDecrement(1);
            score1.setText(String.valueOf(points1));
        } else {
            if (increase) points2++;
            else if (points2 > 0) confirmDecrement(2);
            score2.setText(String.valueOf(points2));
        }

        // Save full match progress including points, set, and timeouts
        matchDbHelper.updateMatchProgress(matchId, points1, points2, currentSet, timeoutsTeam1, timeoutsTeam2);
        checkSetPoint();
        checkSetWinner();
    }

    private void confirmDecrement(int team) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Do you want to decrease the point?")
                .setPositiveButton("Yes", (d, w) -> {
                    if (team == 1 && points1 > 0) points1--;
                    if (team == 2 && points2 > 0) points2--;
                    score1.setText(String.valueOf(points1));
                    score2.setText(String.valueOf(points2));
                })
                .setNegativeButton("No", null)
                .show();
    }

    private boolean setPointShown = false;

    private void checkSetPoint() {
        // Only show once per set
        if (setPointShown) return;

        // Team 1 Set Point Condition
        if (points1 >= 24 && (points1 - points2) == 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Set Point")
                    .setMessage("Set Point for " + team1Name.getText().toString())
                    .setPositiveButton("OK", null)
                    .show();
            setPointShown = true;
        }
        // Team 2 Set Point Condition
        else if (points2 >= 24 && (points2 - points1) == 1) {
            new AlertDialog.Builder(this)
                    .setTitle("Set Point")
                    .setMessage("Set Point for " + team2Name.getText().toString())
                    .setPositiveButton("OK", null)
                    .show();
            setPointShown = true;
        }
    }

    private void checkSetWinner() {
        if ((points1 >= 25 && points1 - points2 >= 2) ||
                (points2 >= 25 && points2 - points1 >= 2)) {

            String winner = points1 > points2 ? team1Name.getText().toString() : team2Name.getText().toString();

            // Update set wins
            if (points1 > points2) setWins[0]++; else setWins[1]++;

            // Save the set score in database
            matchDbHelper.updateSet(matchId, currentSet, points1, points2);

            // Color current set as completed (dark gray)
            setViews[currentSet - 1].setBackgroundResource(R.drawable.set_complete);
            endMatchBtn.setEnabled(true);
            endMatchBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.color_accent));



            // Disable scoring buttons until user clicks "Next Set"
            plus1.setEnabled(false);
            minus1.setEnabled(false);
            plus2.setEnabled(false);
            minus2.setEnabled(false);
            timeout1Btn.setEnabled(false);
            timeout2Btn.setEnabled(false);

            // Enable the "Next Set" button
            endMatchBtn.setEnabled(true);

            // Alert message
            new AlertDialog.Builder(this)
                    .setTitle("Set Over")
                    .setMessage(winner + " wins Set " + currentSet)
                    .setCancelable(false)
                    .setPositiveButton("OK", (d, w) -> d.dismiss())
                    .show();

            // If the match is already won, end it instead
            if (currentSet == 5 || setWins[0] == 3 || setWins[1] == 3) {
                // Match ends
                endMatchBtn.setText(R.string.end_match);
            }

//            // Color and disable the "Next Set" button at the end of a set
//            endMatchBtn.setEnabled(false);
//            endMatchBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.textGray));

        }
    }
    //helper to enable/disable scoring buttons:
    private void setScoringEnabled(boolean enabled) {
        plus1.setEnabled(enabled);
        minus1.setEnabled(enabled);
        plus2.setEnabled(enabled);
        minus2.setEnabled(enabled);
//        timeout1Btn.setEnabled(enabled);
//        timeout2Btn.setEnabled(enabled);
    }

    private void goToNextSet() {
        currentSet++;
        points1 = 0;
        points2 = 0;
        score1.setText("0");
        score2.setText("0");

        // Highlight next set
        if (currentSet <= 5) {
            for (int i = 0; i < setViews.length; i++) {
                if (i == currentSet - 1)
                    setViews[i].setBackgroundResource(R.drawable.set_current); // 🔥 highlight current set
                else if (setViews[i].getBackground().getConstantState() ==
                        ContextCompat.getDrawable(this, R.drawable.set_current).getConstantState()) {
                    // if any previous was current, make it incomplete (light gray)
                    setViews[i].setBackgroundResource(R.drawable.set_current);
                }
            }
        }


        // Update database progress
        matchDbHelper.updateMatchProgress(matchId, points1, points2, currentSet, timeoutsTeam1, timeoutsTeam2);

        // Re-enable scoring
        setScoringEnabled(true);

        // ✅ Re-enable timeout buttons if team still has timeouts left
        if (timeoutsTeam1 > 0) {
            timeout1Btn.setEnabled(true);
        } else {
            timeout1Btn.setEnabled(true);
        }

        if (timeoutsTeam2 > 0) {
            timeout2Btn.setEnabled(true);
        } else {
            timeout2Btn.setEnabled(true);
        }

        // Disable Next Set button until next set ends
        endMatchBtn.setEnabled(false);
        endMatchBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.textGray));
    }

    private void takeTimeout(int team) {
        if (team == 1 && timeoutsTeam1 == 0) {
            showMessage("Team " + team1Name.getText() + " has no timeouts left!");
            return;
        }
        if (team == 2 && timeoutsTeam2 == 0) {
            showMessage("Team " + team2Name.getText() + " has no timeouts left!");
            return;
        }

//        if (team == 1 && timeoutsTeam1 > 0)
//            timeoutsTeam1--;
//        else if (team == 2 && timeoutsTeam2 > 0)
//                timeoutsTeam2--;
//        // Save updated timeouts
//        matchDbHelper.updateTimeouts(matchId, timeoutsTeam1, timeoutsTeam2);

        showTimeoutDialog(team);
    }
    private void reduceTimeout(int team) {
        if (team == 1 && timeoutsTeam1 > 0) {
            timeoutsTeam1--;
        } else if (team == 2 && timeoutsTeam2 > 0) {
            timeoutsTeam2--;
        }

        // Save the updated values in the database
        matchDbHelper.updateTimeouts(matchId, timeoutsTeam1, timeoutsTeam2);
    }


    private void showTimeoutDialog(int team) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Timeout - " + (team == 1 ? team1Name.getText() : team2Name.getText()));

        // Create a centered TextView for timer
        final TextView timerView = new TextView(this);
        timerView.setTextSize(32);
        timerView.setTextColor(ContextCompat.getColor(this, R.color.black));
        timerView.setPadding(30, 60, 30, 60);
        timerView.setGravity(android.view.Gravity.CENTER);


        // Create a vertical layout to hold timer and buttons
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        layout.addView(timerView); // Add timerView to layout


        // Horizontal layout for buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        buttonLayout.setPadding(0, 50, 0, 0);


        // Pause/Play button (toggle)
        ImageButton pausePlayButton = new ImageButton(this);
        pausePlayButton.setImageResource(R.drawable.ic_pause); // initial icon
        pausePlayButton.setBackgroundColor(Color.TRANSPARENT);
        buttonLayout.addView(pausePlayButton);

        // Done button
        Button doneButton = new Button(this);
        doneButton.setText("Done");
        buttonLayout.addView(doneButton);

        // Cancel button
        Button cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        buttonLayout.addView(cancelButton);

        layout.addView(buttonLayout);

        builder.setView(layout);
        builder.setCancelable(false);


        AlertDialog dialog = builder.create();
        dialog.show();

        // Timer
        final long[] timeRemaining = {5 * 60 * 1000}; // 5 minutes
        final boolean[] isPaused = {false};

        CountDownTimer timer = new CountDownTimer(timeRemaining[0], 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isPaused[0]) {
                    timeRemaining[0] = millisUntilFinished;
                    int minutes = (int) (timeRemaining[0] / 1000) / 60;
                    int seconds = (int) (timeRemaining[0] / 1000) % 60;
                    timerView.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }


            @Override
            public void onFinish() {
                timerView.setText("00:00");
                dialog.dismiss();
                reduceTimeout(team);
                showMessage("Timeout ended for " + (team == 1 ? team1Name.getText() : team2Name.getText()));
            }
        }.start();

        // Pause/Play click
        pausePlayButton.setOnClickListener(v -> {
            if (isPaused[0]) {
                // Resume
                isPaused[0] = false;
                pausePlayButton.setImageResource(R.drawable.ic_pause);
            } else {
                // Pause
                isPaused[0] = true;
                pausePlayButton.setImageResource(R.drawable.ic_play);
            }
        });


        // Done button
        doneButton.setOnClickListener(v -> {
            timer.cancel();
            dialog.dismiss();
            reduceTimeout(team);
            showMessage("Timeout finished for " + (team == 1 ? team1Name.getText() : team2Name.getText()));
        });

        // Cancel button - dismiss without finishing
        cancelButton.setOnClickListener(v -> {
            timer.cancel();
            dialog.dismiss();
            showMessage("Timeout canceled for " + (team == 1 ? team1Name.getText() : team2Name.getText()));
        });
    }

    private void showMessage(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void endMatch() {
        // Save current set score before finishing
        matchDbHelper.updateSet(matchId, currentSet, points1, points2);
        String winner;
        if (setWins[0] > setWins[1]) {
            winner = team1Name.getText().toString();
        } else {
            winner = team2Name.getText().toString();
        }

        matchDbHelper.endMatch(matchId, winner);

        // 🏆 Prepare match summary
        String teamA = team1Name.getText().toString();
        String teamB = team2Name.getText().toString();
        String finalScoreSummary =
                teamA + " (" + setWins[0] + " sets)  " + points1 + " pts\n" +
                        teamB + " (" + setWins[1] + " sets)  " + points2 + " pts";

        // 🟢 Show final dialog with winner and scores
        new AlertDialog.Builder(this)
                .setTitle("🏐 Match Ended")
                .setMessage("🎉 Congratulations!!! 🎉\n\nWinner: " + winner +
                        "\n\nFinal Scores:\n" + finalScoreSummary)
                .setCancelable(false)
                .setPositiveButton("OK", (d, w) -> {
                    Intent intent = new Intent(TeamScoringActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
