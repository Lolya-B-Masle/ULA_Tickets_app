package com.example.ula_tickets_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcertActivity extends AppCompatActivity {
    private static final String TAG = "ConcertActivity";

    private Bitmap company_logo, divider, BG, ticket_text;
    private EditText concert_hall_field, concert_row_field, concert_place_field;
    private TextView concert_date_field, concert_time_field;
    private LinearLayout done_btn, clear_btn;

    private final String MAIN_FOLDER = "БИЛЕТЫ_НА_КОНЦЕРТЫ";
    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar selectedTime = Calendar.getInstance();

    // Форматтеры для повторного использования
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());

    // Флаг для предотвращения множественных кликов
    private final AtomicBoolean isCreatingTicket = new AtomicBoolean(false);
    private static final long CLICK_DELAY_MS = 1000;

    // Кэш для ресурсов чтобы избежать многократных вызовов
    private String defaultHint;
    private String dateTimeHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        setContentView(R.layout.activity_concert);
        setupWindowInsets();

        initializeResources();
        initializeViews();
        setupClickListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleBitmaps();
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

    private void initializeResources() {
        // Кэшируем строковые ресурсы
        defaultHint = getString(R.string.default_hint);
        dateTimeHint = getString(R.string.dateTime_val_hint);

        // Ленивая загрузка Bitmap
        loadBitmaps();
    }

    private void loadBitmaps() {
        company_logo = BitmapFactory.decodeResource(getResources(), R.drawable.images_logo);
        divider = BitmapFactory.decodeResource(getResources(), R.drawable.images_divider);
        BG = BitmapFactory.decodeResource(getResources(), R.drawable.images_ticket_background);
        ticket_text = BitmapFactory.decodeResource(getResources(), R.drawable.images_ticket_text);
    }

    private void recycleBitmaps() {
        if (company_logo != null && !company_logo.isRecycled()) {
            company_logo.recycle();
        }
        if (divider != null && !divider.isRecycled()) {
            divider.recycle();
        }
        if (BG != null && !BG.isRecycled()) {
            BG.recycle();
        }
        if (ticket_text != null && !ticket_text.isRecycled()) {
            ticket_text.recycle();
        }
    }

    private void initializeViews() {
        concert_hall_field = findViewById(R.id.concert_hall_field);
        concert_row_field = findViewById(R.id.concert_row_field);
        concert_place_field = findViewById(R.id.concert_place_field);

        concert_date_field = findViewById(R.id.concert_date_field);
        concert_time_field = findViewById(R.id.concert_time_field);

        done_btn = findViewById(R.id.done_btn);
        clear_btn = findViewById(R.id.clear_btn);

        // Устанавливаем начальные значения для даты и времени
        updateSelectedDateText();
        updateSelectedTimeText();
    }

    private void setupClickListeners() {
        clear_btn.setOnClickListener(this::handleClearClick);
        done_btn.setOnClickListener(this::handleDoneClick);
        concert_date_field.setOnClickListener(v -> showDatePickerDialog());
        concert_time_field.setOnClickListener(v -> showTimePickerDialog());
    }

    private void handleClearClick(View v) {
        clearFormFields();
    }

    private void clearFormFields() {
        concert_hall_field.setText("");
        concert_row_field.setText("");
        concert_place_field.setText("");

        // Восстанавливаем подсказки
        concert_hall_field.setHint(defaultHint);
        concert_row_field.setHint(defaultHint);
        concert_place_field.setHint(defaultHint);

        // Сбрасываем дату и время на текущие
        selectedDate.setTimeInMillis(System.currentTimeMillis());
        selectedTime.setTimeInMillis(System.currentTimeMillis());
        updateSelectedDateText();
        updateSelectedTimeText();

        Toast.makeText(this, "Форма очищена", Toast.LENGTH_SHORT).show();
    }

    private void handleDoneClick(View v) {
        if (!isCreatingTicket.compareAndSet(false, true)) {
            return; // Уже обрабатывается клик
        }

        try {
            if (validateForm()) {
                createTicket(MAIN_FOLDER);
                Toast.makeText(this, "Билет создан успешно", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Для создания билета необходимо заполнить все поля", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating ticket", e);
            Toast.makeText(this, "Ошибка при создании билета", Toast.LENGTH_SHORT).show();
        } finally {
            // Разблокируем кнопку после задержки
            done_btn.postDelayed(() -> isCreatingTicket.set(false), CLICK_DELAY_MS);
        }
    }

    private boolean validateForm() {
        return !concert_hall_field.getText().toString().trim().isEmpty() &&
                !concert_row_field.getText().toString().trim().isEmpty() &&
                !concert_place_field.getText().toString().trim().isEmpty() &&
                !concert_date_field.getText().toString().equals(dateTimeHint) &&
                !concert_time_field.getText().toString().equals(dateTimeHint);
    }

    // --------------------------------------------- Date Picker -----------------------

    private void showDatePickerDialog() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this::onDateSet,
                year, month, day
        );

        // Устанавливаем минимальную дату - сегодня
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    private void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDate.set(year, month, dayOfMonth);
        updateSelectedDateText();
    }

    private void updateSelectedDateText() {
        String formattedDate = dateFormat.format(selectedDate.getTime());
        concert_date_field.setText(formattedDate);
        concert_date_field.setTextSize(24);
    }

    // --------------------------------------------- Time Picker -----------------------

    private void showTimePickerDialog() {
        int hour = selectedTime.get(Calendar.HOUR_OF_DAY);
        int minute = selectedTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                this::onTimeSet,
                hour, minute, true // 24-часовой формат
        );

        timePickerDialog.show();
    }

    private void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        selectedTime.set(Calendar.MINUTE, minute);
        updateSelectedTimeText();
    }

    private void updateSelectedTimeText() {
        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE));
        concert_time_field.setText(formattedTime);
        concert_time_field.setTextSize(24);
    }

    // --------------------------------------------- Image Creator -----------------------

    private void createTicket(String folderName) {
        try {
            TicketCreator ticket = new TicketCreator();

            String date = concert_date_field.getText().toString();
            String time = concert_time_field.getText().toString();
            String row = concert_row_field.getText().toString();
            String place = concert_place_field.getText().toString();
            String hall = concert_hall_field.getText().toString();

            Bitmap bitmap = ticket.createTicket_Concert(
                    BG, company_logo, divider, ticket_text,
                    date, time, row, place, hall
            );

            if (bitmap != null) {
                ticket.saveTicket(this, bitmap, date, time, folderName);
            } else {
                throw new IllegalStateException("Failed to create ticket bitmap");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in createTicket method", e);
            throw new RuntimeException("Ticket creation failed", e);
        }
    }

    // --------------------------------------------- Utility Methods -----------------------

    /**
     * Проверяет корректность введенной даты (не в прошлом)
     */
    private boolean isDateValid() {
        Calendar now = Calendar.getInstance();
        return !selectedDate.before(now);
    }

    /**
     * Проверяет корректность введенного времени
     */
    private boolean isTimeValid() {
        Calendar now = Calendar.getInstance();
        if (selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.MONTH) == now.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
            // Если дата сегодняшняя, проверяем чтобы время не было в прошлом
            return selectedTime.after(now);
        }
        return true;
    }
}