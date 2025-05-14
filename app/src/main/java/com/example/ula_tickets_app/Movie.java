package com.example.ula_tickets_app;

import java.util.List;

public class Movie {
    String title;
    List<String> showTimes;

    public Movie(String movie_name, List<String> showTimes) {
        this.title = movie_name;
        this.showTimes = showTimes;
    }

    public String getMovieName() {
        return title;
    }

    public List<String> getShowTimes() {
        return showTimes;
    }


}
