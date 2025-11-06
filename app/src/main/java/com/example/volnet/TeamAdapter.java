package com.example.volnet;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    // --- Interfaces for callback events ---
    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    // 🟢 Interface for delete actions
    public interface OnTeamDeleteListener {
        void onDelete(int position, Team team);
    }
    public interface OnTeamExploreListener {
        void onExplore(Team team);
    }

    public interface OnTeamEditListener {
        void onEdit(Team team);
    }


    private List<Team> teamList;
    private OnTeamClickListener clickListener;
    private OnTeamDeleteListener deleteListener;
    private OnTeamExploreListener exploreListener;
    private OnTeamEditListener editListener;

    // --- Constructor ---
    public TeamAdapter(List<Team> teamList,
                       OnTeamClickListener clickListener,
                       OnTeamDeleteListener deleteListener,
                       OnTeamExploreListener exploreListener,
                       OnTeamEditListener editListener) {
        this.teamList = teamList;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
        this.exploreListener = exploreListener;
        this.editListener = editListener;

    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teamList.get(position);
        holder.bind(team, clickListener, deleteListener, exploreListener, editListener,position);
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    // 🟢 For external refresh (after search, delete, etc.)
    public void updateList(List<Team> updatedTeams) {
        this.teamList = updatedTeams;
        notifyDataSetChanged();
    }

    // --- ViewHolder ---
    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName;
        ImageView teamLogo, deleteButton,btnEdit, btnExplore;

        TeamViewHolder(View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.textTeamName);
            teamLogo = itemView.findViewById(R.id.imageTeamLogo);
            deleteButton = itemView.findViewById(R.id.btn_delete);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnExplore = itemView.findViewById(R.id.btn_explore);// 🟢 make sure item_team.xml has this!

        }

        void bind(Team team,
                  OnTeamClickListener clickListener,
                  OnTeamDeleteListener deleteListener,
                  OnTeamExploreListener exploreListener,
                  OnTeamEditListener editListener,
                  int position) {

            teamName.setText(team.getName());

            // If logo is stored as Uri string in DB
            if (team.getLogo() != null && !team.getLogo().isEmpty()) {
//                teamLogo.setImageURI(Uri.parse(team.getLogo()));
                Glide.with(teamLogo.getContext())
                        .load(Uri.parse(team.getLogo()))
                        .placeholder(R.drawable.logo)  // fallback image while loading
                        .error(R.drawable.logo)        // fallback image on error
                        .into(teamLogo);
            } else {
                teamLogo.setImageResource(R.drawable.logo); // fallback
            }
            // --- Find the new views from item_team.xml ---
            LinearLayout playerSection = itemView.findViewById(R.id.player_section);
            LinearLayout playerListContainer = itemView.findViewById(R.id.player_list_container);
           // Button btnEditExpanded = itemView.findViewById(R.id.btn_edit_expanded);

            // Always start collapsed
            playerSection.setVisibility(View.GONE);
            playerListContainer.removeAllViews();


            // --- Explore button toggles expansion ---
            btnExplore.setOnClickListener(v -> {
                if (playerSection.getVisibility() == View.GONE) {
                    // Expand
                    toggleVisibility(playerSection, true);
                    playerListContainer.removeAllViews();

                    // Dynamically add player names
                    for (String player : team.getPlayers()) {
                        TextView tv = new TextView(playerListContainer.getContext());
                        tv.setText("• " + player);
                        tv.setTextColor(playerListContainer.getResources().getColor(android.R.color.black));
                        tv.setTextSize(14);
                        tv.setPadding(8, 4, 8, 4);
                        playerListContainer.addView(tv);
                    }

                    // Optional: rotate explore icon to indicate expansion
                    btnExplore.animate().rotation(180).setDuration(200).start();

                } else {
                    // Collapse
                    playerSection.setVisibility(View.GONE);
                    btnExplore.animate().rotation(0).setDuration(200).start();
                }
            });

            // --- Edit button inside expanded section ---
            btnEdit.setOnClickListener(v -> {
                if (editListener != null) editListener.onEdit(team);
            });

            // --- Delete button ---
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(position, team);
            });

            // --- Click entire card (optional) ---
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onTeamClick(team);
            });
        }
        // 🔸 Smooth show/hide animation helper
        private void toggleVisibility(View view, boolean show) {
            if (show) {
                view.setAlpha(0f);
                view.setVisibility(View.VISIBLE);
                view.animate().alpha(1f).setDuration(200).start();
            } else {
                view.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> view.setVisibility(View.GONE))
                        .start();
            }
        }
    }
}