package com.example.ula_tickets_app;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Date;
import java.util.Locale;

import java.text.SimpleDateFormat;

public class PDFCreator {
    final PdfDocument document = new PdfDocument();
    private final PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, 1).create();
    final PdfDocument.Page page = document.startPage(pageInfo);
    private final Canvas canvas = page.getCanvas();
    private final File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private final String fileName = "TU.pdf";
    private final Date now = new Date();
    private final SimpleDateFormat ticket_date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private final SimpleDateFormat movie_year = new SimpleDateFormat(".yyyy", Locale.getDefault());
    private final Paint paint = new Paint();

    protected void setBG(Bitmap BG) {
        Bitmap BG_scaled = Bitmap.createScaledBitmap(BG, 1080, 1920, false);
        canvas.drawBitmap(BG_scaled, 0, 0, paint);
    }

    protected void setCinemaLogo(Bitmap logo) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 520, 520, false);
        canvas.drawBitmap(logo_scaled, 0, -34, paint);
    }

    protected void setCompanyLogo(Bitmap logo) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 448, 345, false);
        canvas.drawBitmap(logo_scaled, 585, 10, paint);
    }

    protected void setDivider(Bitmap logo) {
        Bitmap logo_scaled = Bitmap.createScaledBitmap(logo, 920, 150, false);
        canvas.drawBitmap(logo_scaled, 80, 370, paint);
    }

    protected void setBitmap(Bitmap bmp, int width, int height, int x, int y) {
        Bitmap bmp_scaled = Bitmap.createScaledBitmap(bmp, width, height, false);
        canvas.drawBitmap(bmp_scaled, x, y, paint);
    }

    protected void setMovieName(String[] movie_name) {
        paint.setColor(Color.BLACK);

        paint.setTextSize(35);
        canvas.drawText("НАЗВАНИЕ ФИЛЬМА", 60, 560, paint);

        paint.setTextSize(80);
        paint.setFakeBoldText(true);
        canvas.drawText(movie_name[0], 60, 660, paint);

        if (movie_name.length == 2)
            canvas.drawText(movie_name[1], 60, 760, paint);

        paint.setFakeBoldText(false);
    }

    protected void setTicket_date() {
        paint.setColor(Color.LTGRAY);
        paint.setTextSize(35);

        canvas.drawText("Билет от:  " + ticket_date.format(now), 12, 35, paint);
    }

    protected void setMovieDateTime(String date_value, String time_value) {
        paint.setColor(Color.BLACK);

        paint.setTextSize(35);
        canvas.drawText("ДАТА:", 60, 920, paint);
        canvas.drawText("ВРЕМЯ:", 60, 1040, paint);

        paint.setTextSize(75);
        canvas.drawText(date_value + movie_year.format(now), 210, 920, paint);
        canvas.drawText(time_value, 210, 1040, paint);
    }

    protected void setMoviePlace(String hall_value, String row_value, String place_value) {
        paint.setColor(Color.BLACK);

        paint.setTextSize(35);
        canvas.drawText("ЗАЛ:", 60, 1240, paint);
        canvas.drawText("РЯД:", 60, 1360, paint);
        canvas.drawText("МЕСТО:", 60, 1480, paint);

        paint.setTextSize(75);
        canvas.drawText(hall_value, 220, 1240, paint);
        canvas.drawText(row_value, 220, 1360, paint);
        canvas.drawText(place_value, 220, 1480, paint);
    }

    public void createPDF(Context context) {

        document.finishPage(page);

        File file = new File(downloadDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            Toast.makeText(context, "Создание документа завершено!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
