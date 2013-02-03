package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.casimirlab.simpleDeadlines.R;

public class GroupAdapter extends CursorAdapter
{
  private static final String TAG = "GroupAdapter";
  private DataHelper _db;

  public GroupAdapter(Context context, Cursor c, String current)
  {
    super(context, c);

    _db = new DataHelper(context);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor)
  {
    Holder h = (Holder)view.getTag();
    TextView label = h.Label;
    final int idx = cursor.getColumnIndex(DataHelper.KEY_GROUP);
    label.setText(cursor.getString(idx));
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent)
  {
    View v = LayoutInflater.from(context).inflate(R.layout.group_entry, parent, false);
    Holder h = new Holder();
    h.Label = (TextView)v.findViewById(R.id.group);
    v.setTag(h);
    return v;
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

  private static class Holder
  {
    public TextView Label;
  }
}
