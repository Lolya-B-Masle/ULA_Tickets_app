package com.example.ula_tickets_app;

public class Movie {
    int ID;
    String movie_name;
    String[] sessions;

    public Movie(int ID, String movie_name, String[] sessions) {
        this.ID = ID;
        this.movie_name = movie_name;
        this.sessions = sessions;
    }

    public int getID() {
        return ID;
    }

    public String getMovieName() {
        return movie_name;
    }

    public String[] getSessions() {
        return sessions;
    }
}
