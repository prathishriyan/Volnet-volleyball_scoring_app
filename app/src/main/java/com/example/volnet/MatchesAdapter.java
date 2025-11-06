package com.example.volnet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<Match> matchList;        // Current displayed list
    private List<Match> originalList;     // Full list for filtering

    public MatchesAdapter(List<Match> matchList) {
        this.matchList = matchList;
        this.originalList = new ArrayList<>(matchList);
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.match_item, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position);
        holder.teamA.setText(match.getTeamAName());
        holder.teamB.setText(match.getTeamBName());
        holder.date.setText(match.getDate());
        holder.time.setText(match.getTime());
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

//    public void updateList(List<Match> filtered) {
//        matchList.clear();
//        matchList.addAll(filtered);
//        notifyDataSetChanged();
//    }

    public void updateList(List<Match> filtered) {
        matchList.clear();
        matchList.addAll(filtered);

//        // Keep originalList in sync
//        originalList.clear();
//        originalList.addAll(filtered);

        notifyDataSetChanged();
    }

    // -----------------------------
    // This is your search filter
    // -----------------------------
    public void filter(String text) {
        List<Match> filtered = new ArrayList<>();
        for (Match m : originalList) {
            if (m.getTeamAName().toLowerCase().contains(text.toLowerCase()) ||
                    m.getTeamBName().toLowerCase().contains(text.toLowerCase()) ||
                    m.getDate().toLowerCase().contains(text.toLowerCase()) ||
                    m.getTime().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(m);
            }
        }
        updateList(filtered);
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView teamA, teamB, date, time;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            teamA = itemView.findViewById(R.id.tvTeamA);
            teamB = itemView.findViewById(R.id.tvTeamB);
            date = itemView.findViewById(R.id.tvDate);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}
