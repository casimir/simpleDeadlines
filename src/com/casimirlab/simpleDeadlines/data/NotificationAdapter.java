package com.casimirlab.simpleDeadlines.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.widget.RemoteViews;
import com.casimirlab.simpleDeadlines.R;

public class NotificationAdapter
{
  private Context _context;
  private ContentResolver _cr;
  private Cursor _cursor;
  private int _resLayout;

  public NotificationAdapter(Context context, int layout)
  {
    _context = context;
    _resLayout = layout;

    _cr = context.getContentResolver();
    _cursor = _cr.query(DeadlinesContract.Count.CONTENT_URI, null, null, null, null);
  }

  public boolean isEmpty()
  {
    return _cursor.getCount() <= 0;
  }

  public RemoteViews getView()
  {
    if (!_cursor.moveToFirst())
      throw new UnsupportedOperationException("Empty count data");

    RemoteViews view = new RemoteViews(_context.getPackageName(), _resLayout);
    view.setTextViewText(R.id.count_today, String.valueOf(_cursor.getInt(0)));
    view.setTextColor(R.id.count_today,
		      _context.getResources().getColor(R.color.simple_lvl_today));
    view.setTextViewText(R.id.count_urgent, String.valueOf(_cursor.getInt(1)));
    view.setTextColor(R.id.count_urgent,
		      _context.getResources().getColor(R.color.simple_lvl_urgent));
    view.setTextViewText(R.id.count_worrying, String.valueOf(_cursor.getInt(2)));
    view.setTextColor(R.id.count_worrying,
		      _context.getResources().getColor(R.color.simple_lvl_worrying));
    view.setTextViewText(R.id.count_nice, String.valueOf(_cursor.getInt(3)));
    view.setTextColor(R.id.count_nice,
		      _context.getResources().getColor(R.color.simple_lvl_nice));
    return view;
  }
}
