package com.example.volnet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// TeamDatabaseHelper.java
public class TeamDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "teams.db";
    private static final int DATABASE_VERSION = 1;

    // Teams table
    private static final String TABLE_TEAM = "teams";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "team_name";
    private static final String COLUMN_LOGO = "team_logo";

    // Players table
    private static final String TABLE_PLAYERS = "players";
    private static final String COLUMN_PLAYER_ID = "player_id";
    private static final String COLUMN_PLAYER_NAME = "player_name";
    private static final String COLUMN_TEAM_ID = "team_id";  // Foreign key to teams.id

    public TeamDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create teams table
        String CREATE_TEAM_TABLE = "CREATE TABLE " + TABLE_TEAM + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_LOGO + " TEXT)";
        db.execSQL(CREATE_TEAM_TABLE);

        // Create players table with foreign key to teams table
        String CREATE_PLAYERS_TABLE = "CREATE TABLE " + TABLE_PLAYERS + "("
                + COLUMN_PLAYER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PLAYER_NAME + " TEXT,"
                + COLUMN_TEAM_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_TEAM_ID + ") REFERENCES " + TABLE_TEAM + "(" + COLUMN_ID + ") ON DELETE CASCADE)";
        db.execSQL(CREATE_PLAYERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop both tables if upgrading
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAM);
        onCreate(db);
    }

    // Add a team and return the inserted team id
    public long addTeam(String teamName, String teamLogo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, teamName);
        values.put(COLUMN_LOGO, teamLogo);
        long teamId = db.insert(TABLE_TEAM, null, values);
        db.close();
        return teamId; // return id for adding players
    }

    // Add player linked to a team id
    public void addPlayer(long teamId, String playerName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYER_NAME, playerName);
        values.put(COLUMN_TEAM_ID, teamId);
        db.insert(TABLE_PLAYERS, null, values);
        db.close();
    }

    // Get all teams with their info (players not included here)
    public List<Team> getAllTeams() {
        List<Team> teamList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TEAM, null);

//        if (cursor.moveToFirst()) {
//            do {
//                teamList.add(new Team(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
//                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
//                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGO))
//                ));
//            } while (cursor.moveToNext());
//        }
        if (cursor.moveToFirst()) {
            do {
                int teamId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String teamName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String teamLogo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGO));

                List<String> players = getPlayersByTeamId(teamId);
                teamList.add(new Team(teamId, teamName, teamLogo, players));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return teamList;
    }

    // Get players for a specific team
    public List<String> getPlayersByTeamId(long teamId) {
        List<String> players = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLAYERS,
                new String[]{COLUMN_PLAYER_NAME},
                COLUMN_TEAM_ID + "=?",
                new String[]{String.valueOf(teamId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                players.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAYER_NAME)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return players;
    }

    public boolean deleteTeam(int teamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("teams", "id = ?", new String[]{String.valueOf(teamId)});
        db.delete("players", "team_id = ?", new String[]{String.valueOf(teamId)}); // also remove players
        return rows > 0;
    }


    // ✅ Delete all players of a team
    public void deletePlayersByTeamId(int teamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, COLUMN_TEAM_ID + "=?", new String[]{String.valueOf(teamId)});
        db.close();
    }

    // ✅ Delete a team (and its players)
    public void deleteTeamById(int teamId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLAYERS, COLUMN_TEAM_ID + "=?", new String[]{String.valueOf(teamId)});
        db.delete(TABLE_TEAM, COLUMN_ID + "=?", new String[]{String.valueOf(teamId)});
        db.close();
    }

    // ✅ Update team name/logo
    public void updateTeam(Team team) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, team.getName());
        values.put(COLUMN_LOGO, team.getLogo());

        db.update(TABLE_TEAM, values, COLUMN_ID + "=?", new String[]{String.valueOf(team.getId())});

        // Delete old players
        db.delete(TABLE_PLAYERS, COLUMN_TEAM_ID + "=?", new String[]{String.valueOf(team.getId())});

        // Reinsert updated players
        for (String player : team.getPlayers()) {
            addPlayer(team.getId(), player);
        }

        db.close();
    }

    public void deleteMatchById(long matchId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("matches", "id = ?", new String[]{String.valueOf(matchId)});
        db.close();
    }

}
