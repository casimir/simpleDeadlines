package com.casimirlab.simpleDeadlines;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import java.util.Date;

public class DeadlineModel
{
  public static final long DAY_LEN = 1000 * 60 * 60 * 24;
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
    model.setId(values.getAsInteger(DataHelper.KEY_ID));
    model.setLabel(values.getAsString(DataHelper.KEY_LABEL));
    model.setGroup(values.getAsString(DataHelper.KEY_GROUP));
    model.setDueDate(new Date(values.getAsLong(DataHelper.KEY_DUE_DATE)));
    model.setDone(values.getAsInteger(DataHelper.KEY_DONE) == 1);
    return model;
  }

  public static ContentValues toValues(DeadlineModel model)
  {
    ContentValues values = new ContentValues();
    values.put(DataHelper.KEY_LABEL, model.Label());
    values.put(DataHelper.KEY_GROUP, model.Group());
    values.put(DataHelper.KEY_DUE_DATE, model.DueDate().getTime());
    values.put(DataHelper.KEY_DONE, model.Done() ? 1 : 0);
    return values;
  }
}