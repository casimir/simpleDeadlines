package com.casimirlab.simpleDeadlines;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class GroupAdapter extends CursorAdapter
{
  private static final String TAG = "GroupAdapter";
  private DataHelper _db;
  private String _current;

  public GroupAdapter(Context context, Cursor c, String current)
  {
    super(context, c);

    _db = new DataHelper(context);
    _current = current;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor)
  {
    TextView label = (TextView)view.findViewById(R.id.group);
    int idx = cursor.getColumnIndex(DataHelper.KEY_GROUP);
    label.setText(cursor.getString(idx));

    if (_current != null && label.getText().equals(_current))
      label.setTextColor(Color.RED);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent)
  {
    return LayoutInflater.from(context).inflate(R.layout.group_entry, parent, false);
  }

  @Override
  public Cursor runQueryOnBackgroundThread(CharSequence constraint)
  {
    if (getFilterQueryProvider() == null || constraint == null)
      return super.runQueryOnBackgroundThread(constraint);
    return _db.filteredGroups(constraint);
  }

  @Override
  public CharSequence convertToString(Cursor cursor)
  {
    int idx = cursor.getColumnIndex(DataHelper.KEY_GROUP);
    return cursor.getString(idx);
  }

  @Override
  public Filter getFilter()
  {
    Log.d(TAG, "Filter required");
    return super.getFilter();
  }
}
