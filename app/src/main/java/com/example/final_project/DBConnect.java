package com.example.final_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBConnect extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mynasa.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MYNASA = "mynasa";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMAGE_DATE = "ImageDate";
    public static final String COLUMN_NASA_URL = "NasaURL";
    public static final String COLUMN_HD_URL = "HDUrl"; // New column for HD URL

    // Constructor
    public DBConnect(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_MYNASA + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_IMAGE_DATE + " TEXT, "
                + COLUMN_NASA_URL + " TEXT, "
                + COLUMN_HD_URL + " TEXT)"; // Added HD URL column
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYNASA);
        onCreate(db);
    }

    // Method to insert data into database
    public long insertData(String imageDate, String nasaUrl, String hdUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_IMAGE_DATE, imageDate);
        contentValues.put(COLUMN_NASA_URL, nasaUrl);
        contentValues.put(COLUMN_HD_URL, hdUrl); // Insert HD URL into database
        long result = db.insert(TABLE_MYNASA, null, contentValues);
        db.close();
        return result;
    }
    public Cursor getAllSavedImages() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_IMAGE_DATE, COLUMN_NASA_URL, COLUMN_HD_URL}; // Columns to retrieve
        return db.query(TABLE_MYNASA, projection, null, null, null, null, null);
    }
    public void deleteSavedImage(String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MYNASA, COLUMN_NASA_URL + " = ?", new String[]{imageUrl});
        db.close();
    }
    public void undoDeleteImage(String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Insert the image details back into the database
        values.put(COLUMN_NASA_URL, imageUrl);
        db.insert(TABLE_MYNASA, null, values);
        db.close();
    }
}
