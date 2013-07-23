package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
  private static final String DB_NAME = "deadlines.db";
  private static final int DB_VERSION = 1;

  public DBHelper(Context context)
  {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    String sql = "";
    sql += "CREATE TABLE " + DeadlinesContract.DEADLINES_PATH + "(";
    sql += DeadlinesContract.DeadlinesColumns.ID + " INTEGER PRIMARY KEY, ";
    sql += DeadlinesContract.DeadlinesColumns.LABEL + " TEXT, ";
    sql += DeadlinesContract.DeadlinesColumns.GROUP + " TEXT, ";
    sql += DeadlinesContract.DeadlinesColumns.DUE_DATE + " INTEGER, ";
    sql += DeadlinesContract.DeadlinesColumns.DONE + " INTEGER";
    sql += ")";
    db.execSQL(sql);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS " + DeadlinesContract.DEADLINES_PATH);
    onCreate(db);
  }
}