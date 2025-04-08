package com.example.ula_tickets_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;
import android.widget.Toast;
import java.io.File;

public class PDFOpener {
    private static final String PREFS_NAME = "PdfOpenerPrefs";
    private static final String KEY_SELECTED_APP = "selected_app";

    public static void openPdf(final Context context, String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            Toast.makeText(context, "Файл не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = getFileUri(context, file);
        Intent intent = createPdfIntent(uri);

        String selectedAppPackage = getSavedAppChoice(context);
        if (selectedAppPackage != null) {
            try {
                intent.setPackage(selectedAppPackage);
                context.startActivity(intent);
            } catch (Exception e) {
                showAppChooser(context, intent);
            }
        } else {
            showAppChooser(context, intent);
        }
    }

    private static Uri getFileUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    private static Intent createPdfIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        return intent;
    }

    private static void showAppChooser(Context context, Intent intent) {
        Intent chooser = Intent.createChooser(intent, "Выберите приложение для открытия PDF");
        context.startActivity(chooser);
    }

    private static String getSavedAppChoice(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SELECTED_APP, null);
    }

    public static void saveAppChoice(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED_APP, packageName).apply();
    }
}


