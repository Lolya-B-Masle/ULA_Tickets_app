package com.example.ula_tickets_app;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ULA.db";
    private static final int SCHEMA = 1;
    static final String TABLE = "tickets";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "movie_name";
    public static final String COLUMN_DATE = "ticket_date";
    public static final String COLUMN_ROW = "hall_row";
    public static final String COLUMN_PLACE = "hall_place";
    public static final String COLUMN_HALL = "hall_number";
    public static final String COLUMN_COST = "ticket_cost";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS tickets ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DATE + " TEXT"
                + COLUMN_ROW + "TEXT"
                + COLUMN_PLACE +"TEXT"
                + COLUMN_HALL + "TEXT"
                + COLUMN_COST + "INTEGER);");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
    }
}
