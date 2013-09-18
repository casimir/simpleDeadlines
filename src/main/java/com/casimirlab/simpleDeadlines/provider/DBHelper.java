package com.casimirlab.simpleDeadlines.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "deadlines.db";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DeadlinesContract.Deadlines.TABLE_NAME + "("
                + DeadlinesContract.Deadlines.ID + " INTEGER PRIMARY KEY, "
                + DeadlinesContract.Deadlines.LABEL + " TEXT, "
                + DeadlinesContract.Deadlines.GROUP + " TEXT, "
                + DeadlinesContract.Deadlines.DUE_DATE + " INTEGER, "
                + DeadlinesContract.Deadlines.DONE + " INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DeadlinesContract.Deadlines.TABLE_NAME);
        onCreate(db);
    }
}