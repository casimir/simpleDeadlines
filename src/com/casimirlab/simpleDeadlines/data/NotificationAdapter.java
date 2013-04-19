package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.widget.RemoteViews;
import com.casimirlab.simpleDeadlines.R;
import java.util.Map;

public class NotificationAdapter
{
  private Context _context;
  private Map<Integer, Integer> _counts;
  private int _resLayout;

  public NotificationAdapter(Context context, DBHelper db, int layout)
  {
    _context = context;
    _counts = db.counts();
    _resLayout = layout;
  }

  public boolean isEmpty()
  {
    int sum = 0;
    for (int count : _counts.values())
      sum += count;
    return sum == 0;
  }

  public RemoteViews getView()
  {
    RemoteViews view = new RemoteViews(_context.getPackageName(), _resLayout);
    view.setTextViewText(R.id.count_today, _counts.get(DeadlineModel.LVL_TODAY).toString());
    view.setTextColor(R.id.count_today,
		      _context.getResources().getColor(R.color.simple_lvl_today));
    view.setTextViewText(R.id.count_urgent, _counts.get(DeadlineModel.LVL_URGENT).toString());
    view.setTextColor(R.id.count_urgent,
		      _context.getResources().getColor(R.color.simple_lvl_urgent));
    view.setTextViewText(R.id.count_worrying, _counts.get(DeadlineModel.LVL_WORRYING).toString());
    view.setTextColor(R.id.count_worrying,
		      _context.getResources().getColor(R.color.simple_lvl_worrying));
    view.setTextViewText(R.id.count_nice, _counts.get(DeadlineModel.LVL_NICE).toString());
    view.setTextColor(R.id.count_nice,
		      _context.getResources().getColor(R.color.simple_lvl_nice));
    return view;
  }
}
