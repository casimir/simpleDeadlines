package com.casimirlab.simpleDeadlines.data;

import android.content.ContentValues;
import android.net.Uri;
import java.util.List;

public final class DeadlineUtils
{
  public static final int LVL_TODAY = 1;
  public static final int LVL_URGENT = 3;
  public static final int LVL_WORRYING = 7;
  public static final int LVL_NICE = 15;

  public static ContentValues UriToContentValues(Uri u)
  {
    ContentValues values = new ContentValues(4);
    List<String> segments = u.getPathSegments();

    values.put(DeadlinesContract.DeadlinesColumns.LABEL, segments.get(0));
    values.put(DeadlinesContract.DeadlinesColumns.GROUP, segments.get(1));
    values.put(DeadlinesContract.DeadlinesColumns.DUE_DATE, Long.getLong(segments.get(2)));
    values.put(DeadlinesContract.DeadlinesColumns.DONE, segments.get(3));

    return values;
  }
}
