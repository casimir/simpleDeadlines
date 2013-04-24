package com.casimirlab.simpleDeadlines.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import java.util.Date;

public class DeadlineModel
{
  public static final int LVL_TODAY = 1;
  public static final int LVL_URGENT = 3;
  public static final int LVL_WORRYING = 7;
  public static final int LVL_NICE = 15;
  private int _id;
  private String _label;
  private String _group;
  private Date _dueDate;
  private boolean _done;

  public int Id()
  {
    return _id;
  }

  public void setId(int id)
  {
    _id = id;
  }

  public String Label()
  {
    return _label;
  }

  public void setLabel(String label)
  {
    _label = label;
  }

  public String Group()
  {
    return _group;
  }

  public void setGroup(String group)
  {
    _group = group;
  }

  public Date DueDate()
  {
    return _dueDate;
  }

  public void setDueDate(Date dueDate)
  {
    _dueDate = dueDate;
  }

  public boolean Done()
  {
    return _done;
  }

  public void setDone(boolean done)
  {
    _done = done;
  }

  public static DeadlineModel fromCursor(Cursor cursor)
  {
    ContentValues values = new ContentValues();
    DatabaseUtils.cursorRowToContentValues(cursor, values);
    DeadlineModel model = new DeadlineModel();
    model.setDone(values.getAsInteger(DeadlinesContract.DeadlinesColumns.DONE) == 1);
    model.setDueDate(new Date(values.getAsLong(DeadlinesContract.DeadlinesColumns.DUE_DATE)));
    model.setGroup(values.getAsString(DeadlinesContract.DeadlinesColumns.GROUP));
    model.setId(values.getAsInteger(DeadlinesContract.DeadlinesColumns.ID));
    model.setLabel(values.getAsString(DeadlinesContract.DeadlinesColumns.LABEL));
    return model;
  }
}