package com.example.ula_tickets_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MovieActivity extends AppCompatActivity {
    private static final String TAG = "MovieActivity";

    private TextView hall_places;
    private Spinner movie_name, movie_date, movie_time, hall_row, movie_hall;
    private Button history_btn;
    private Bitmap company_logo, cinema_logo, divider, BG, ticket_text;
    private LinearLayout hall_view, clear_btn, done_btn;
    private ImageView back_button;
    private DatabaseHelper databaseHelper;
    private FrameLayout progressBar;

    private final String MAIN_FOLDER = "БИЛЕТЫ_В_КИНО";
    private final String sourceURL = "https://firstmall.ru/radugarub/kino/";
    private final String userAgent = "Chrome/96.0.4664.93 Safari/537.36";
    private final String referrer = "https://google.com";

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Кэш для данных
    private final Map<String, List<String>> dateCache = new HashMap<>();
    private final Map<String, List<String>> movieCache = new HashMap<>();
    private final Map<String, String[]> sessionCache = new HashMap<>();

    // Предварительно инициализированные массивы для спиннеров
    private static final List<Integer> ROW_NUMBERS = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    private static final List<Integer> HALL_NUMBERS = Arrays.asList(1, 2, 3, 4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Запрос разрешений только если не предоставлены
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PackageManager.PERMISSION_GRANTED);
        }

        initializeComponents();
        setupSpinners();
        setupClickListeners();
        loadInitialData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        // Освобождение ресурсов Bitmap
        recycleBitmaps();
    }

    private void initializeComponents() {
        databaseHelper = new DatabaseHelper(getApplicationContext());

        // Ленивая загрузка Bitmap
        loadBitmaps();

        done_btn = findViewById(R.id.done_btn);
        clear_btn = findViewById(R.id.clear_btn);
        history_btn = findViewById(R.id.history_btn);
        back_button = findViewById(R.id.back_img_btn);

        movie_name = findViewById(R.id.movie_name_field);
        movie_date = findViewById(R.id.movie_date_field);
        movie_time = findViewById(R.id.movie_time_field);
        hall_row = findViewById(R.id.movie_row_field);
        movie_hall = findViewById(R.id.movie_hall_field);

        hall_places = findViewById(R.id.movie_place_field);
        progressBar = findViewById(R.id.progressBar);
        hall_view = findViewById(R.id.movie_hall_view);
    }

    private void loadBitmaps() {
        company_logo = BitmapFactory.decodeResource(getResources(), R.drawable.images_logo);
        cinema_logo = BitmapFactory.decodeResource(getResources(), R.drawable.images_rainbow_logo);
        divider = BitmapFactory.decodeResource(getResources(), R.drawable.images_divider);
        BG = BitmapFactory.decodeResource(getResources(), R.drawable.images_ticket_background);
        ticket_text = BitmapFactory.decodeResource(getResources(), R.drawable.images_ticket_text);
    }

    private void recycleBitmaps() {
        if (company_logo != null && !company_logo.isRecycled()) company_logo.recycle();
        if (cinema_logo != null && !cinema_logo.isRecycled()) cinema_logo.recycle();
        if (divider != null && !divider.isRecycled()) divider.recycle();
        if (BG != null && !BG.isRecycled()) BG.recycle();
        if (ticket_text != null && !ticket_text.isRecycled()) ticket_text.recycle();
    }

    private void setupSpinners() {
        // Используем предварительно созданные неизменяемые списки
        ArrayAdapter<Integer> rowAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ROW_NUMBERS);
        rowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hall_row.setAdapter(rowAdapter);

        ArrayAdapter<Integer> hallAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, HALL_NUMBERS);
        hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        movie_hall.setAdapter(hallAdapter);
    }

    private void setupClickListeners() {
        clear_btn.setOnClickListener(v -> {
            hall_places.setText("");
        });

        history_btn.setOnClickListener(v -> {
            startActivity(new Intent(MovieActivity.this, HistoryActivity.class));
        });

        back_button.setOnClickListener(v -> {
            startActivity(new Intent(MovieActivity.this, HomeActivity.class));
            finish();
        });

        done_btn.setOnClickListener(v -> handleTicketCreation(v));

        setupSpinnerListeners();
    }

    private void setupSpinnerListeners() {
        movie_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = movie_date.getSelectedItem().toString();
                if (!selectedDate.isEmpty()) {
                    showProgressBar();
                    loadFilmsOnDate(selectedDate);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        movie_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMovie = movie_name.getSelectedItem().toString();
                String selectedDate = movie_date.getSelectedItem().toString();
                if (!selectedMovie.isEmpty() && !selectedDate.isEmpty() &&
                        !selectedMovie.equals("Нет сеансов на эту дату.")) {
                    showProgressBar();
                    loadMovieSession(selectedMovie, selectedDate);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadInitialData() {
        showProgressBar();
        executor.execute(() -> {
            try {
                loadDateList();
                mainHandler.post(this::hideProgressBar);
            } catch (Exception e) {
                Log.e(TAG, "Error loading initial data", e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(this, "Не удалось загрузить данные", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void handleTicketCreation(View v) {
        if (!validateForm()) {
            Toast.makeText(this, "Для создания билета необходимо заполнить все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        v.setEnabled(false);

        try {
            createTicketAndSaveToDatabase();

            if (CacheHelper.getFromCache(this, "WA", "false").equals("true")) {
                openWhatsApp(" ");
            }

            mainHandler.postDelayed(() -> v.setEnabled(true), 5000);

        } catch (Exception e) {
            Log.e(TAG, "Error creating ticket", e);
            Toast.makeText(this, "Ошибка при создании билета", Toast.LENGTH_SHORT).show();
            v.setEnabled(true);
        }
    }

    private boolean validateForm() {
        return !movie_name.getSelectedItem().toString().equals("Нет сеансов на эту дату.")
                && !movie_hall.getSelectedItem().toString().isEmpty()
                && !hall_row.getSelectedItem().toString().isEmpty()
                && !hall_places.getText().toString().isEmpty();
    }

    private void createTicketAndSaveToDatabase() {
        String date = movie_date.getSelectedItem().toString();
        createTicket(date, MAIN_FOLDER);

        SimpleDateFormat ticket_date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String ticketCost_str = CacheHelper.getFromCache(this, "cost", "120");

        String[] placesList = hall_places.getText().toString().split(",");
        int ticketAmount = placesList.length;
        int ticketCost = Integer.parseInt(ticketCost_str) * ticketAmount;

        databaseHelper.addTicket(
                movie_name.getSelectedItem().toString(),
                ticket_date.format(new Date()),
                hall_row.getSelectedItem().toString(),
                hall_places.getText().toString(),
                movie_hall.getSelectedItem().toString(),
                ticketCost,
                ticketAmount
        );
    }

    // === Методы загрузки данных ===

    public void loadDateList() {
        String cacheKey = "dates";
        if (dateCache.containsKey(cacheKey)) {
            updateDateSpinner(dateCache.get(cacheKey));
            return;
        }

        executor.execute(() -> {
            try {
                Document doc = Jsoup.connect(sourceURL)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .timeout(10000)
                        .get();

                Elements dateElements = doc.select("div.fs-14.cinema-day__date.fw-500");
                List<String> dates = new ArrayList<>();

                for (Element element : dateElements) {
                    dates.add(element.text());
                }

                dateCache.put(cacheKey, dates);
                updateDateSpinner(dates);

            } catch (IOException e) {
                Log.e(TAG, "Error loading date list", e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(this, "Ошибка загрузки дат", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateDateSpinner(List<String> dates) {
        mainHandler.post(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, dates);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            movie_date.setAdapter(adapter);
            hideProgressBar();
        });
    }

    public void loadFilmsOnDate(String date) {
        String cacheKey = "movies_" + date;
        if (movieCache.containsKey(cacheKey)) {
            updateMovieSpinner(movieCache.get(cacheKey));
            return;
        }

        executor.execute(() -> {
            try {
                String[] parseDate = formatDateForParser(date);
                if (parseDate == null) {
                    throw new IllegalArgumentException("Invalid date format: " + date);
                }

                String day = parseDate[0];
                String month = parseDate[1];
                String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

                String url = sourceURL + "?date=" + year + "-" + month + "-" + day;
                Log.d(TAG, "Loading movies from: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .timeout(10000)
                        .get();

                Elements movieElements = doc.select("div.col-sm-6.col-md-4.col-lg-3.movies-item");
                List<String> movies = new ArrayList<>();

                for (Element movieElement : movieElements) {
                    String movieName = movieElement.select("h6.h6.mb-2").text();
                    if (!movieName.isEmpty()) {
                        movies.add(movieName);
                    }
                }

                if (movies.isEmpty()) {
                    movies.add("Нет сеансов на эту дату.");
                }

                movieCache.put(cacheKey, movies);
                updateMovieSpinner(movies);

            } catch (Exception e) {
                Log.e(TAG, "Error loading films for date: " + date, e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(this, "Ошибка загрузки фильмов", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateMovieSpinner(List<String> movies) {
        mainHandler.post(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, movies);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            movie_name.setAdapter(adapter);
            hideProgressBar();
        });
    }

    public void loadMovieSession(String movieName, String date) {
        String cacheKey = "sessions_" + date + "_" + movieName;
        if (sessionCache.containsKey(cacheKey)) {
            updateSessionSpinner(sessionCache.get(cacheKey));
            return;
        }

        executor.execute(() -> {
            try {
                String[] parseDate = formatDateForParser(date);
                if (parseDate == null) {
                    throw new IllegalArgumentException("Invalid date format: " + date);
                }

                String day = parseDate[0];
                String month = parseDate[1];
                String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());

                String url = sourceURL + "?date=" + year + "-" + month + "-" + day;
                Log.d(TAG, "Loading sessions from: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .referrer(referrer)
                        .timeout(10000)
                        .get();

                Elements movieElements = doc.select("div.col-sm-6.col-md-4.col-lg-3.movies-item");
                Map<String, String[]> sessions = new HashMap<>();

                for (Element movieElement : movieElements) {
                    String name = movieElement.select(".h6.mb-2").text().trim();
                    String sessionText = movieElement.select(".session__time").text().trim();

                    if (!name.isEmpty() && !sessionText.isEmpty()) {
                        sessions.put(name, sessionText.split("\\s+"));
                    }
                }

                String[] movieTimes = sessions.get(movieName);
                if (movieTimes != null) {
                    sessionCache.put(cacheKey, movieTimes);
                    updateSessionSpinner(movieTimes);
                } else {
                    updateSessionSpinner(new String[0]);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading sessions for: " + movieName, e);
                mainHandler.post(() -> {
                    hideProgressBar();
                    Toast.makeText(this, "Ошибка загрузки сеансов", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateSessionSpinner(String[] sessions) {
        mainHandler.post(() -> {
            List<String> sessionList = sessions.length > 0 ?
                    Arrays.asList(sessions) : Collections.singletonList("Нет доступных сеансов");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, sessionList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            movie_time.setAdapter(adapter);
            hideProgressBar();
        });
    }

    // === Вспомогательные методы ===

    public String[] formatDateForParser(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        String[] dateStr = date.trim().split("\\s+");
        if (dateStr.length < 2) {
            return null;
        }

        String day = dateStr[0];
        String monthName = dateStr[1].toLowerCase();

        Map<String, String> monthMap = createMonthMap();
        String month = monthMap.get(monthName);

        if (month != null) {
            return new String[]{day, month};
        }

        Log.w(TAG, "Unknown month name: " + monthName);
        return null;
    }

    private Map<String, String> createMonthMap() {
        Map<String, String> monthMap = new HashMap<>();
        monthMap.put("января", "01"); monthMap.put("янв", "01");
        monthMap.put("февраля", "02"); monthMap.put("фев", "02");
        monthMap.put("марта", "03"); monthMap.put("мар", "03");
        monthMap.put("апреля", "04"); monthMap.put("апр", "04");
        monthMap.put("мая", "05");
        monthMap.put("июня", "06");
        monthMap.put("июля", "07");
        monthMap.put("августа", "08"); monthMap.put("авг", "08");
        monthMap.put("сентября", "09"); monthMap.put("сен", "09");
        monthMap.put("октября", "10"); monthMap.put("окт", "10");
        monthMap.put("ноября", "11"); monthMap.put("ноя", "11");
        monthMap.put("декабря", "12"); monthMap.put("дек", "12");
        return monthMap;
    }

    public void openWhatsApp(String message) {
        try {
            String url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(message, "UTF-8");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening WhatsApp", e);
            Toast.makeText(this, "Ошибка открытия WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    public static String[] splitStringByLastSpace(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return new String[]{input};
        }

        int lastSpaceIndex = input.substring(0, maxLength).lastIndexOf(' ');
        if (lastSpaceIndex == -1) {
            lastSpaceIndex = maxLength;
        }

        return new String[]{
                input.substring(0, lastSpaceIndex).trim(),
                input.substring(lastSpaceIndex).trim()
        };
    }

    private void createTicket(String date, String folderName) {
        String[] parseDate = formatDateForParser(date);
        if (parseDate == null) {
            Log.e(TAG, "Invalid date format for ticket creation: " + date);
            return;
        }

        String movie_date = parseDate[0] + "." + parseDate[1];
        String timestamp = new SimpleDateFormat("yyyy-h:m:s", Locale.getDefault()).format(new Date());

        TicketCreator ticket = new TicketCreator();
        Bitmap bitmap = ticket.createTicket_Movie(
                BG, cinema_logo, company_logo, divider, ticket_text,
                splitStringByLastSpace(movie_name.getSelectedItem().toString(), 20),
                movie_date, movie_time.getSelectedItem().toString(),
                hall_row.getSelectedItem().toString(), hall_places.getText().toString(),
                movie_hall.getSelectedItem().toString()
        );

        ticket.saveTicket(this, bitmap, movie_date, timestamp, folderName);
    }

    private void showProgressBar() {
        mainHandler.post(() -> progressBar.setVisibility(View.VISIBLE));
    }

    private void hideProgressBar() {
        mainHandler.post(() -> progressBar.setVisibility(View.GONE));
    }
}