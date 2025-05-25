package com.example.ula_tickets_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    Button exit_btn, clear_btn;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    TextView status;
    ListView item_list;

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseHelper = new DatabaseHelper(getApplicationContext());

        exit_btn = findViewById(R.id.exit_btn);
        clear_btn = findViewById(R.id.clear_btn);
        item_list = findViewById(R.id.ticketsList);
        status = findViewById(R.id.status);

        exit_btn.setOnClickListener(v ->  finish());
        clear_btn.setOnClickListener(v -> showClearConfirmationDialog());

        db = databaseHelper.getReadableDatabase();

        loadData();
    }

    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы действительно хотите удалить все записи? Это действие нельзя отменить.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    databaseHelper.clearTable();
                    status.setText("Нет билетов...");
                    Toast.makeText(getApplicationContext(), "Все записи удалены", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void loadData() {
        int cost = 0;
        int ticketsAmount = 0;
        String splitLine = "-".repeat(75);

        Cursor cursor = databaseHelper.getAllTickets();
        ArrayList<String> TicketsList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            TicketsList.add("Записей нет");
        } else {
            while (cursor.moveToNext()) {
                String movie_name = cursor.getString(1);
                String date = cursor.getString(2);
                String row = cursor.getString(3);
                String place = cursor.getString(4);
                String hall = cursor.getString(5);
                String ticketCost = cursor.getString(6);
                TicketsList.add(splitLine+" "+ticketCost+"₽\n"+movie_name+": "+date+"\n"+row+" р, м "+place+", зал "+hall+'\n'+splitLine);
                ticketsAmount++;
                cost+=Integer.parseInt(ticketCost);
            }
            Collections.reverse(TicketsList);
            status.setText("Всего билетов: " + ticketsAmount + " на сумму " + cost + " руб.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                TicketsList
        );
        item_list.setAdapter(adapter);
    }
}