package com.example.ula_tickets_app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView movie_name, movie_hall, movie_date, movie_time, hall_row, hall_places;
    Button done_btn, clear_btn;
    Bitmap bmp, bmp_r, divider, bg, scaledDivider, scaledBitmap, scaledBitmap_r, scaledBg;

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

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        bmp_r = BitmapFactory.decodeResource(getResources(), R.drawable.rainbow_logo);
        divider = BitmapFactory.decodeResource(getResources(), R.drawable.divider);
        bg = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        scaledBitmap = Bitmap.createScaledBitmap(bmp, 448, 345, false);
        scaledBitmap_r = Bitmap.createScaledBitmap(bmp_r, 520, 520, false);
        scaledDivider = Bitmap.createScaledBitmap(divider, 920, 150, false);
        scaledBg = Bitmap.createScaledBitmap(bg, 1080, 1920, false);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        done_btn = findViewById(R.id.done_btn);
        clear_btn = findViewById(R.id.clear_btn);

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

        done_btn.setOnClickListener(v -> {
            try {
                createPDF(
                        movie_name.getText().toString(),
                        movie_hall.getText().toString(),
                        movie_date.getText().toString(),
                        movie_time.getText().toString(),
                        hall_row.getText().toString(),
                        hall_places.getText().toString()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // --------------------------------------------- Date picker -----------------------

    private Calendar selectedDate = Calendar.getInstance();

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

    private Calendar selectedTime = Calendar.getInstance();
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
        if (input == null || input.length() <= maxLength) {
            return new String[]{input};
        }

        int lastSpaceIndex = input.substring(0, maxLength).lastIndexOf(' ');

        if (lastSpaceIndex == -1) {
            lastSpaceIndex = maxLength;
        }

        String firstPart = input.substring(0, lastSpaceIndex).trim();
        String secondPart = input.substring(lastSpaceIndex).trim();

        return new String[]{firstPart, secondPart};
    }

    // --------------------------------------------- PDF creator -----------------------

    private void createPDF(String name, String hall, String date, String time, String row, String place) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat year = new SimpleDateFormat(".yyyy", Locale.getDefault());
        byte ML = 20;
        String extra_name = "";


        if (name.length() > ML) {
            String[] formatName = splitStringByLastSpace(name, ML);
            name = formatName[0];
            extra_name = formatName[1];
        }

        String name_str = "НАЗВАНИЕ ФИЛЬМА";
        String hall_str = "ЗАЛ:    ";
        String date_str = "ДАТА:    ";
        String time_str = "ВРЕМЯ:    ";
        String row_str = "РЯД:    ";
        String place_str = "МЕСТО:    ";
        String ticketDate = "Билет от:  " + sdf.format(now);

        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, 1).create();
        PdfDocument.Page page_1 = document.startPage(pageInfo);

        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Ticket.pdf";

        Canvas canvas = page_1.getCanvas();

        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextSize(35);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(80);

        Paint detailsPaint = new Paint();
        detailsPaint.setColor(Color.LTGRAY);
        detailsPaint.setTextSize(35);

        canvas.drawBitmap(scaledBg, 0, 0, labelPaint);

        canvas.drawBitmap(scaledBitmap_r, 0, -34, labelPaint);
        canvas.drawBitmap(scaledBitmap, 585, 10, labelPaint);

        canvas.drawText(ticketDate, 12, 35, detailsPaint);

        canvas.drawText(name_str, 60, 560, labelPaint);
        canvas.drawText(name, 60, 660, textPaint);
        canvas.drawText(extra_name, 60, 760, textPaint);

        canvas.drawText(date_str, 60, 920, labelPaint);
        canvas.drawText(date + year.format(now), 210, 920, textPaint);

        canvas.drawText(time_str, 60, 1020, labelPaint);
        canvas.drawText(time, 210, 1020, textPaint);

        canvas.drawText(hall_str, 60, 1220, labelPaint);
        canvas.drawText(hall, 170, 1220, textPaint);

        canvas.drawText(row_str, 60, 1320, labelPaint);
        canvas.drawText(row, 170, 1320, textPaint);

        canvas.drawText(place_str, 315, 1320, labelPaint);
        canvas.drawText(place, 465, 1320, textPaint);

        canvas.drawBitmap(scaledDivider, 80, 370, labelPaint);

        document.finishPage(page_1);

        File file = new File(downloadDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            Toast.makeText(this, "done!!", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Ticket.pdf";
        PDFOpener.openPdf(MainActivity.this, pdfPath);
    }
}