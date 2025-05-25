package com.example.ula_tickets_app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_settings extends AppCompatActivity {
    String PHONE_KEY = "phone";
    String TICKET_COST_KEY = "cost";
    String IS_DIP_KEY = "isDip";
    String WA_OPEN_KEY = "WA";
    String isDipKey_status = "";
    String isWAKey_status = "";

    boolean isChecked = false;

    Button exit_btn, save_btn;
    CheckBox isDip, WA_open;
    EditText phone, ticketCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        save_btn = findViewById(R.id.save_btn);
        exit_btn = findViewById(R.id.exit_btn);

        isDip = findViewById(R.id.isDit_radio);
        WA_open = findViewById(R.id.WA_open);
        phone = findViewById(R.id.phone_field);
        ticketCost = findViewById(R.id.ticketCost_field);

        exit_btn.setOnClickListener(v ->  finish());

        loadDataFromCache(this);

        isDip.setOnClickListener(v -> {
            isChecked = ((CheckBox) v).isChecked();
            if (isChecked) {
                CacheHelper.saveToCache(getApplicationContext(), IS_DIP_KEY, "true");
                Toast.makeText(this, "Номер зала спрятан, перезагрузите приложение!", Toast.LENGTH_SHORT).show();
            } else {
                CacheHelper.saveToCache(getApplicationContext(), IS_DIP_KEY, "false");
                Toast.makeText(this, "Номер зала доступен, перезагрузите приложение!", Toast.LENGTH_SHORT).show();
            }
        });

        WA_open.setOnClickListener(v -> {
            isChecked = ((CheckBox) v).isChecked();
            if (isChecked) {
                CacheHelper.saveToCache(getApplicationContext(), WA_OPEN_KEY, "true");
            } else {
                CacheHelper.saveToCache(getApplicationContext(), WA_OPEN_KEY, "false");
            }
        });

        save_btn.setOnClickListener(O->{
            CacheHelper.saveToCache(getApplicationContext(), PHONE_KEY, phone.getText().toString());
            CacheHelper.saveToCache(getApplicationContext(), TICKET_COST_KEY, ticketCost.getText().toString());
            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
        });

    }

    public void loadDataFromCache(Context context) {
        phone.setText(CacheHelper.getFromCache(context, PHONE_KEY, ""));
        ticketCost.setText(CacheHelper.getFromCache(context, TICKET_COST_KEY, ""));

        isDipKey_status = CacheHelper.getFromCache(context, IS_DIP_KEY, "false");
        if (isDipKey_status.equals("false"))
            isDip.setChecked(false);
        else if (isDipKey_status.equals("true"))
            isDip.setChecked(true);

        isWAKey_status = CacheHelper.getFromCache(context, WA_OPEN_KEY, "false");
        if (isWAKey_status.equals("false"))
            WA_open.setChecked(false);
        else if (isWAKey_status.equals("true"))
            WA_open.setChecked(true);
    }
}