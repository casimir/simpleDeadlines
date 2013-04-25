package com.casimirlab.simpleDeadlines.data;

import android.net.Uri;

public final class DeadlinesContract
{
  public static final String AUTHORITY = "com.casimirlab.simpleDeadlines.provider";
  public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
  public static final String COUNT_PATH = "count";
  public static final String DEADLINES_PATH = "deadlines";
  public static final String GROUPS_PATH = "groups";

  public static final class Count
  {
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/simpledeadlines.count";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, COUNT_PATH);
  }

  public static final class CountColumns
  {
    public static final String TODAY = "today";
    public static final String URGENT = "urgent";
    public static final String WORRYING = "worrying";
    public static final String NICE = "nice";
    public static final String[] ALL =
    {
      TODAY, URGENT, WORRYING, NICE
    };
  }

  public static final class Deadlines
  {
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/simpledeadlines.deadline";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/simpledeadlines.deadline";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, DEADLINES_PATH);
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
    public static final String[] ALL =
    {
      ID, LABEL, GROUP, DUE_DATE, DONE
    };
  }

  public static final class Groups
  {
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/simpledeadlines.group";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/simpledeadlines.group";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, GROUPS_PATH);
  }
}
