package com.example.ula_tickets_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "app_ULA.db";
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
        db.execSQL("CREATE TABLE if not exists tickets ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_ROW + " TEXT, "
                + COLUMN_PLACE +" TEXT, "
                + COLUMN_HALL + " TEXT, "
                + COLUMN_COST + " INTEGER);");

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS "+TABLE);
        onCreate(db);
    }

    public boolean addTicket(String name, String date, String row, String place, String hall, int cost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_ROW, row);
        cv.put(COLUMN_PLACE, place);
        cv.put(COLUMN_HALL, hall);
        cv.put(COLUMN_COST, cost);
        long result = db.insert(TABLE, null, cv);
        return result != -1;
    }

    public Cursor getAllTickets() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE, null);
    }

    public void clearTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }
}
