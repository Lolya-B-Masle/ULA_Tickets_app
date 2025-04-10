package com.example.ula_tickets_app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView movie_name, movie_hall, movie_date, movie_time, hall_row, hall_places;
    Button done_btn, clear_btn, history_btn;
    Bitmap company_logo, cinema_logo, divider, BG;

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

        company_logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        cinema_logo = BitmapFactory.decodeResource(getResources(), R.drawable.rainbow_logo);
        divider = BitmapFactory.decodeResource(getResources(), R.drawable.divider);
        BG = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        done_btn = findViewById(R.id.done_btn);
        clear_btn = findViewById(R.id.clear_btn);
        history_btn = findViewById(R.id.history_btn);

        TextView[] fields = {
            movie_name = findViewById(R.id.movie_name_field),
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
            Toast.makeText(this, "Опция находится в разработке...", Toast.LENGTH_LONG).show();
        });

        done_btn.setOnClickListener(v -> {
            createPDF();
            openPDF();
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

    // --------------------------------------------- PDF creator -----------------------

    private void createPDF() {
        PDFCreator PDF = new PDFCreator();

        PDF.setBG(BG);

        PDF.setCinemaLogo(cinema_logo);
        PDF.setCompanyLogo(company_logo);
        PDF.setDivider(divider);

        PDF.setMovieName(splitStringByLastSpace(movie_name.getText().toString(), 20));
        PDF.setMovieDateTime(movie_date.getText().toString(), movie_time.getText().toString());
        PDF.setMoviePlace(movie_hall.getText().toString(), hall_row.getText().toString(), hall_places.getText().toString());

        PDF.setTicket_date();

        PDF.createPDF(getApplicationContext());
    }

    private void openPDF() {
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/TU.pdf";
        PDFOpener.openPdf(MainActivity.this, pdfPath);
    }
}