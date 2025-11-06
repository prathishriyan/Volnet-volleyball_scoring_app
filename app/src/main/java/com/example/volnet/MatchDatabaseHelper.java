package com.example.volnet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MatchDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "matches.db";
    private static final int DATABASE_VERSION = 2;

    public static final  String TABLE_MATCH = "matches";
    private static final String COLUMN_ID = "match_id";
    private static final String COLUMN_TEAM_A_NAME = "team_a_name";
    private static final String COLUMN_TEAM_A_LOGO = "team_a_logo";
    private static final String COLUMN_TEAM_B_NAME = "team_b_name";
    private static final String COLUMN_TEAM_B_LOGO = "team_b_logo";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_SCORE_A = "score_a";
    private static final String COLUMN_SCORE_B = "score_b";
    private static final String COLUMN_SET_NUMBER = "set_number";
    private static final String COLUMN_TIMEOUT_A = "timeout_a";
    private static final String COLUMN_TIMEOUT_B = "timeout_b";
    private static final String COLUMN_WINNER = "winner";

    public MatchDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MATCH_TABLE = "CREATE TABLE " + TABLE_MATCH + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TEAM_A_NAME + " TEXT,"
                + COLUMN_TEAM_A_LOGO + " TEXT,"
                + COLUMN_TEAM_B_NAME + " TEXT,"
                + COLUMN_TEAM_B_LOGO + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_SCORE_A + " INTEGER,"
                + COLUMN_SCORE_B + " INTEGER,"
                + COLUMN_SET_NUMBER + " INTEGER,"
                + COLUMN_TIMEOUT_A + " INTEGER,"
                + COLUMN_TIMEOUT_B + " INTEGER,"
                + COLUMN_WINNER + " TEXT,"
                + "current_set INTEGER DEFAULT 1,"
                + "timeouts_team1 INTEGER DEFAULT 2,"
                + "timeouts_team2 INTEGER DEFAULT 2,"
                + "points_team1 INTEGER DEFAULT 0,"
                + "points_team2 INTEGER DEFAULT 0,"
                // New columns for 5 sets per team
                + "team1_set1 INTEGER DEFAULT 0,"
                + "team1_set2 INTEGER DEFAULT 0,"
                + "team1_set3 INTEGER DEFAULT 0,"
                + "team1_set4 INTEGER DEFAULT 0,"
                + "team1_set5 INTEGER DEFAULT 0,"
                + "team2_set1 INTEGER DEFAULT 0,"
                + "team2_set2 INTEGER DEFAULT 0,"
                + "team2_set3 INTEGER DEFAULT 0,"
                + "team2_set4 INTEGER DEFAULT 0,"
                + "team2_set5 INTEGER DEFAULT 0"
                + ")";

        db.execSQL(CREATE_MATCH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATCH);
        onCreate(db);
    }

    public long createMatch(String teamAName, String teamALogo,
                            String teamBName, String teamBLogo,
                            String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEAM_A_NAME, teamAName);
        values.put(COLUMN_TEAM_A_LOGO, teamALogo);
        values.put(COLUMN_TEAM_B_NAME, teamBName);
        values.put(COLUMN_TEAM_B_LOGO, teamBLogo);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_SCORE_A, 0);
        values.put(COLUMN_SCORE_B, 0);
        values.put(COLUMN_SET_NUMBER, 0);
        values.put(COLUMN_TIMEOUT_A, 2);
        values.put(COLUMN_TIMEOUT_B, 2);
        values.put(COLUMN_WINNER, "");
        values.put("current_set", 1);
        long id = db.insert(TABLE_MATCH, null, values);
        db.close();
        return id;
    }
    // Get match by id (for ScoringActivity)
    public Cursor getMatchById(long matchId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MATCH + " WHERE " + COLUMN_ID + "=?",
                new String[]{String.valueOf(matchId)});
    }

    // Update scores and set number (during scoring)
    public void updateScore(long matchId, int points1, int points2, int currentSet) {
        updateMatchProgress(matchId, points1, points2, currentSet, -1, -1); // -1 = don't update timeouts
    }

    // New method for updating timeouts separately
    public void updateTimeouts(long matchId, int timeoutsTeam1, int timeoutsTeam2) {
        updateMatchProgress(matchId, -1, -1, -1, timeoutsTeam1, timeoutsTeam2); // -1 = don't update points/set
    }

    // Your core method for updating any match progress
    public void updateMatchProgress(long matchId, int points1, int points2, int currentSet,
                                    int timeoutsTeam1, int timeoutsTeam2) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (points1 != -1) values.put("points_team1", points1);
        if (points2 != -1) values.put("points_team2", points2);
        if (currentSet != -1) values.put("current_set", currentSet);
        if (timeoutsTeam1 != -1) values.put("timeouts_team1", timeoutsTeam1);
        if (timeoutsTeam2 != -1) values.put("timeouts_team2", timeoutsTeam2);

        db.update(TABLE_MATCH, values, COLUMN_ID + "=?", new String[]{String.valueOf(matchId)});
        db.close();
    }
    // Set winner and finalize match

    public void endMatch(long matchId, String winner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WINNER, winner);
        db.update(TABLE_MATCH, values, COLUMN_ID + "=?", new String[]{String.valueOf(matchId)});
        db.close();
    }

    // Delete a match
    public void deleteMatch(long matchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MATCH, COLUMN_ID + "=?", new String[]{String.valueOf(matchId)});
        db.close();
    }

    // Get all matches (for Match History)
    public List<Match> getAllMatches() {
        List<Match> matchList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MATCH + " ORDER BY " + COLUMN_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Match match = new Match(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEAM_A_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEAM_A_LOGO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEAM_B_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEAM_B_LOGO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE_A)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE_B)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SET_NUMBER)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIMEOUT_A)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIMEOUT_B)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WINNER)),
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
                matchList.add(match);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return matchList;
    }
    public void updateSet(long matchId, int setNumber, int team1Points, int team2Points) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("team1_set" + setNumber, team1Points);
        values.put("team2_set" + setNumber, team2Points);
        db.update(TABLE_MATCH, values, "match_id=?", new String[]{String.valueOf(matchId)});
        db.close();
    }
    public void deleteMatchById(long matchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("matches", "match_id = ?", new String[]{String.valueOf(matchId)});
        db.close();
    }

}
