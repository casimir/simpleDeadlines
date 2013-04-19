package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper
{
  public static final String ACTION_UPDATE = "DataHelper.ACTION_UPDATE";
  private static final String DB_NAME = "deadlines.db";
  private static final int DB_VERSION = 1;
  private static final String KEY_ID = DeadlinesContract.DeadlinesColumns.ID;
  private static final String KEY_LABEL = DeadlinesContract.DeadlinesColumns.LABEL;
  private static final String KEY_GROUP = DeadlinesContract.DeadlinesColumns.GROUP;
  private static final String KEY_DUE_DATE = DeadlinesContract.DeadlinesColumns.DUE_DATE;
  private static final String KEY_DONE = DeadlinesContract.DeadlinesColumns.DONE;
  private static final String TABLE_NAME = DeadlinesContract.DEADLINES_PATH;

  public DBHelper(Context context)
  {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    String sql = "";
    sql += "CREATE TABLE " + TABLE_NAME + "(";
    sql += KEY_ID + " INTEGER PRIMARY KEY, ";
    sql += KEY_LABEL + " TEXT, ";
    sql += KEY_GROUP + " TEXT, ";
    sql += KEY_DUE_DATE + " INTEGER, ";
    sql += KEY_DONE + " INTEGER";
    sql += ")";
    db.execSQL(sql);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
  }

  public Map<Integer, Integer> counts()
  {
    Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
    String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + KEY_DUE_DATE
		 + " <= ? AND " + KEY_DONE + " = 0;";
    SQLiteStatement req = getReadableDatabase().compileStatement(sql);
    String[] param = new String[1];
    int[] levels = new int[]
    {
      DeadlineModel.LVL_TODAY, DeadlineModel.LVL_URGENT, DeadlineModel.LVL_WORRYING, DeadlineModel.LVL_NICE
    };
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);

    int count = 0;
    for (int lvl : levels)
    {
      long diff = today.getTimeInMillis() + lvl * DeadlineModel.DAY_LEN;
      param[0] = String.valueOf(diff);
      int value = (int)DatabaseUtils.longForQuery(req, param) - count;
      ret.put(lvl, value);
      count += value;
    }
    return ret;
  }
}