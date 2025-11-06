package com.example.volnet;
import android.net.Uri;
import java.io.Serializable;
import java.util.List;
public class Team  implements Serializable{
    private int id;
    private String name;
    private String logo;
    private List<String> players;


    // 🟢 Empty constructor (useful for editing or creating)
    public Team() {}


    public Team(int id, String name,String logo,List<String> players) {
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.players = players;
    }
    public Team(String name, String logo, List<String> players) {
        this.name = name;
        this.logo = logo;
        this.players = players;
    }

    // ====== GETTERS ======
    public int getId() { return id; }
    public String getName() { return name; }
    public String getLogo() { return logo; }
    public List<String> getPlayers() { return players; }

    // ====== SETTERS ======
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogo(String logo) { this.logo = logo; }
    public void setPlayers(List<String> players) { this.players = players; }
}
