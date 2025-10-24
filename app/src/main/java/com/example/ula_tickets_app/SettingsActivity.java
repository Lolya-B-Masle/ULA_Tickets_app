package com.example.ula_tickets_app;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    // Ключи для кэша
    private static final String TICKET_COST_KEY = "cost";
    private static final String WA_OPEN_KEY = "WA";

    // Views
    private LinearLayout exit_btn, save_btn;
    private CheckBox WA_open;
    private EditText ticketCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        setContentView(R.layout.activity_settings);
        setupWindowInsets();

        initializeViews();
        setupClickListeners();
        loadDataFromCache();
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
        save_btn = findViewById(R.id.save_btn);
        exit_btn = findViewById(R.id.back_btn);
        ticketCost = findViewById(R.id.ticketCost_field);
        WA_open = findViewById(R.id.WA_open);
    }

    private void setupClickListeners() {
        exit_btn.setOnClickListener(v -> finish());

        WA_open.setOnClickListener(v -> {
            boolean isChecked = ((CheckBox) v).isChecked();
            String value = isChecked ? "true" : "false";
            CacheHelper.saveToCache(this, WA_OPEN_KEY, value);
        });

        save_btn.setOnClickListener(v -> handleSaveClick());
    }

    private void handleSaveClick() {
        String costText = ticketCost.getText().toString().trim();

        if (TextUtils.isEmpty(costText)) {
            Toast.makeText(this, "Введите стоимость билета", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Валидация что введено число
            int cost = Integer.parseInt(costText);
            if (cost <= 0) {
                Toast.makeText(this, "Стоимость должна быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }

            CacheHelper.saveToCache(this, TICKET_COST_KEY, costText);
            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректную стоимость", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataFromCache() {
        // Загружаем стоимость билета с значением по умолчанию "120"
        String cachedCost = CacheHelper.getFromCache(this, TICKET_COST_KEY, "120");
        ticketCost.setText(cachedCost);

        // Устанавливаем курсор в конец текста
        if (!TextUtils.isEmpty(cachedCost)) {
            ticketCost.setSelection(cachedCost.length());
        }

        // Загружаем статус WhatsApp
        String waStatus = CacheHelper.getFromCache(this, WA_OPEN_KEY, "false");
        WA_open.setChecked("true".equals(waStatus));
    }
}