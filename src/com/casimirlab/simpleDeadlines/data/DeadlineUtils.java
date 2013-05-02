package com.casimirlab.simpleDeadlines.data;

import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;
import java.util.List;

public final class DeadlineUtils
{
  public static final int LVL_TODAY = 1;
  public static final int LVL_URGENT = 3;
  public static final int LVL_WORRYING = 7;
  public static final int LVL_NICE = 15;
  public static final Uri SHARE_BASE_URI = Uri.parse("sd.casimir-lab.net");

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

  public static ContentValues shareUriToContentValues(Uri uri)
  {
    List<String> segments = uri.getPathSegments();
    if (segments.size() < 4)
      throw new IllegalArgumentException(String.format("Malformed Uri: %s", uri));

    ContentValues values = new ContentValues(4);

    if (TextUtils.isEmpty(segments.get(0)))
      throw new IllegalArgumentException(String.format("Empty label: %s", uri));
    values.put(DeadlinesContract.DeadlinesColumns.LABEL, segments.get(0));

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
