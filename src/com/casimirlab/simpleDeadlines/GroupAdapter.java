package com.casimirlab.simpleDeadlines;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class GroupAdapter extends CursorAdapter
{
  private String _current;

  public GroupAdapter(Context context, Cursor c, String current)
  {
    super(context, c);
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
}
