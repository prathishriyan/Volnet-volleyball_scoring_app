package com.example.volnet;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import com.example.volnet.MatchHistoryAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private MatchHistoryAdapter adapter;
    // DECLARATIONS: Use Match1
    private List<Match> matchList;
    private List<Match> originalMatchList;
    private EditText searchBox;
    private MatchDatabaseHelper matchDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Find Views
        ImageView backButton = findViewById(R.id.back_arrow);
        ImageView sortButton = findViewById(R.id.sort_button);
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        searchBox = findViewById(R.id.search_box);

        matchDbHelper = new MatchDatabaseHelper(this);

        // Load from DB
        originalMatchList = loadMatchesFromDB();
        matchList = new ArrayList<>(originalMatchList);

        // RecyclerView Setup: Use the 4-parameter constructor (Delete, Export handlers)
        // NOTE: You must update MatchHistoryAdapter.java to accept List<Match1> and Consumer<Match1>
        adapter = new MatchHistoryAdapter(
                this,
                matchList,
                this::showDeleteConfirmation, // Handler for Delete
                this::performExportAction    // Handler for Export
        );
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);
// HistoryActivity.java

        adapter = new MatchHistoryAdapter(
                this,
                matchList,
                this::showDeleteConfirmation, // Handler for Delete
                this::performExportAction    // Handler for Export
        );// HistoryActivity.java

        adapter = new MatchHistoryAdapter(
                this,
                matchList,
                this::showDeleteConfirmation, // Handler for Delete
                this::performExportAction    // Handler for Export
        );// HistoryActivity.java

        adapter = new MatchHistoryAdapter(
                this,
                matchList,
                this::showDeleteConfirmation, // Handler for Delete
                this::performExportAction    // Handler for Export
        );// HistoryActivity.java

        adapter = new MatchHistoryAdapter(
                this,
                matchList,
                this::showDeleteConfirmation, // Handler for Delete
                this::performExportAction    // Handler for Export
        );// HistoryActivity.java

        adapter = new MatchHistoryAdapter(
                this,
                matchList,
                this::showDeleteConfirmation, // Handler for Delete
                this::performExportAction    // Handler for Export
        );
        // Attach Listeners
//        backButton.setOnClickListener(v -> finish());

        backButton.setOnClickListener(v -> {
            // Create an intent to navigate back to MainActivity
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            // Start MainActivity
            startActivity(intent);
            // Optionally, finish the current activity to remove it from the back stack
            finish();
        });


        sortButton.setOnClickListener(this::showSortMenu);

        // --- OnTouchListener (unchanged) ---
        searchBox.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                // Check for tap on the RIGHT (Date Picker) icon
                if (searchBox.getCompoundDrawables()[2] != null) {
                    if (event.getRawX() >= (searchBox.getRight() - searchBox.getCompoundDrawables()[2].getBounds().width() - searchBox.getTotalPaddingEnd())) {
                        showDatePicker();
                        v.performClick(); // FIX 1: Add this call
                        return true;
                    }
                }
                // Check for tap on the LEFT (Enable Typing) icon
                if (searchBox.getCompoundDrawables()[0] != null) {
                    if (event.getRawX() <= (searchBox.getCompoundDrawables()[0].getBounds().width() + searchBox.getTotalPaddingStart())) {
                        searchBox.requestFocus();
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
                        v.performClick(); // FIX 2: Add this call
                        return true;
                    }
                }

            }
            // Allow default behavior for typing in the middle of the EditText
            return false;
        });

        setupSearchListener();


        // ----------------------------------
// BOTTOM NAVIGATION BAR HANDLER
// ----------------------------------
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

