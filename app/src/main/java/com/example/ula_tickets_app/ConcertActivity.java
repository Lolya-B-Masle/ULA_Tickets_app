package com.example.ula_tickets_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ConcertActivity extends AppCompatActivity {

    Bitmap company_logo, divider, BG, ticket_text;
    EditText concert_hall_field, concert_row_field, concert_place_field;
    TextView concert_date_field, concert_time_field;
    LinearLayout done_btn, clear_btn;
    private final String MAIN_FOLDER = "БИЛЕТЫ_НА_КОНЦЕРТЫ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_concert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        company_logo = BitmapFactory.decodeResource(getResources(), R.drawable.images_logo);
        divider = BitmapFactory.decodeResource(getResources(), R.drawable.images_divider);
        BG = BitmapFactory.decodeResource(getResources(), R.drawable.images_ticket_background);
        ticket_text = BitmapFactory.decodeResource(getResources(), R.drawable.images_ticket_text);

        concert_hall_field = findViewById(R.id.concert_hall_field);
        concert_row_field = findViewById(R.id.concert_row_field);
        concert_place_field = findViewById(R.id.concert_place_field);

        concert_date_field = findViewById(R.id.concert_date_field);
        concert_time_field = findViewById(R.id.concert_time_field);

        done_btn = findViewById(R.id.done_btn);
        clear_btn = findViewById(R.id.clear_btn);

        clear_btn.setOnClickListener(v->{
            try {
                concert_hall_field.setText(null);
                concert_row_field.setText(null);
                concert_place_field.setText(null);

                concert_hall_field.setHint(R.string.default_hint);
                concert_row_field.setHint(R.string.default_hint);
                concert_place_field.setHint(R.string.default_hint);
            } catch (Exception e) {}
        });

        done_btn.setOnClickListener(v->{
            if (concert_hall_field.getText().toString().isEmpty() || concert_row_field.getText().toString().isEmpty() || concert_place_field.getText().toString().isEmpty()
                || concert_date_field.equals(R.string.dateTime_val_hint) || concert_time_field.equals(R.string.dateTime_val_hint)) {
                Toast.makeText(this, "Для создания билета необходимо заполнить все поля", Toast.LENGTH_SHORT).show();
            } else {
                createTicket(MAIN_FOLDER);
            }
        });

        concert_date_field.setOnClickListener(v->{
            showDatePickerDialog();
        });

        concert_time_field.setOnClickListener(v->{
            showTimePickerDialog();
        });

    }

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
        concert_date_field.setText(formattedDate);
        concert_date_field.setTextSize(24);
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
        concert_time_field.setText(formattedTime);
        concert_time_field.setTextSize(24);
    }

    // --------------------------------------------- Image creator -----------------------

    private void createTicket(String folderName) {
        TicketCreator ticket = new TicketCreator();
        Bitmap bitmap = ticket.createTicket_Concert(BG, company_logo, divider, ticket_text, concert_date_field.getText().toString(), concert_time_field.getText().toString(),
                concert_row_field.getText().toString(), concert_place_field.getText().toString(), concert_hall_field.getText().toString());
        ticket.saveTicket(this, bitmap, concert_date_field.getText().toString(), concert_time_field.getText().toString(), folderName);
    }


}