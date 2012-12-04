package com.casimirlab.simpleDeadlines;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataHelper extends SQLiteOpenHelper
{
  private static final String DB_NAME = "deadlines.db";
  private static final int DB_VERSION = 1;
  public static final String TABLE_NAME = "deadlines";
  public static final String KEY_ID = "_id";
  public static final String KEY_LABEL = "label";
  public static final String KEY_GROUP = "groupname";
  public static final String KEY_DUE_DATE = "due_date";
  public static final String KEY_DONE = "done";

  public DataHelper(Context context)
  {
    super(context, DB_NAME, null, DB_VERSION);
    getWritableDatabase();
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

  public void create(DeadlineModel model)
  {
    getWritableDatabase().insert(TABLE_NAME, null, DeadlineModel.toValues(model));
  }

  public DeadlineModel read(int id)
  {
    String where = KEY_ID + " = ?";
    String[] whereArgs = new String[]
    {
      String.valueOf(id)
    };

    Cursor c = getReadableDatabase().query(TABLE_NAME, null, where, whereArgs, null, null, null);
    DeadlineModel model = DeadlineModel.fromCursor(c);

    return model;
  }

  public void update(DeadlineModel model)
  {
    String where = KEY_ID + " = ?";
    String[] whereArgs = new String[]
    {
      String.valueOf(model.Id())
    };
    getWritableDatabase().update(TABLE_NAME, DeadlineModel.toValues(model), where, whereArgs);
  }

  public void delete(int id)
  {
    String where = KEY_ID + " = ?";
    String[] whereArgs = new String[]
    {
      String.valueOf(id)
    };
    getWritableDatabase().delete(TABLE_NAME, where, whereArgs);
  }

  public List<DeadlineModel> readAll()
  {
    Cursor c = getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, KEY_DUE_DATE);
    return DeadlineModel.fromMultipleCursor(c);
  }

  public int count()
  {
    return (int)DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_NAME);
  }

  public Map<Integer, Integer> counts()
  {
    Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
    String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + KEY_DUE_DATE + " <= ?";
    SQLiteStatement req = getReadableDatabase().compileStatement(sql);
    String[] param = new String[1];
    int[] levels = new int[]
    {
      DeadlineModel.LVL_TODAY, DeadlineModel.LVL_URGENT, DeadlineModel.LVL_WORRYING, DeadlineModel.LVL_NICE
    };
    Date today = new Date();
    today.setHours(0);

    int count = 0;
    for (int lvl : levels)
    {
      long diff = today.getTime() + lvl * DeadlineModel.DAY_LEN;
      param[0] = String.valueOf(diff);
      int value = (int)DatabaseUtils.longForQuery(req, param) - count;
      ret.put(lvl, value);
      count += value;
    }

    return ret;
  }
}
