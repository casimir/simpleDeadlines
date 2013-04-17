package com.casimirlab.simpleDeadlines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.Date;

public class DeadlineProvider extends ContentProvider
{
  private static final int DEADLINES = 0;
  private static final int DEADLINES_ARCHIVED = 10;
  private static final int DEADLINES_GROUPS = 1;
  private static final int DEADLINES_ARCHIVED_GROUPS = 11;
  private static final int DEADLINES_GROUP_LABEL = 2;
  private static final int DEADLINES_ARCHIVED_GROUP_LABEL = 12;
  private static final int DEADLINE_ID = 3;
  private static final String TAG = "DeadlineProvider";
  private static final UriMatcher Matcher = new UriMatcher(UriMatcher.NO_MATCH);
  private DataHelper _dbHelper;

  static
  {
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
  }

  @Override
  public boolean onCreate()
  {
    _dbHelper = new DataHelper(getContext());

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

    getContext().getContentResolver().notifyChange(uri, null);
    return ret;
  }

  @Override
  public String getType(Uri uri)
  {
    switch (Matcher.match(uri))
    {
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
      default:
	return null;
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values)
  {
    if (Matcher.match(uri) != DEADLINE_ID)
    {
      Log.e(TAG, "INSERT - Unsupported path:" + uri.getPath());
      throw new IllegalArgumentException("INSERT - Unsupported path: " + uri.getPath());
    }

    SQLiteDatabase db = _dbHelper.getWritableDatabase();
    long id = db.insert(DeadlinesContract.DEADLINES_PATH, null, values);

    getContext().getContentResolver().notifyChange(uri, null);
    return ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
  {
    Cursor ret = null;

    switch (Matcher.match(uri))
    {
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
	SQLiteDatabase db = _dbHelper.getReadableDatabase();
	ret = db.query(DeadlinesContract.DEADLINES_PATH, null,
		       where, whereArgs,
		       null, null, null);
	break;
      default:
	Log.e(TAG, "QUERY - Unsupported path:" + uri.getPath());
	throw new IllegalArgumentException("QUERY - Unsupported path: " + uri.getPath());
    }

    if (ret != null)
      ret.setNotificationUri(getContext().getContentResolver(), uri);
    return ret;
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
    if (!TextUtils.isEmpty(group))
      groupSelection = " AND " + DeadlinesContract.DeadlinesColumns.GROUP
		       + " = " + DatabaseUtils.sqlEscapeString(group);
    String[] selectionArgs = new String[]
    {
      String.valueOf(new Date().getTime()),
      "1",
      group
    };

    SQLiteDatabase db = _dbHelper.getReadableDatabase();
    return db.query(DeadlinesContract.DEADLINES_PATH, null,
		    selection + groupSelection, selectionArgs,
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

    getContext().getContentResolver().notifyChange(uri, null);
    return ret;
  }
}
