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
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TicketCreator {
    private final Date now = new Date();
    private final SimpleDateFormat ticket_date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat movie_year = new SimpleDateFormat(".yyyy", Locale.getDefault());
    private final Paint paint = new Paint();

    // Generate movie ticket
    public Bitmap createTicket_Movie(Bitmap BG, Bitmap R_logo, Bitmap U_logo, Bitmap divider, Bitmap dec_text,
                                             String[] movieTitle, String date, String time,
                                             String row, String seat, String hall) {
        int width = 1080;
        int height = 1920;
        Bitmap bitmap = Bitmap.createScaledBitmap(BG, width, height, false);
        Canvas canvas = new Canvas(bitmap);

        setCinemaLogo(R_logo, canvas);
        setCompanyLogo(U_logo, canvas);
        setDivider(divider, canvas);

        setBitmap(dec_text, 704, 445, 60, 1470, canvas);

        setMovieName(movieTitle, canvas);
        setDateTime(date, time, canvas);
        setMoviePlace(hall, row, seat, canvas);

        setTicket_date(canvas);

        return bitmap;
    }

    // Generate concert ticket
    public Bitmap createTicket_Concert(Bitmap BG, Bitmap logo, Bitmap divider, Bitmap dec_text, String date, String time,
                                      String row, String seat, String hall) {
        int width = 1080;
        int height = 1920;
        Bitmap bitmap = Bitmap.createScaledBitmap(BG, width, height, false);
        Canvas canvas = new Canvas(bitmap);

        setCompanyLogo(logo, canvas);
        setDivider(divider, canvas);

        setBitmap(dec_text, 704, 445, 60, 1470, canvas);

        setDateTime(date, time, canvas);
        setMoviePlace(hall, row, seat, canvas);

        setTicket_date(canvas);

        return bitmap;
    }

    // Save ticket in gallery
    public void saveTicket(Context context, Bitmap bitmap, String date, String time, String folder) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Билет_" + date + "_" + time + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + folder);

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return;

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            new File(uri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = "Билет сохранён в папке "+ folder +" вашей галереи";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Set logos on ticket
    private void setCinemaLogo(Bitmap logo, Canvas canvas) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 520, 520, false);
        canvas.drawBitmap(logo_scaled, 0, -34, paint);
    }
    private void setCompanyLogo(Bitmap logo, Canvas canvas) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 448, 345, false);
        canvas.drawBitmap(logo_scaled, 585, 10, paint);
    }
    private void setDivider(Bitmap logo, Canvas canvas) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 920, 150, false);
        canvas.drawBitmap(logo_scaled, 80, 370, paint);
    }
    private void setBitmap(Bitmap bmp, int width, int height, int x, int y, Canvas canvas) {
        Bitmap bmp_scaled = Bitmap.createScaledBitmap(bmp, width, height, false);
        canvas.drawBitmap(bmp_scaled, x, y, paint);
    }

    // Set text on ticket
    private void setMovieName(String[] movie_name, Canvas canvas) {
        paint.setColor(Color.BLACK);

        paint.setTextSize(35);
        canvas.drawText("НАЗВАНИЕ ФИЛЬМА", 60, 560, paint);

        paint.setTextSize(80);
        paint.setFakeBoldText(true);

        if (movie_name.length == 2) {
            if (movie_name[1].length() > 18)
                paint.setTextSize(65);
            canvas.drawText(movie_name[1].toUpperCase().trim(), 60, 760, paint);
        }

        canvas.drawText(movie_name[0].toUpperCase().trim(), 60, 660, paint);

        paint.setFakeBoldText(false);
    }
    private void setTicket_date(Canvas canvas) {
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(35);

        canvas.drawText(ticket_date.format(now), 12, 35, paint);
    }
    private void setDateTime(String date_value, String time_value, Canvas canvas) {
        paint.setColor(Color.BLACK);

        paint.setTextSize(45);
        canvas.drawText("ДАТА:", 60, 920, paint);
        canvas.drawText("ВРЕМЯ:", 60, 1040, paint);

        paint.setTextSize(85);
        paint.setFakeBoldText(true);
        canvas.drawText(date_value + movie_year.format(now), 260, 920, paint);
        canvas.drawText(time_value, 260, 1040, paint);
        paint.setFakeBoldText(false);
    }
    private void setMoviePlace(String hall_value, String row_value, String place_value, Canvas canvas) {
        paint.setColor(Color.BLACK);

        paint.setTextSize(45);
        canvas.drawText("ЗАЛ:", 60, 1230, paint);
        canvas.drawText("РЯД:", 60, 1350, paint);
        canvas.drawText("МЕСТО:", 60, 1470, paint);

        paint.setTextSize(85);
        paint.setFakeBoldText(true);

        if (hall_value.equals("уточнять у кассира"))
            paint.setTextSize(65);
        canvas.drawText(hall_value.trim(), 260, 1230, paint);

        paint.setTextSize(85);
        canvas.drawText(row_value.trim(), 260, 1350, paint);
        canvas.drawText(place_value.trim(), 260, 1470, paint);
        paint.setFakeBoldText(false);
    }

}
