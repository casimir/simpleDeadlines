package com.casimirlab.simpleDeadlines.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.casimirlab.simpleDeadlines.R;

import static com.casimirlab.simpleDeadlines.provider.DeadlinesContract.Deadlines;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "deadlines.db";
    private static final int DB_VERSION = 2;
    private static final String TAG = DBHelper.class.getSimpleName();

    private final Context _context;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        _context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Deadlines.TABLE_NAME + "("
                + Deadlines.ID + " INTEGER PRIMARY KEY, "
                + Deadlines.LABEL + " TEXT, "
                + Deadlines.GROUP + " TEXT, "
                + Deadlines.DUE_DATE + " INTEGER, "
                + Deadlines.DONE + " INTEGER"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database: " + oldVersion + " -> " + newVersion);
        switch (oldVersion) {
            case 1:
                v2(db);
        }
    }

    private void v2(SQLiteDatabase db) {
        String sqlSelect = "SELECT " + Deadlines.ID + " "
                + "FROM " + Deadlines.TABLE_NAME + " "
                + "WHERE " + Deadlines.GROUP + " = '';";
        String sqlUpdate = "UPDATE " + Deadlines.TABLE_NAME + " "
                + "SET " + Deadlines.GROUP + " = '" + _context.getString(R.string.default_group) + "' "
                + "WHERE " + Deadlines.ID + " = ?;";
        Cursor c = db.rawQuery(sqlSelect, null);

        while (c.moveToNext()) {
            int id = c.getInt(0);
            db.execSQL(sqlUpdate, new Object[]{id});
        }
    }
}