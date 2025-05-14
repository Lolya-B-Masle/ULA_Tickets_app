package com.example.ula_tickets_app;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserHelper extends AppCompatActivity {

    private final String sourceURL = "https://perviymall.ru/radugarub/kino/";
    private final String userAgent = "Chrome/96.0.4664.93 Safari/537.36", referrer = "https://google.com";

    public void loadDates(Context context) {
        List<String> dateList = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, dateList);

        new Thread(() ->{
            try {
                Document doc = Jsoup.connect(sourceURL)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .get();

                Elements listNews = doc.select("div.cinema-calendar");

                for (Element element : listNews.select("div.fs-14.cinema-day__date.fw-500"))
                    dateList.add(element.text());

            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(()->{
                adapter.notifyDataSetChanged();
                //progressBar.setVisibility(View.GONE);
            });
        }).start();

    }



}
