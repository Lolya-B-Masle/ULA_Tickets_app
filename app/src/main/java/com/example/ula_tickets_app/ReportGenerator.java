package com.example.ula_tickets_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.text.SimpleDateFormat;
import java.util.Random;

public class ReportGenerator {
    static DatabaseHelper databaseHelper;
    private static final  Date now = new Date();
    private static final SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    public static File generateReportFile(Context context, String date, String time, Bitmap BG) {
        // 1. Создаем Bitmap
        Bitmap bitmap = createTicketBitmap(BG, context);

        // 2. Сохраняем в зависимости от версии Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveForAndroid10Plus(context, bitmap, date, time);
        } else {
            return saveLegacy(context, bitmap, date, time);
        }
    }
    private static Bitmap createTicketBitmap(Bitmap BG, Context context) {

        databaseHelper = new DatabaseHelper(context);
        Cursor cursor = databaseHelper.getMoviesWithTicketCounts();
        Cursor count_cursor = databaseHelper.getTicketCount();
        Cursor date_cursor = databaseHelper.getMinMaxDate();

        List<String> soldList = new ArrayList<>();
        List<String> soldCountList = new ArrayList<>();
        List<String> soldSumList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            soldList.add("Записей нет");
        } else {
            while (cursor.moveToNext()) {
                String soldLine = cursor.getString(0);
                String soldCount = cursor.getString(1);
                String soldSum = cursor.getString(2);
                soldList.add(soldLine);
                soldCountList.add(soldCount);
                soldSumList.add(soldSum);
            }
        }

        int width = 1080;
        int height = 1920;
        Bitmap bitmap = Bitmap.createScaledBitmap(BG, width, height, false);
        Canvas canvas = new Canvas(bitmap);

        // Рисуем билет
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, paint);


        paint.setColor(Color.BLACK);
        paint.setTextSize(70);

        paint.setFakeBoldText(true);
        canvas.drawText("Отчет: 'проданные купоны'", 100, 120, paint);
        paint.setFakeBoldText(false);

        while(date_cursor.moveToNext()){
            String date_MIN = date_cursor.getString(0);
            String date_MAX = date_cursor.getString(1);
            paint.setTextSize(45);
            canvas.drawText("За период: " + date_MIN + " - " + date_MAX, 100, 175, paint);
        }

        paint.setColor(Color.BLACK);
        paint.setTextSize(40);

        int y = 200;
        int bias = 90;
        int i = 0;
        int x_count = 680;
        int x_sum = 810;


        for (String el : soldList) {
            String[] movie_name = MainActivity.splitStringByLastSpace(el, 25);
            if (movie_name.length == 2) {
                canvas.drawText(movie_name[0], 50, y+=bias, paint);
                bias = 40;
                canvas.drawText(movie_name[1], 50, y+=bias, paint);
                canvas.drawText(soldCountList.get(i) + " шт.", x_count, y, paint);
                canvas.drawText(soldSumList.get(i) + " руб.", x_sum, y, paint);
                bias = 90;
            }
            else{
                canvas.drawText(el, 50, y+=bias, paint);
                canvas.drawText(soldCountList.get(i) + " шт.", x_count, y, paint);
                canvas.drawText(soldSumList.get(i) + " руб.", x_sum, y, paint);
            }
            i++;
        }

        paint.setFakeBoldText(true);
        paint.setTextSize(50);
        while (count_cursor.moveToNext()) {
            String countSum = count_cursor.getString(0);
            String costSum = count_cursor.getString(1);
            canvas.drawText("Итого ", 50, 1850, paint);
            canvas.drawText( countSum + " шт. ", 220, 1850, paint);
            canvas.drawText( "на сумму "+ costSum + " руб.", 450, 1850, paint);
        }

        return bitmap;
    }

    private static File saveLegacy(Context context, Bitmap bitmap, String date, String time) {
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

    private static File saveForAndroid10Plus(Context context, Bitmap bitmap, String date, String time) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Отчет_" + date + "_" + time + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ОТЧЕТЫ_ПО_ПРОДАЖАМ");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return null;

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return new File(uri.getPath()); // Возвращаем файловый объект для совместимости
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void scanMediaFile(Context context, File file) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{file.getAbsolutePath()},
                new String[]{"image/jpeg"},
                (path, uri) -> Toast.makeText(context, "Отчет сохранен: " + path, Toast.LENGTH_LONG).show()
        );
    }
}
