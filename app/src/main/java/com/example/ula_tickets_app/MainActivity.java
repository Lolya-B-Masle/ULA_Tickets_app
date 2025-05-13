package com.example.ula_tickets_app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
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
    TextView movie_hall, movie_date, movie_time, hall_row, hall_places;
    Spinner movie_name;
    Button done_btn, clear_btn, history_btn;
    ImageView settings_btn;
    Bitmap company_logo, cinema_logo, divider, BG, ticket_text;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
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
        settings_btn = findViewById(R.id.settings_btn);
        progressBar = findViewById(R.id.progressBar);

        List<String> movieList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, movieList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        movie_name.setAdapter(adapter);

        progressBar.setVisibility(View.VISIBLE);
        new Thread(() ->{

            try {
                Document doc = Jsoup.connect(sourceURL)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .get();

                Elements listNews = doc.select(cssQuery);

                for (Element element : listNews.select(query))
                    movieList.add(element.text());


            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(()->{
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });
        }).start();

        TextView[] fields = {
            movie_hall = findViewById(R.id.movie_hall_field),
            movie_date = findViewById(R.id.movie_date_field),
            movie_time = findViewById(R.id.movie_time_field),
            hall_row = findViewById(R.id.movie_row_field),
            hall_places = findViewById(R.id.movie_place_field)
        };

        movie_date.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        movie_time.setOnClickListener(v -> {
            showTimePickerDialog();
        });

        clear_btn.setOnClickListener(v -> {
            try {
                for(int i = 0; i <= fields.length; i++){
                    fields[i].setText(null);
                    movie_date.setText(R.string.dateTime_val_hint);
                    movie_time.setText(R.string.dateTime_val_hint);

                    movie_date.setTextSize(18);
                    movie_time.setTextSize(18);
                }
            } catch (Exception e) {
                Log.d(e.toString(), "clear error");
            }
        });

        history_btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        done_btn.setOnClickListener(v -> {
            if (movie_hall.getText().toString().isEmpty() || movie_date.getText().toString().isEmpty() ||
                    movie_time.getText().toString().isEmpty() || hall_row.getText().toString().isEmpty() || hall_places.getText().toString().isEmpty()) {
                Toast.makeText(this, "Для создания билета необходимо заполнить все поля", Toast.LENGTH_SHORT).show();
            } else {

                v.setEnabled(false);

                createTicket();

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
                //Toast.makeText(this, "Билет в базу", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("SELECTED_DATE", movie_date.getText().toString());
        outState.putString("SELECTED_TIME", movie_time.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String savedDate = savedInstanceState.getString("SELECTED_DATE");
        String savedTime = savedInstanceState.getString("SELECTED_TIME");
        if (savedDate != null) {
            movie_date.setText(savedDate);
            movie_time.setText(savedTime);
        }
    }

    // --------------------------------------------- DB --------------------------------


    // --------------------------------------------- Date picker -----------------------

    private final Calendar selectedDate = Calendar.getInstance();

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        updateSelectedDateText();
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void updateSelectedDateText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
        String formattedDate = dateFormat.format(selectedDate.getTime());
        movie_date.setText(formattedDate);
        movie_date.setTextSize(24);
    }

// --------------------------------------------- Time picker -----------------------

    private final Calendar selectedTime = Calendar.getInstance();

    private void showTimePickerDialog() {
        int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        updateSelectedTimeText();
                    }
                },
                hour, minute, true // true - 24-часовой формат, false - AM/PM
        );

        timePickerDialog.show();
    }

    private void updateSelectedTimeText() {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE));
        movie_time.setText(formattedTime);
        movie_time.setTextSize(24);
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

    private void createTicket() {
        TicketCreator ticket = new TicketCreator();
        ticket.generateTicketImage(this, BG, cinema_logo, company_logo, divider, ticket_text,
                splitStringByLastSpace(movie_name.getSelectedItem().toString(), 20),
                movie_date.getText().toString(), movie_time.getText().toString(),
                hall_row.getText().toString(), hall_places.getText().toString(), movie_hall.getText().toString());
    }

}

