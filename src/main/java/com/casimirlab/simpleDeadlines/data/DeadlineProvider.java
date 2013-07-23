package com.casimirlab.simpleDeadlines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DeadlineProvider extends ContentProvider
{
  private static final int COUNT = 1;
  private static final int DEADLINES = 0;
  private static final int DEADLINES_ARCHIVED = 11;
  private static final int DEADLINES_GROUPS = 2;
  private static final int DEADLINES_ARCHIVED_GROUPS = 12;
  private static final int DEADLINES_GROUP_LABEL = 3;
  private static final int DEADLINES_ARCHIVED_GROUP_LABEL = 13;
  private static final int DEADLINE_ID = 4;
  private static final int GROUPS = 5;
  private static final String TAG = "DeadlineProvider";
  private static final UriMatcher Matcher = new UriMatcher(UriMatcher.NO_MATCH);
  private DBHelper _dbHelper;

  static
  {
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.COUNT_PATH,
		   COUNT);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH,
		   DEADLINES);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH + "/archived",
		   DEADLINES_ARCHIVED);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH + "/groups",
		   DEADLINES_GROUPS);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH + "/archived/groups",
		   DEADLINES_ARCHIVED_GROUPS);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH + "/group/*",
		   DEADLINES_GROUP_LABEL);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH + "/archived/group/*",
		   DEADLINES_ARCHIVED_GROUP_LABEL);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.DEADLINES_PATH + "/#",
		   DEADLINE_ID);
    Matcher.addURI(DeadlinesContract.AUTHORITY,
		   DeadlinesContract.GROUPS_PATH,
		   GROUPS);
  }

  @Override
  public boolean onCreate()
  {
    _dbHelper = new DBHelper(getContext());

    return true;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs)
  {
    SQLiteDatabase db = _dbHelper.getWritableDatabase();
    int ret = 0;

    switch (Matcher.match(uri))
    {
      case DEADLINES:
	// TODO placeholder
	break;
      case DEADLINE_ID:
	String whereClause = DeadlinesContract.DeadlinesColumns.ID + " = ?";
	String[] whereArgs =
	{
	  uri.getLastPathSegment()
	};
	ret = db.delete(DeadlinesContract.DEADLINES_PATH, whereClause, whereArgs);
	break;
      default:
	Log.e(TAG, "DELETE - Unsupported path:" + uri.getPath());
	throw new IllegalArgumentException("DELETE - Unsupported path: " + uri.getPath());
    }

    getContext().getContentResolver().notifyChange(DeadlinesContract.BASE_URI, null);
    return ret;
  }

  @Override
  public String getType(Uri uri)
  {
    switch (Matcher.match(uri))
    {
      case COUNT:
	return DeadlinesContract.Count.CONTENT_TYPE;
      case DEADLINES:
      case DEADLINES_ARCHIVED:
	return DeadlinesContract.Deadlines.CONTENT_TYPE;
      case DEADLINES_GROUPS:
      case DEADLINES_ARCHIVED_GROUPS:
	return DeadlinesContract.Groups.CONTENT_TYPE;
      case DEADLINES_GROUP_LABEL:
      case DEADLINES_ARCHIVED_GROUP_LABEL:
	return DeadlinesContract.Groups.CONTENT_ITEM_TYPE;
      case DEADLINE_ID:
	return DeadlinesContract.Deadlines.CONTENT_ITEM_TYPE;
      case GROUPS:
	return DeadlinesContract.Groups.CONTENT_TYPE;
      default:
	return null;
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values)
  {
    if (Matcher.match(uri) != DEADLINES)
    {
      Log.e(TAG, "INSERT - Unsupported path:" + uri.getPath());
      throw new IllegalArgumentException("INSERT - Unsupported path: " + uri.getPath());
    }

    SQLiteDatabase db = _dbHelper.getWritableDatabase();
    long id = db.insert(DeadlinesContract.DEADLINES_PATH, null, values);

    getContext().getContentResolver().notifyChange(DeadlinesContract.BASE_URI, null);
    return ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
  {
    SQLiteDatabase db = _dbHelper.getReadableDatabase();
    Cursor ret = null;

    switch (Matcher.match(uri))
    {
      case COUNT:
	ret = queryCount();
	break;
      case DEADLINES:
	ret = queryDeadlines(false);
	break;
      case DEADLINES_ARCHIVED:
	ret = queryDeadlines(true);
	break;
      case DEADLINES_GROUPS:
	ret = queryGroups(false);
	break;
      case DEADLINES_ARCHIVED_GROUPS:
	ret = queryGroups(true);
	break;
      case DEADLINES_GROUP_LABEL:
	ret = queryDeadlines(false, uri.getLastPathSegment());
	break;
      case DEADLINES_ARCHIVED_GROUP_LABEL:
	ret = queryDeadlines(true, uri.getLastPathSegment());
	break;
      case DEADLINE_ID:
	String where = DeadlinesContract.DeadlinesColumns.ID + " = ?";
	String[] whereArgs = new String[]
	{
	  uri.getLastPathSegment()
	};
	ret = db.query(DeadlinesContract.DEADLINES_PATH, null,
		       where, whereArgs,
		       null, null, null);
	break;
      case GROUPS:
	String groupCol = DeadlinesContract.DeadlinesColumns.GROUP;
	String having = groupCol + " != ''";
	String[] cols =
	{
	  DeadlinesContract.DeadlinesColumns.ID,
	  groupCol
	};
	ret = db.query(DeadlinesContract.DEADLINES_PATH, cols,
		       selection, selectionArgs,
		       groupCol, having, groupCol);
	break;
      default:
	Log.e(TAG, "QUERY - Unsupported path:" + uri.getPath());
	throw new IllegalArgumentException("QUERY - Unsupported path: " + uri.getPath());
    }

    if (ret != null)
      ret.setNotificationUri(getContext().getContentResolver(), uri);
    return ret;
  }

  public Cursor queryCount()
  {
    MatrixCursor c = new MatrixCursor(DeadlinesContract.CountColumns.ALL, 1);
    // FIXME avoidable SQL ?
    String sql = "SELECT COUNT(*) FROM " + DeadlinesContract.DEADLINES_PATH
		 + " WHERE " + DeadlinesContract.DeadlinesColumns.DUE_DATE
		 + " <= ? AND " + DeadlinesContract.DeadlinesColumns.DONE + " = 0;";
    SQLiteStatement req = _dbHelper.getReadableDatabase().compileStatement(sql);
    String[] param = new String[1];
    int[] levels = new int[]
    {
      DeadlineUtils.LVL_TODAY,
      DeadlineUtils.LVL_URGENT,
      DeadlineUtils.LVL_WORRYING,
      DeadlineUtils.LVL_NICE
    };
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);

    int count = 0;
    MatrixCursor.RowBuilder builder = c.newRow();
    for (int lvl : levels)
    {
      long diff = today.getTimeInMillis() + lvl * DateUtils.DAY_IN_MILLIS;
      param[0] = String.valueOf(diff);
      int value = (int)DatabaseUtils.longForQuery(req, param) - count;
      builder.add(value);
      count += value;
    }

    return c;
  }

  private Cursor queryDeadlines(boolean archived)
  {
    return queryDeadlines(archived, null);
  }

  private Cursor queryDeadlines(boolean archived, String group)
  {
    String selection = DeadlinesContract.DeadlinesColumns.DUE_DATE + " < ? AND "
		       + DeadlinesContract.DeadlinesColumns.DONE + " = ?";
    if (!archived)
      selection = "NOT(" + selection + ")";
    String groupSelection = "";
    List<String> selectionArgs = new LinkedList<String>();
    selectionArgs.add(String.valueOf(new Date().getTime()));
    selectionArgs.add("1");

    if (!TextUtils.isEmpty(group))
    {
      groupSelection = " AND " + DeadlinesContract.DeadlinesColumns.GROUP + " = ?";
      selectionArgs.add(group);
    }

    SQLiteDatabase db = _dbHelper.getReadableDatabase();
    return db.query(DeadlinesContract.DEADLINES_PATH, null,
		    selection + groupSelection, selectionArgs.toArray(new String[selectionArgs.size()]),
		    null, null, DeadlinesContract.DeadlinesColumns.DUE_DATE);
  }

  private Cursor queryGroups(boolean archived)
  {
    String[] cols = new String[]
    {
      DeadlinesContract.DeadlinesColumns.ID,
      DeadlinesContract.DeadlinesColumns.GROUP
    };
    String selection = DeadlinesContract.DeadlinesColumns.DUE_DATE + " < ? AND "
		       + DeadlinesContract.DeadlinesColumns.DONE + " = ?";
    if (!archived)
      selection = "NOT(" + selection + ")";
    String[] selectionArgs = new String[]
    {
      String.valueOf(new Date().getTime()),
      "1"
    };
    String having = DeadlinesContract.DeadlinesColumns.GROUP + " != ''";

    SQLiteDatabase db = _dbHelper.getReadableDatabase();
    return db.query(DeadlinesContract.DEADLINES_PATH, cols,
		    selection, selectionArgs,
		    DeadlinesContract.DeadlinesColumns.GROUP, having, DeadlinesContract.DeadlinesColumns.GROUP);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
  {
    SQLiteDatabase db = _dbHelper.getWritableDatabase();
    int ret = 0;

    switch (Matcher.match(uri))
    {
      case DEADLINES:
	// TODO placeholder
	break;
      case DEADLINE_ID:
	String whereClause = DeadlinesContract.DeadlinesColumns.ID + " = ?";
	String[] whereArgs =
	{
	  uri.getLastPathSegment()
	};
	ret = db.update(DeadlinesContract.DEADLINES_PATH, values, whereClause, whereArgs);
	break;
      default:
	Log.e(TAG, "UPDATE - Unsupported path:" + uri.getPath());
	throw new IllegalArgumentException("UPDATE - Unsupported path: " + uri.getPath());
    }

    getContext().getContentResolver().notifyChange(DeadlinesContract.BASE_URI, null);
    return ret;
  }
}
