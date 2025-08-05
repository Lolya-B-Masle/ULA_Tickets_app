package com.example.ula_tickets_app;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    String TICKET_COST_KEY = "cost";
    String IS_DIP_KEY = "isDip";
    String WA_OPEN_KEY = "WA";
    String isWAKey_status = "";

    boolean isChecked = false;
    LinearLayout exit_btn, save_btn;
    CheckBox WA_open;
    EditText ticketCost;


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
        exit_btn = findViewById(R.id.back_btn);

        ticketCost = findViewById(R.id.ticketCost_field);

        WA_open = findViewById(R.id.WA_open);

        exit_btn.setOnClickListener(v ->  finish());

        loadDataFromCache(this);

        WA_open.setOnClickListener(v -> {
            isChecked = ((CheckBox) v).isChecked();
            if (isChecked) {
                CacheHelper.saveToCache(getApplicationContext(), WA_OPEN_KEY, "true");
            } else {
                CacheHelper.saveToCache(getApplicationContext(), WA_OPEN_KEY, "false");
            }
        });

        save_btn.setOnClickListener(O->{
            //CacheHelper.saveToCache(getApplicationContext(), PHONE_KEY, phone.getText().toString());
            CacheHelper.saveToCache(getApplicationContext(), TICKET_COST_KEY, ticketCost.getText().toString());
            Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
        });

    }

    public void loadDataFromCache(Context context) {
        ticketCost.setText(CacheHelper.getFromCache(context, TICKET_COST_KEY, ""));

        isWAKey_status = CacheHelper.getFromCache(context, WA_OPEN_KEY, "false");
        if (isWAKey_status.equals("false"))
            WA_open.setChecked(false);
        else if (isWAKey_status.equals("true"))
            WA_open.setChecked(true);
    }
}