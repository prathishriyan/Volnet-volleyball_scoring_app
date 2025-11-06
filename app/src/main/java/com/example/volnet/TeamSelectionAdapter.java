package com.example.volnet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;


import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.List;

public class TeamSelectionAdapter extends RecyclerView.Adapter<TeamSelectionAdapter.TeamViewHolder> {

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    private List<Team> teams;
    private OnTeamClickListener listener;

    public TeamSelectionAdapter(List<Team> teams, OnTeamClickListener listener) {
        this.teams = teams;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_only_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.teamName.setText(team.getName());

        // Load logo (Glide is recommended)
        Glide.with(holder.itemView.getContext())
                .load(team.getLogo()) // drawable resource ID or URL
                .into(holder.teamLogo);

        holder.teamLogo.setVisibility(View.VISIBLE);

//        // Hide delete button
//        holder.deleteButton.setVisibility(View.GONE);

        // Handle click
        holder.itemView.setOnClickListener(v -> listener.onTeamClick(team));
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        ImageView teamLogo, deleteButton;
        TextView teamName;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            teamLogo = itemView.findViewById(R.id.imageTeamLogo);
            teamName = itemView.findViewById(R.id.textTeamName);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public interface TeamSelectionListener {
            void onTeamSelected(Team team);
        }
        void bind(Team team) {
            // bind your team data to itemView
            // teamName.setText(team.getName());
        }
    }
}
