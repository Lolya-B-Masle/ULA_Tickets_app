package com.example.ula_tickets_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private LinearLayout toMoviePageBtn, toConcertPageBtn;
    private ImageView toSettingsPageBtn, toInfoPageBtn;

    // Кэш для Intent'ов чтобы избежать повторного создания
    private Intent movieIntent;
    private Intent concertIntent;
    private Intent settingsIntent;

    // Флаг для предотвращения множественных быстрых нажатий
    private boolean isClickInProgress = false;
    private static final long CLICK_DELAY = 500; // мс

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        setContentView(R.layout.activity_home);
        setupWindowInsets();

        initializeViews();
        setupClickListeners();

        // Предварительное создание Intent'ов для улучшения отзывчивости
        preCreateIntents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Сброс флага при возвращении на экран
        isClickInProgress = false;
    }

    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        toMoviePageBtn = findViewById(R.id.MovieOptionContainer);
        toConcertPageBtn = findViewById(R.id.ConcertOptionContainer);
        toSettingsPageBtn = findViewById(R.id.SettingsButton);
        toInfoPageBtn = findViewById(R.id.InfoButton);
    }

    private void preCreateIntents() {
        // Предварительное создание Intent'ов для уменьшения задержек при нажатии
        movieIntent = new Intent(HomeActivity.this, MovieActivity.class);
        concertIntent = new Intent(HomeActivity.this, ConcertActivity.class);
        settingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);
    }

    private void setupClickListeners() {
        // Используем лямбды с защитой от множественных нажатий
        toMoviePageBtn.setOnClickListener(v -> handleMovieClick());
        toConcertPageBtn.setOnClickListener(v -> handleConcertClick());
        toSettingsPageBtn.setOnClickListener(v -> handleSettingsClick());
        toInfoPageBtn.setOnClickListener(v -> handleInfoClick());
    }

    private synchronized boolean shouldHandleClick() {
        if (isClickInProgress) {
            Log.d(TAG, "Click ignored - previous click still processing");
            return false;
        }
        isClickInProgress = true;
        return true;
    }

    private void resetClickFlag() {
        // Сбрасываем флаг после задержки для предотвращения спама
        toMoviePageBtn.postDelayed(() -> isClickInProgress = false, CLICK_DELAY);
    }

    private void handleMovieClick() {
        if (!shouldHandleClick()) return;

        try {
            Log.d(TAG, "Navigate to MovieActivity");
            startActivity(movieIntent);
            // Не вызываем finish() чтобы пользователь мог вернуться назад
        } catch (Exception e) {
            Log.e(TAG, "Error starting MovieActivity", e);
            isClickInProgress = false;
        } finally {
            resetClickFlag();
        }
    }

    private void handleConcertClick() {
        if (!shouldHandleClick()) return;

        try {
            Log.d(TAG, "Navigate to ConcertActivity");
            startActivity(concertIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting ConcertActivity", e);
            isClickInProgress = false;
        } finally {
            resetClickFlag();
        }
    }

    private void handleSettingsClick() {
        if (!shouldHandleClick()) return;

        try {
            Log.d(TAG, "Navigate to SettingsActivity");
            startActivity(settingsIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting SettingsActivity", e);
            isClickInProgress = false;
        } finally {
            resetClickFlag();
        }
    }

    private void handleInfoClick() {
        if (!shouldHandleClick()) return;

        try {
            Log.d(TAG, "Info button clicked - placeholder for future implementation");
            // TODO: Реализовать функциональность Info activity
            // Intent infoIntent = new Intent(HomeActivity.this, InfoActivity.class);
            // startActivity(infoIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error handling info click", e);
        } finally {
            resetClickFlag();
        }
    }

    @Override
    public void onBackPressed() {
        // Стандартное поведение - выход из приложения
        super.onBackPressed();
        // Альтернативно можно показать диалог подтверждения выхода:
        // showExitConfirmationDialog();
    }

    // Дополнительный метод для чистки ресурсов если нужно
    @Override
    protected void onDestroy() {
        // Очистка ссылок для помощи GC
        toMoviePageBtn = null;
        toConcertPageBtn = null;
        toSettingsPageBtn = null;
        toInfoPageBtn = null;

        movieIntent = null;
        concertIntent = null;
        settingsIntent = null;

        super.onDestroy();
    }
}