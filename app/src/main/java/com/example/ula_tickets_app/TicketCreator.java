package com.example.ula_tickets_app;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketCreator {
    private static final String TAG = "TicketCreator";

    // Константы для размеров и позиций
    private static final int TICKET_WIDTH = 1080;
    private static final int TICKET_HEIGHT = 1920;
    private static final int COMPRESSION_QUALITY = 90;

    // Позиции элементов
    private static final int CINEMA_LOGO_SIZE = 520;
    private static final int CINEMA_LOGO_Y_OFFSET = -34;
    private static final int COMPANY_LOGO_WIDTH = 448;
    private static final int COMPANY_LOGO_HEIGHT = 345;
    private static final int COMPANY_LOGO_X = 585;
    private static final int COMPANY_LOGO_Y = 10;
    private static final int DIVIDER_WIDTH = 920;
    private static final int DIVIDER_HEIGHT = 150;
    private static final int DIVIDER_X = 80;
    private static final int DIVIDER_Y = 370;
    private static final int DEC_TEXT_WIDTH = 704;
    private static final int DEC_TEXT_HEIGHT = 445;
    private static final int DEC_TEXT_X = 60;
    private static final int DEC_TEXT_Y = 1470;

    // Текстовые позиции
    private static final int TEXT_START_X = 60;
    private static final int TEXT_VALUE_X = 260;
    private static final int TITLE_Y = 560;
    private static final int MOVIE_NAME_FIRST_Y = 660;
    private static final int MOVIE_NAME_SECOND_Y = 760;
    private static final int DATE_LABEL_Y = 920;
    private static final int TIME_LABEL_Y = 1040;
    private static final int HALL_LABEL_Y = 1230;
    private static final int ROW_LABEL_Y = 1350;
    private static final int SEAT_LABEL_Y = 1470;
    private static final int TIMESTAMP_Y = 35;
    private static final int TIMESTAMP_X = 12;

    // Размеры текста
    private static final int TEXT_SIZE_SMALL = 35;
    private static final int TEXT_SIZE_MEDIUM = 45;
    private static final int TEXT_SIZE_LARGE = 80;
    private static final int TEXT_SIZE_SMALLER = 65;

    private final Paint paint = new Paint();
    private final SimpleDateFormat ticketDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat yearFormat = new SimpleDateFormat(".yyyy", Locale.getDefault());

    public Bitmap createTicket_Movie(Bitmap background, Bitmap cinemaLogo, Bitmap companyLogo,
                                     Bitmap divider, Bitmap decorationText, String[] movieTitle,
                                     String date, String time, String row, String seat, String hall) {
        Bitmap bitmap = createBaseBitmap(background);
        Canvas canvas = new Canvas(bitmap);

        drawCinemaLogo(cinemaLogo, canvas);
        drawCompanyLogo(companyLogo, canvas);
        drawDivider(divider, canvas);
        drawDecorationText(decorationText, canvas);

        drawMovieTitle(movieTitle, canvas);
        drawDateTime(date, time, canvas);
        drawPlaceInfo(hall, row, seat, canvas);
        drawTimestamp(canvas);

        return bitmap;
    }

    public Bitmap createTicket_Concert(Bitmap background, Bitmap companyLogo, Bitmap divider,
                                       Bitmap decorationText, String date, String time,
                                       String row, String seat, String hall) {
        Bitmap bitmap = createBaseBitmap(background);
        Canvas canvas = new Canvas(bitmap);

        drawCompanyLogo(companyLogo, canvas);
        drawDivider(divider, canvas);
        drawDecorationText(decorationText, canvas);

        drawDateTime(date, time, canvas);
        drawPlaceInfo(hall, row, seat, canvas);
        drawTimestamp(canvas);

        return bitmap;
    }

    public void saveTicket(Context context, Bitmap bitmap, String date, String time, String folder) {
        if (bitmap == null || bitmap.isRecycled()) {
            Log.e(TAG, "Cannot save null or recycled bitmap");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Билет_" + date + "_" + time + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + folder);

        try {
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Log.e(TAG, "Failed to create URI for saving ticket");
                return;
            }

            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, out);
                    showSuccessToast(context, folder);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving ticket to gallery", e);
            Toast.makeText(context, "Ошибка сохранения билета", Toast.LENGTH_SHORT).show();
        }
    }

    // Private helper methods
    private Bitmap createBaseBitmap(Bitmap background) {
        return Bitmap.createScaledBitmap(background, TICKET_WIDTH, TICKET_HEIGHT, false);
    }

    private void drawCinemaLogo(Bitmap logo, Canvas canvas) {
        if (logo != null && !logo.isRecycled()) {
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, CINEMA_LOGO_SIZE, CINEMA_LOGO_SIZE, false);
            canvas.drawBitmap(scaledLogo, 0, CINEMA_LOGO_Y_OFFSET, paint);
        }
    }

    private void drawCompanyLogo(Bitmap logo, Canvas canvas) {
        if (logo != null && !logo.isRecycled()) {
            Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, COMPANY_LOGO_WIDTH, COMPANY_LOGO_HEIGHT, false);
            canvas.drawBitmap(scaledLogo, COMPANY_LOGO_X, COMPANY_LOGO_Y, paint);
        }
    }

    private void drawDivider(Bitmap divider, Canvas canvas) {
        if (divider != null && !divider.isRecycled()) {
            Bitmap scaledDivider = Bitmap.createScaledBitmap(divider, DIVIDER_WIDTH, DIVIDER_HEIGHT, false);
            canvas.drawBitmap(scaledDivider, DIVIDER_X, DIVIDER_Y, paint);
        }
    }

    private void drawDecorationText(Bitmap decorationText, Canvas canvas) {
        if (decorationText != null && !decorationText.isRecycled()) {
            Bitmap scaledText = Bitmap.createScaledBitmap(decorationText, DEC_TEXT_WIDTH, DEC_TEXT_HEIGHT, false);
            canvas.drawBitmap(scaledText, DEC_TEXT_X, DEC_TEXT_Y, paint);
        }
    }

    private void drawMovieTitle(String[] movieTitle, Canvas canvas) {
        if (movieTitle == null || movieTitle.length == 0) return;

        paint.setColor(Color.BLACK);
        paint.setTextSize(TEXT_SIZE_SMALL);
        canvas.drawText("НАЗВАНИЕ ФИЛЬМА", TEXT_START_X, TITLE_Y, paint);

        paint.setTextSize(TEXT_SIZE_LARGE);
        paint.setFakeBoldText(true);

        // Первая строка названия
        String firstLine = movieTitle[0].toUpperCase().trim();
        canvas.drawText(firstLine, TEXT_START_X, MOVIE_NAME_FIRST_Y, paint);

        // Вторая строка названия (если есть)
        if (movieTitle.length > 1 && !movieTitle[1].isEmpty()) {
            String secondLine = movieTitle[1].toUpperCase().trim();
            int textSize = secondLine.length() > 18 ? TEXT_SIZE_SMALLER : TEXT_SIZE_LARGE;
            paint.setTextSize(textSize);
            canvas.drawText(secondLine, TEXT_START_X, MOVIE_NAME_SECOND_Y, paint);
        }

        paint.setFakeBoldText(false);
    }

    private void drawTimestamp(Canvas canvas) {
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(TEXT_SIZE_SMALL);
        canvas.drawText(ticketDateFormat.format(new Date()), TIMESTAMP_X, TIMESTAMP_Y, paint);
    }

    private void drawDateTime(String date, String time, Canvas canvas) {
        paint.setColor(Color.BLACK);

        // Метки
        paint.setTextSize(TEXT_SIZE_MEDIUM);
        canvas.drawText("ДАТА:", TEXT_START_X, DATE_LABEL_Y, paint);
        canvas.drawText("ВРЕМЯ:", TEXT_START_X, TIME_LABEL_Y, paint);

        // Значения
        paint.setTextSize(TEXT_SIZE_LARGE);
        paint.setFakeBoldText(true);
        canvas.drawText(date + yearFormat.format(new Date()), TEXT_VALUE_X, DATE_LABEL_Y, paint);
        canvas.drawText(time, TEXT_VALUE_X, TIME_LABEL_Y, paint);
        paint.setFakeBoldText(false);
    }

    private void drawPlaceInfo(String hall, String row, String seat, Canvas canvas) {
        paint.setColor(Color.BLACK);

        // Метки
        paint.setTextSize(TEXT_SIZE_MEDIUM);
        canvas.drawText("ЗАЛ:", TEXT_START_X, HALL_LABEL_Y, paint);
        canvas.drawText("РЯД:", TEXT_START_X, ROW_LABEL_Y, paint);
        canvas.drawText("МЕСТО:", TEXT_START_X, SEAT_LABEL_Y, paint);

        // Значения
        paint.setTextSize(TEXT_SIZE_LARGE);
        paint.setFakeBoldText(true);

        // Зал с проверкой на длинный текст
        if ("уточнять у кассира".equals(hall)) {
            paint.setTextSize(TEXT_SIZE_SMALLER);
        }
        canvas.drawText(hall.trim(), TEXT_VALUE_X, HALL_LABEL_Y, paint);

        // Ряд и место
        paint.setTextSize(TEXT_SIZE_LARGE);
        canvas.drawText(row.trim(), TEXT_VALUE_X, ROW_LABEL_Y, paint);
        canvas.drawText(seat.trim(), TEXT_VALUE_X, SEAT_LABEL_Y, paint);

        paint.setFakeBoldText(false);
    }

    private void showSuccessToast(Context context, String folder) {
        String message = "Билет сохранён в папке " + folder + " вашей галереи";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}