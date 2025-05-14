package com.example.ula_tickets_app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {
    TextView movie_hall, hall_row, hall_places;
    Spinner movie_name, movie_date, movie_time;
    Button done_btn, clear_btn, history_btn;
    ImageView settings_btn;
    Bitmap company_logo, cinema_logo, divider, BG, ticket_text;
    DatabaseHelper databaseHelper;
    private FrameLayout progressBar;
    private final String cssQuery = "div.col-sm-6.col-md-4.col-lg-3.movies-item", query = "h6.h6.mb-2";
    private final String sourceURL = "https://perviymall.ru/radugarub/kino/";
    private final String userAgent = "Chrome/96.0.4664.93 Safari/537.36", referrer = "https://google.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        databaseHelper = new DatabaseHelper(getApplicationContext());

        company_logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        cinema_logo = BitmapFactory.decodeResource(getResources(), R.drawable.rainbow_logo);
        divider = BitmapFactory.decodeResource(getResources(), R.drawable.divider);
        BG = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        ticket_text = BitmapFactory.decodeResource(getResources(), R.drawable.ticket_text);

        done_btn = findViewById(R.id.done_btn);
        clear_btn = findViewById(R.id.clear_btn);
        history_btn = findViewById(R.id.history_btn);

        movie_name = findViewById(R.id.movie_name_field);
        movie_date = findViewById(R.id.movie_date_field);
        movie_time = findViewById(R.id.movie_time_field);

        settings_btn = findViewById(R.id.settings_btn);
        progressBar = findViewById(R.id.progressBar);

        //List<String> movieList = new ArrayList<>();
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, movieList);



        progressBar.setVisibility(View.VISIBLE);
        loadDateList();

        movie_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                formatDateForParser(movie_date.getSelectedItem().toString());
                progressBar.setVisibility(view.VISIBLE);
                loadFilmsOnDate(movie_date.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        movie_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(view.VISIBLE);
                loadMovieSession(movie_name.getSelectedItem().toString(), movie_date.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        TextView[] fields = {
            movie_hall = findViewById(R.id.movie_hall_field),
            hall_row = findViewById(R.id.movie_row_field),
            hall_places = findViewById(R.id.movie_place_field)
        };

        clear_btn.setOnClickListener(v -> {
            try {
                for(int i = 0; i <= fields.length; i++)
                    fields[i].setText(null);
            } catch (Exception e) {
                Log.d(e.toString(), "clear error");
            }
        });

        history_btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        done_btn.setOnClickListener(v -> {
            if (movie_hall.getText().toString().isEmpty() || hall_row.getText().toString().isEmpty() || hall_places.getText().toString().isEmpty()) {
                Toast.makeText(this, "Для создания билета необходимо заполнить все поля", Toast.LENGTH_SHORT).show();
            } else {

                v.setEnabled(false);

                createTicket(movie_date.getSelectedItem().toString());

                Date now = new Date();
                SimpleDateFormat ticket_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                boolean isInserted = databaseHelper.addTicket(movie_name.getSelectedItem().toString(), ticket_date.format(now), hall_row.getText().toString(),
                        hall_places.getText().toString(), movie_hall.getText().toString(), 120);

                String message = "Билет сохранён в папке «БИЛЕТЫ_В_КИНО» вашей галереи";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        v.setEnabled(true);
                    }
                }, 5000);
            }
        });
    }


    public void loadDateList() {
        List<String> dateList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        movie_date.setAdapter(adapter);

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
                progressBar.setVisibility(View.GONE);
            }

            runOnUiThread(()->{
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }
    public void loadFilmsOnDate(String date) {
        String[] parseDate = formatDateForParser(date);

        String day = parseDate[0];
        String month = parseDate[1];
        String url = "https://perviymall.ru/radugarub/kino/?date=2025-"+month+"-"+day+"";

        List<String> movieList = new ArrayList<>();
        ArrayAdapter<String> movieAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, movieList);
        movieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        movie_name.setAdapter(movieAdapter);

        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .get();

                Elements movieElements = doc.select("div.col-sm-6.col-md-4.col-lg-3.movies-item");

                for (Element movieElement : movieElements)
                    movieList.add(movieElement.select("h6.h6.mb-2").text());

            } catch (Exception e) {
                Log.d("ERROR", e.toString());
            }

            runOnUiThread(()->{
                movieAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });


        }).start();

    }

    public void loadMovieSession(String movieName, String date) {
        String[] parseDate = formatDateForParser(date);

        String day = parseDate[0];
        String month = parseDate[1];
        String url = "https://perviymall.ru/radugarub/kino/?date=2025-"+month+"-"+day+"";

        List<String> movieTimesList = new ArrayList<>();
        ArrayAdapter<String> movieTimesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, movieTimesList);
        movieTimesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        movie_time.setAdapter(movieTimesAdapter);

        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .get();

                Elements movieElements = doc.select("div.col-sm-6.col-md-4.col-lg-3.movies-item");

                for (Element movieElement : movieElements.select("h6.h6.mb-2"))
                    if (movieElement.text().equals(movieName))
                            for (Element time : movieElements.select(".session.fs-14"))
                                movieTimesList.add(time.select(".session__time").text());

            } catch (Exception e) {
                Log.d("ERROR", e.toString());
            }

            runOnUiThread(()->{
                movieTimesAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    public String[] formatDateForParser(String date) {
        String[] dateStr = date.trim().split(" ");
        Log.d("Элементы строки даты", dateStr[0] + " - " + dateStr[1]);

        String monthName = dateStr[1];
        String day = dateStr[0];

        if (monthName.equals("января") || monthName.equals("янв"))
            return new String[] {day, "01"};
        else if (monthName.equals("февраля") || monthName.equals("фев"))
            return new String[] {day, "02"};
        else if (monthName.equals("марта") || monthName.equals("мат"))
            return new String[] {day, "03"};
        else if (monthName.equals("апреля") || monthName.equals("апр"))
            return new String[] {day, "04"};
        else if (monthName.equals("мая"))
            return new String[] {day, "05"};
        else if (monthName.equals("июня"))
            return new String[] {day, "06"};
        else if (monthName.equals("июля"))
            return new String[] {day, "07"};
        else if (monthName.equals("августа") || monthName.equals("авг"))
            return new String[] {day, "08"};
        else if (monthName.equals("сентября") || monthName.equals("сен"))
            return new String[] {day, "09"};
        else if (monthName.equals("октября") || monthName.equals("окт"))
            return new String[] {day, "10"};
        else if (monthName.equals("ноября") || monthName.equals("ноя"))
            return new String[] {day, "11"};
        else if (monthName.equals("декабря") || monthName.equals("дек"))
            return new String[] {day, "12"};

        return null;
    }

    // --------------------------------------------- Text splitter -----------------------

    public static String[] splitStringByLastSpace(String input, int maxLength) {
        if (input == null || input.length() <= maxLength)
            return new String[]{input};

        int lastSpaceIndex = input.substring(0, maxLength).lastIndexOf(' ');

        if (lastSpaceIndex == -1)
            lastSpaceIndex = maxLength;

        String firstPart = input.substring(0, lastSpaceIndex).trim();
        String secondPart = input.substring(lastSpaceIndex).trim();

        return new String[]{firstPart, secondPart};
    }

    // --------------------------------------------- Image creator -----------------------
    private void createTicket(String date) {

        String[] parseDate = formatDateForParser(date);

        String day = parseDate[0];
        String month = parseDate[1];

        String movie_date = day+"."+month;

        TicketCreator ticket = new TicketCreator();
        ticket.generateTicketImage(this, BG, cinema_logo, company_logo, divider, ticket_text,
                splitStringByLastSpace(movie_name.getSelectedItem().toString(), 20),
                movie_date, movie_time.getSelectedItem().toString(),
                hall_row.getText().toString(), hall_places.getText().toString(), movie_hall.getText().toString());
    }

}