// Highlight the current tab (History)
        bottomNavigationView.setSelectedItemId(R.id.nav_history);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener()

        {
            @Override
            public boolean onNavigationItemSelected (@NonNull MenuItem item){
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    startActivity(new Intent(HistoryActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_teams) {
                    startActivity(new Intent(HistoryActivity.this, manage_team_activity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (id == R.id.nav_history) {
                    // Already in History page
                    return true;

                } else if (id == R.id.nav_rules) {
                    startActivity(new Intent(HistoryActivity.this, RulesActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });
    }

    // ------------------------------
    // Load Matches from Database
    // ------------------------------
    private List<Match> loadMatchesFromDB() {
        List<Match> matches = new ArrayList<>();
        SQLiteDatabase db = matchDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + MatchDatabaseHelper.TABLE_MATCH + " WHERE winner != '' ORDER BY match_id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Match match = new Match(
                        cursor.getInt(cursor.getColumnIndexOrThrow("match_id")),           // id
                        cursor.getString(cursor.getColumnIndexOrThrow("team_a_name")),      // teamAName
                        cursor.getString(cursor.getColumnIndexOrThrow("team_a_logo")),      // teamALogo
                        cursor.getString(cursor.getColumnIndexOrThrow("team_b_name")),      // teamBName
                        cursor.getString(cursor.getColumnIndexOrThrow("team_b_logo")),      // teamBLogo
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),             // date
                        cursor.getString(cursor.getColumnIndexOrThrow("time")),             // time
                        cursor.getInt(cursor.getColumnIndexOrThrow("score_a")),        // scoreA
                        cursor.getInt(cursor.getColumnIndexOrThrow("score_b")),        // scoreB
                        cursor.getInt(cursor.getColumnIndexOrThrow("set_number")),         // setNumber
                        cursor.getInt(cursor.getColumnIndexOrThrow("timeouts_team1")),      // timeoutA
                        cursor.getInt(cursor.getColumnIndexOrThrow("timeouts_team2")),      // timeoutB
                        cursor.getString(cursor.getColumnIndexOrThrow("winner")),// winner
                        cursor.getInt(cursor.getColumnIndexOrThrow("team1_set1")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team1_set2")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team1_set3")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team1_set4")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team1_set5")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team2_set1")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team2_set2")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team2_set3")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team2_set4")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("team2_set5"))
                );
                matches.add(match);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return matches;
    }
    // ------------------------------------
    // SEARCH & DATE PICKER IMPLEMENTATION
    // ------------------------------------

    private void setupSearchListener() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterList(String query) {
        String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

        if (lowerCaseQuery.isEmpty()) {
            matchList.clear();
            matchList.addAll(originalMatchList);
        } else {
            // FILTERING: Use Match
            List<Match> filteredList = new ArrayList<>();
            for (Match match : originalMatchList) {
                if (match.getDate().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        match.getTime().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        match.getTeamAName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        match.getTeamBName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredList.add(match);
                }
            }
            matchList.clear();
            matchList.addAll(filteredList);
        }
        adapter.notifyDataSetChanged();
    }

    // Date Picker Dialog (unchanged)
    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat matchDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    String formattedDate = matchDateFormat.format(selectedDate.getTime());
                    searchBox.setText(formattedDate);
                    searchBox.setSelection(formattedDate.length());
                    Toast.makeText(this, "Searching for matches on: " + formattedDate, Toast.LENGTH_SHORT).show();
                },
                year, month, day);
        datePickerDialog.show();
    }

    // ------------------------------------
    // SORT IMPLEMENTATION
    // ------------------------------------

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.menu_sort_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            performSort((String) item.getTitle());
            return true;
        });

        popup.show();
    }

    private void performSort(String sortType) {
        // COMPARATOR: Use Match1
        Comparator<Match> comparator;

        switch (sortType.trim()) {
            case "A-Z":
                comparator = (m1, m2) -> m1.getTeamAName().compareToIgnoreCase(m2.getTeamAName());
                break;
            case "Z-A":
                comparator = (m1, m2) -> m2.getTeamAName().compareToIgnoreCase(m1.getTeamAName());
                break;
            case "Date Ascending":
                comparator = new DateComparator(true);
                break;
            case "Date Descending":
                comparator = new DateComparator(false);
                break;
            default:
                Toast.makeText(this, "Unknown sort option: " + sortType, Toast.LENGTH_SHORT).show();
                return;
        }

        Collections.sort(matchList, comparator);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Sorting applied: " + sortType, Toast.LENGTH_SHORT).show();
    }

    // Date Comparator for sorting - Use Match
    private static class DateComparator implements Comparator<Match> {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.US);
        private final boolean ascending;

        public DateComparator(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public int compare(Match m1, Match m2) {
            try {
                Date date1 = dateFormat.parse(m1.getDate() + " " + m1.getTime());
                Date date2 = dateFormat.parse(m2.getDate() + " " + m2.getTime());
                return ascending ? date1.compareTo(date2) : date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    // ------------------------------------
    // EXPORT & DELETE IMPLEMENTATION
    // ------------------------------------

    // Export Action - Use Match
    public void performExportAction(Match match) {
        Toast.makeText(this,
                "Exporting match details... ", Toast.LENGTH_LONG).show();

        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(16);
        paint.setAntiAlias(true);

        int y = 60;
        int lineSpacing = 25;

        // Title
        paint.setFakeBoldText(true);
        canvas.drawText("Match Report", 40, y, paint);
        paint.setFakeBoldText(false);
        y += lineSpacing * 2;

        // Match Header
        canvas.drawText("Teams: " + match.getTeamAName() + "  vs  " + match.getTeamBName(), 40, y, paint);
        y += lineSpacing;
        canvas.drawText("Date: " + match.getDate(), 40, y, paint);
        y += lineSpacing;
        canvas.drawText("Time: " + match.getTime(), 40, y, paint);
        y += lineSpacing;

        // Winner
        paint.setFakeBoldText(true);
        canvas.drawText("Winner: " + match.getWinner(), 40, y, paint);
        paint.setFakeBoldText(false);
        y += lineSpacing * 2;

        // Final Score (including all sets if available)
        if (match.getTeamASets() != null && match.getTeamBSets() != null &&
                !match.getTeamASets().isEmpty() && !match.getTeamBSets().isEmpty()) {

            canvas.drawText("Final Score (per set):", 40, y, paint);
            y += lineSpacing;

            List<Integer> aSets = match.getTeamASets();
            List<Integer> bSets = match.getTeamBSets();
            for (int i = 0; i < aSets.size(); i++) {
                canvas.drawText(
                        String.format("Set %d: %d - %d", i + 1, aSets.get(i), bSets.get(i)),
                        60, y, paint
                );
                y += lineSpacing;
            }
        } else {
            canvas.drawText("Final Score: " + match.getFinalScore(), 40, y, paint);
            y += lineSpacing;
        }

        // Timeouts
        y += lineSpacing;
        canvas.drawText(
                String.format("Timeouts → %s: %d not used | %s: %d not used",
                        match.getTeamAName(), match.getTimeoutA(),
                        match.getTeamBName(), match.getTimeoutB()),
                40, y, paint
        );

        pdfDocument.finishPage(page);


        // ===== Save to Downloads Folder =====
        String fileName = match.getTeamAName() + "_vs_" + match.getTeamBName() + ".pdf";

        Uri pdfUri = null;

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ (Scoped Storage)
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/VolNetReports");

                ContentResolver resolver = getContentResolver();
                pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

                if (pdfUri != null) {
                    try (OutputStream out = resolver.openOutputStream(pdfUri)) {
                        pdfDocument.writeTo(out);
                    }
                }
            } else {
                // For older Android versions
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File folder = new File(downloadsDir, "VolNetReports");
                if (!folder.exists()) folder.mkdirs();

                File pdfFile = new File(folder, fileName);
                try (FileOutputStream out = new FileOutputStream(pdfFile)) {
                    pdfDocument.writeTo(out);
                }
                pdfUri = Uri.fromFile(pdfFile);
            }

            Toast.makeText(this, "PDF saved to Downloads/VolNetReports", Toast.LENGTH_LONG).show();

            // ===== Share the PDF =====
            if (pdfUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Match PDF"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }


    // Delete Confirmation - Use Match in the logic
    private void showDeleteConfirmation(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // FIX: Corrected the layout name spelling
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
            Match matchToDelete = matchList.get(position);

            // ✅ Delete from database
            matchDbHelper.deleteMatchById(matchToDelete.getId());

            // ✅ Remove from memory lists
            matchList.remove(position);
            originalMatchList.remove(matchToDelete);

            // ✅ Notify adapter
            adapter.notifyItemRemoved(position);
            Toast.makeText(this, "Match permanently deleted", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        closeButton.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
}
}