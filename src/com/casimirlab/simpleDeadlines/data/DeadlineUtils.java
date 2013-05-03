package com.casimirlab.simpleDeadlines.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MergeCursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.casimirlab.simpleDeadlines.R;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DeadlineUtils
{
  public static final String BACKUP_FILENAME = "backup.sd";
  public static final int LVL_TODAY = 1;
  public static final int LVL_URGENT = 3;
  public static final int LVL_WORRYING = 7;
  public static final int LVL_NICE = 15;
  public static final int LVL_NEVERMIND = -1;
  public static final int[] LVL_ALL =
  {
    LVL_TODAY, LVL_URGENT, LVL_WORRYING, LVL_NICE, LVL_NEVERMIND
  };
  public static final Uri SHARE_BASE_URI = Uri.parse("http://sd.casimir-lab.net");
  private static final String TAG = "DeadlineUtils";

  public static Uri contentValuesToShareUri(ContentValues values)
  {
    Uri.Builder builder = SHARE_BASE_URI.buildUpon();
    String group = values.getAsString(DeadlinesContract.DeadlinesColumns.GROUP);

    builder.appendPath(values.getAsString(DeadlinesContract.DeadlinesColumns.LABEL));
    if (TextUtils.isEmpty(group))
      builder.appendEncodedPath("%00");
    else
      builder.appendPath(group);
    builder.appendPath(values.getAsString(DeadlinesContract.DeadlinesColumns.DUE_DATE));
    builder.appendPath(values.getAsString(DeadlinesContract.DeadlinesColumns.DONE));

    return builder.build();
  }

  public static Uri performBackup(Context context)
  {
    ContentResolver cr = context.getContentResolver();
    Uri.Builder uriBuilder = DeadlinesContract.Deadlines.CONTENT_URI.buildUpon();
    Uri archivedUri = uriBuilder.appendPath(DeadlinesContract.Deadlines.FILTER_ARCHIVED).build();
    MergeCursor c = new MergeCursor(new Cursor[]
    {
      cr.query(DeadlinesContract.Deadlines.CONTENT_URI, null, null, null, null),
      cr.query(archivedUri, null, null, null, null)
    });

    String buff = "";
    ContentValues values = new ContentValues();
    while (c.moveToNext())
    {
      DatabaseUtils.cursorRowToContentValues(c, values);
      buff += (buff.equals("") ? "" : "\n");
      buff += contentValuesToShareUri(values);
      values.clear();
    }

    try
    {
      FileOutputStream fos = context.openFileOutput(BACKUP_FILENAME, Context.MODE_WORLD_READABLE);
      fos.write(buff.getBytes());
      fos.close();
      return Uri.fromFile(context.getFileStreamPath(BACKUP_FILENAME));
    }
    catch (Exception ex)
    {
      Log.e(TAG, "Failed to perform backup", ex);
    }
    return Uri.EMPTY;
  }

  public static int performRecover(Context context, Uri uri)
  {
    ContentResolver cr = context.getContentResolver();
    try
    {
      InputStream is = cr.openInputStream(uri);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line;
      int count = 0;

      while ((line = br.readLine()) != null)
      {
	ContentValues values = shareUriToContentValues(Uri.parse(line));
	cr.insert(DeadlinesContract.Deadlines.CONTENT_URI, values);
	++count;
      }

      return count;
    }
    catch (FileNotFoundException ex)
    {
      Log.e(TAG, "Backup file not found", ex);
    }
    catch (IOException ex)
    {
      Log.e(TAG, "Failed to read backup file", ex);
    }

    return 0;
  }

  public static int resColorFromLevel(int level)
  {
    switch (level)
    {
      case LVL_TODAY:
	return R.color.lvl_today;
      case LVL_URGENT:
	return R.color.lvl_urgent;
      case LVL_WORRYING:
	return R.color.lvl_worrying;
      case LVL_NICE:
	return R.color.lvl_nice;
      default:
	return R.color.lvl_other;
    }
  }

  public static ContentValues shareUriToContentValues(Uri uri)
  {
    List<String> segments = uri.getPathSegments();
    if (segments.size() < 4)
      throw new IllegalArgumentException(String.format("Malformed Uri: %s", uri));

    ContentValues values = new ContentValues(4);

    if (TextUtils.isEmpty(segments.get(0)))
      throw new IllegalArgumentException(String.format("Empty label: %s", uri));
    values.put(DeadlinesContract.DeadlinesColumns.LABEL, segments.get(0));

    if (segments.get(1).getBytes()[0] == 0)
      values.put(DeadlinesContract.DeadlinesColumns.GROUP, (String)null);
    else
      values.put(DeadlinesContract.DeadlinesColumns.GROUP, segments.get(1));

    if (TextUtils.isEmpty(segments.get(2)) || !TextUtils.isDigitsOnly(segments.get(2)))
      throw new IllegalArgumentException(String.format("Malformed date: %s", uri));
    values.put(DeadlinesContract.DeadlinesColumns.DUE_DATE, Long.parseLong(segments.get(2)));

    if (TextUtils.isEmpty(segments.get(3)) || !TextUtils.isDigitsOnly(segments.get(3)))
      throw new IllegalArgumentException(String.format("Malformed done state: %s", uri));
    values.put(DeadlinesContract.DeadlinesColumns.DONE, Integer.parseInt(segments.get(3)));

    return values;
  }
}
