package com.example.volnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Match.java
public class Match {
    private long id;
    private String teamAName, teamALogo;
    private String teamBName, teamBLogo;
    private String date, time;
    private int scoreA, scoreB, setNumber;
    private int timeoutA, timeoutB;
    private String winner;
    private List<Integer> teamASets;  // 🆕 add this
    private List<Integer> teamBSets;

    public Match(long id, String teamAName, String teamALogo,
                 String teamBName, String teamBLogo,
                 String date, String time,
                 int scoreA, int scoreB, int setNumber,
                 int timeoutA, int timeoutB, String winner,
                 int team1Set1, int team1Set2, int team1Set3, int team1Set4, int team1Set5,
                 int team2Set1, int team2Set2, int team2Set3, int team2Set4, int team2Set5) {
        this.id = id;
        this.teamAName = teamAName;
        this.teamALogo = teamALogo;
        this.teamBName = teamBName;
        this.teamBLogo = teamBLogo;
        this.date = date;
        this.time = time;
        this.scoreA = scoreA;
        this.scoreB = scoreB;
        this.setNumber = setNumber;
        this.timeoutA = timeoutA;
        this.timeoutB = timeoutB;
        this.winner = winner;
        // Initialize sets
        // Mutable set lists
        this.teamASets = new ArrayList<>(Arrays.asList(team1Set1, team1Set2, team1Set3, team1Set4, team1Set5));
        this.teamBSets = new ArrayList<>(Arrays.asList(team2Set1, team2Set2, team2Set3, team2Set4, team2Set5));
    }
    public long getId() { return id; }
    public String getTeamAName() { return teamAName; }
    public String getTeamALogo() { return teamALogo != null ? teamALogo : ""; }
    public String getTeamBName() { return teamBName; }
    public String getTeamBLogo() {return teamBLogo != null ? teamBLogo : ""; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getScoreA() { return scoreA; }
    public int getScoreB() { return scoreB; }
    public int getSetNumber() { return setNumber; }
    public int getTimeoutA() { return timeoutA; }
    public int getTimeoutB() { return timeoutB; }
    public String getWinner() { return winner; }
    public List<Integer> getTeamASets() { return teamASets; }
    public List<Integer> getTeamBSets() { return teamBSets; }

    public String getFinalScore() {
            if (winner == null || winner.isEmpty()) return scoreA + " - " + scoreB;
            return winner + " (" + scoreA + " - " + scoreB + ")";
        }

    }


