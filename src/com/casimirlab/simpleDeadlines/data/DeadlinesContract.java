package com.casimirlab.simpleDeadlines.data;

import android.net.Uri;

public final class DeadlinesContract
{
  public static final String AUTHORITY = "com.casimirlab.simpleDeadlines.provider";
  public static final String DEADLINES_PATH = "deadlines";

  public static final class Deadlines
  {
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/simpledeadlines.deadline";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/simpledeadlines.deadline";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DEADLINES_PATH);
    public static final String FILTER_ARCHIVED = "archived";
    public static final String FILTER_GROUP = "group";
    public static final int TYPE_ARCHIVED = 0;
    public static final int TYPE_IN_PROGRESS = 1;
  }

  public static final class DeadlinesColumns
  {
    public static final String ID = "_id";
    public static final String LABEL = "label";
    public static final String GROUP = "groupname";
    public static final String DUE_DATE = "due_date";
    public static final String DONE = "done";
  }

  public static final class Groups
  {
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/simpledeadlines.group";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/simpledeadlines.group";
  }
}
