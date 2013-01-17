package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataHelper extends SQLiteOpenHelper
{
  public static final int TYPE_PENDING = 0;
  public static final int TYPE_ARCHIVED = 1;
  public static final int TYPE_ALL = 2;
  public static final String TABLE_NAME = "deadlines";
  public static final String KEY_ID = "_id";
  public static final String KEY_LABEL = "label";
  public static final String KEY_GROUP = "groupname";
  public static final String KEY_DUE_DATE = "due_date";
  public static final String KEY_DONE = "done";
  private static final String DB_NAME = "deadlines.db";
  private static final int DB_VERSION = 1;

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
    c.moveToFirst();
    return DeadlineModel.fromCursor(c);
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

  public int count()
  {
    return (int)DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_NAME);
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

  public Cursor deadlines(int type, String group)
  {
    // TODO LAME!
    String selection = KEY_DUE_DATE + " < ? AND " + KEY_DONE + " = ?";
    String[] selectionArgs = new String[]
    {
      String.valueOf(new Date().getTime()), "1"
    };

    String groupSelection = "";
    if (group != null && !group.isEmpty())
      groupSelection = " AND " + KEY_GROUP + " = " + DatabaseUtils.sqlEscapeString(group);

    switch (type)
    {
      case TYPE_PENDING:
	return getReadableDatabase().query(TABLE_NAME, null,
					   "NOT(" + selection + ")" + groupSelection, selectionArgs,
					   null, null, KEY_DUE_DATE);
      case TYPE_ARCHIVED:
	return getReadableDatabase().query(TABLE_NAME, null,
					   selection + groupSelection, selectionArgs,
					   null, null, KEY_DUE_DATE);
      case TYPE_ALL:
      default:
	return getReadableDatabase().query(TABLE_NAME, null,
					   null, null,
					   null, null, KEY_DUE_DATE);
    }
  }

  public Cursor groups(int type)
  {
    Cursor c;
    String[] cols = new String[]
    {
      KEY_ID, KEY_GROUP
    };
    String selection = KEY_DUE_DATE + " < ? AND " + KEY_DONE + " = ?";
    String[] selectionArgs = new String[]
    {
      String.valueOf(new Date().getTime()), "1"
    };
    String having = KEY_GROUP + " != ''";
    switch (type)
    {
      case TYPE_PENDING:
	c = getReadableDatabase().query(TABLE_NAME, cols,
					"NOT(" + selection + ")", selectionArgs,
					KEY_GROUP, having, KEY_GROUP);
	break;
      case TYPE_ARCHIVED:
	c = getReadableDatabase().query(TABLE_NAME, cols,
					selection, selectionArgs,
					KEY_GROUP, having, KEY_GROUP);
	break;
      case TYPE_ALL:
      default:
	c = getReadableDatabase().query(TABLE_NAME, cols,
					null, null,
					KEY_GROUP, having, KEY_GROUP);
    }
    return c;
  }

  public Cursor filteredGroups(CharSequence constraint)
  {
    String[] cols = new String[]
    {
      KEY_ID, KEY_GROUP
    };
    String filter = DatabaseUtils.sqlEscapeString(constraint + "%");
    String having = KEY_GROUP + " != '' AND " + KEY_GROUP + " LIKE " + filter;
    return getReadableDatabase().query(TABLE_NAME, cols,
				       null, null,
				       KEY_GROUP, having, KEY_GROUP);
  }
}
