package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;
import com.casimirlab.simpleDeadlines.R;
import java.util.Map;

public class NotificationAdapter
{
  private Context _context;
  private DataHelper _db;
  private int _resLayout;

  public NotificationAdapter(Context context, DataHelper db, int layout)
  {
    _context = context;
    _db = db;
    _resLayout = layout;
  }

  public boolean isEmpty()
  {
    return _db.count() == 0;
  }

  public RemoteViews getView()
  {
    Map<Integer, Integer> counts = _db.counts();
    RemoteViews view = new RemoteViews(_context.getPackageName(), _resLayout);
    view.setTextViewText(R.id.count_today, String.valueOf(counts.get(DeadlineModel.LVL_TODAY)));
    view.setTextColor(R.id.count_today, Color.rgb(0xFF, 0x00, 0x00));
    view.setTextViewText(R.id.count_urgent, String.valueOf(counts.get(DeadlineModel.LVL_URGENT)));
    view.setTextColor(R.id.count_urgent, Color.rgb(0xFF, 0xA5, 0x00));
    view.setTextViewText(R.id.count_worrying, String.valueOf(counts.get(DeadlineModel.LVL_WORRYING)));
    view.setTextColor(R.id.count_worrying, Color.rgb(0xFF, 0xFF, 0x00));
    view.setTextViewText(R.id.count_nice, String.valueOf(counts.get(DeadlineModel.LVL_NICE)));
    view.setTextColor(R.id.count_nice, Color.argb(204, 0x00, 0xFF, 0x00));
    return view;
  }
}
