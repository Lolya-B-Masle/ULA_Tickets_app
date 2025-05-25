package com.example.ula_tickets_app;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
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

    public File generateTicketImage(Context context, Bitmap BG, Bitmap R_logo, Bitmap U_logo, Bitmap divider, Bitmap dec_text,
                                    String[] movieTitle, String date, String time,
                                    String row, String seat, String hall) {
        Bitmap bitmap = createTicketBitmap(BG, R_logo, U_logo, divider, dec_text, movieTitle, date, time, row, seat, hall);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            return saveForAndroid10Plus(context, bitmap, date, time);
        else
            return saveLegacy(context, bitmap, date, time);
    }

    private Bitmap createTicketBitmap(Bitmap BG, Bitmap R_logo, Bitmap U_logo, Bitmap divider, Bitmap dec_text,
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
        setMovieDateTime(date, time, canvas);
        setMoviePlace(hall, row, seat, canvas);

        setTicket_date(canvas);

        return bitmap;
    }

    protected void setCinemaLogo(Bitmap logo, Canvas canvas) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 520, 520, false);
        canvas.drawBitmap(logo_scaled, 0, -34, paint);
    }

    protected void setCompanyLogo(Bitmap logo, Canvas canvas) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 448, 345, false);
        canvas.drawBitmap(logo_scaled, 585, 10, paint);
    }

    protected void setDivider(Bitmap logo, Canvas canvas) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 920, 150, false);
        canvas.drawBitmap(logo_scaled, 80, 370, paint);
    }

    protected void setBitmap(Bitmap bmp, int width, int height, int x, int y, Canvas canvas) {
        Bitmap bmp_scaled = Bitmap.createScaledBitmap(bmp, width, height, false);
        canvas.drawBitmap(bmp_scaled, x, y, paint);
    }

    protected void setMovieName(String[] movie_name, Canvas canvas) {
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

    protected void setTicket_date(Canvas canvas) {
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(35);

        canvas.drawText(ticket_date.format(now), 12, 35, paint);
    }

    protected void setMovieDateTime(String date_value, String time_value, Canvas canvas) {
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

    protected void setMoviePlace(String hall_value, String row_value, String place_value, Canvas canvas) {
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

    private File saveLegacy(Context context, Bitmap bitmap, String date, String time) {
        File publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File ticketsDir = new File(publicDir, "Cinema Tickets");
        if (!ticketsDir.exists() && !ticketsDir.mkdirs()) {
            Toast.makeText(context, "Ошибка создания папки", Toast.LENGTH_SHORT).show();
            return null;
        }

        File ticketFile = new File(ticketsDir, "Билет_" + date + "_" + time + ".jpg");

        try (FileOutputStream out = new FileOutputStream(ticketFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            scanMediaFile(context, ticketFile);
            return ticketFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File saveForAndroid10Plus(Context context, Bitmap bitmap, String date, String time) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Билет_" + date + "_" + time + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/БИЛЕТЫ_В_КИНО");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return null;

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return new File(uri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void scanMediaFile(Context context, File file) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{file.getAbsolutePath()},
                new String[]{"image/jpeg"},
                (path, uri) -> Toast.makeText(context, "Билет сохранён: " + path, Toast.LENGTH_LONG).show()
        );
    }
}
