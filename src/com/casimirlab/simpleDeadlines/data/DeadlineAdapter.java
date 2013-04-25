package com.casimirlab.simpleDeadlines.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.casimirlab.simpleDeadlines.R;
import java.text.DateFormat;
import java.util.Calendar;

public class DeadlineAdapter extends CursorAdapter
{
  public DeadlineAdapter(Context context, Cursor c)
  {
    super(context, c);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor)
  {
    Holder holder = (Holder)view.getTag();
    final DeadlineModel model = DeadlineModel.fromCursor(cursor);

    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    long todayMs = today.getTimeInMillis() / DateUtils.DAY_IN_MILLIS;
    long deadlineMs = model.DueDate().getTime() / DateUtils.DAY_IN_MILLIS;
    long days = deadlineMs - todayMs;
    holder.Remaining.setText(String.valueOf(days));

    if (days <= DeadlineModel.LVL_TODAY)
      holder.setColor(context, R.color.simple_lvl_today);
    else if (days <= DeadlineModel.LVL_URGENT)
      holder.setColor(context, R.color.simple_lvl_urgent);
    else if (days <= DeadlineModel.LVL_WORRYING)
      holder.setColor(context, R.color.simple_lvl_worrying);
    else if (days <= DeadlineModel.LVL_NICE)
      holder.setColor(context, R.color.simple_lvl_nice);
    else
      holder.setColor(context, R.color.simple_lvl_other);

    holder.Label.setText(model.Label());
    setStrikeText(holder.Label, model.Done());

    holder.Group.setText(model.Group());

    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
    holder.DueDate.setText(df.format(model.DueDate()));

    holder.Done.setOnCheckedChangeListener(null);
    holder.Done.setChecked(model.Done());
    final Context ctxt = context;
    final TextView label = holder.Label;
    final int id = cursor.getInt(cursor.getColumnIndex(DeadlinesContract.DeadlinesColumns.ID));
    holder.Done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
	model.setDone(isChecked);
	setStrikeText(label, isChecked);

	ContentResolver cr = ctxt.getContentResolver();
	Uri uri = ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id);
	ContentValues values = new ContentValues();
	values.put(DeadlinesContract.DeadlinesColumns.DONE, isChecked ? 1 : 0);
	cr.update(uri, values, null, null);
      }
    });
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent)
  {
    View v = LayoutInflater.from(context).inflate(R.layout.deadline_entry, parent, false);
    Holder h = new Holder();
    h.RemainingBg = v.findViewById(R.id.remaining_bg);
    h.Remaining = (TextView)v.findViewById(R.id.remaining);
    h.Label = (TextView)v.findViewById(R.id.label);
    h.Group = (TextView)v.findViewById(R.id.group);
    h.DueDate = (TextView)v.findViewById(R.id.due_date);
    h.Done = (CheckBox)v.findViewById(R.id.done);
    v.setTag(h);
    return v;
  }

  private static void setStrikeText(TextView text, boolean strike)
  {
    if (strike)
      text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    else
      text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
  }

  private static class Holder
  {
    public View RemainingBg;
    public TextView Remaining;
    public TextView Label;
    public TextView Group;
    public TextView DueDate;
    public CheckBox Done;

    public void setColor(Context context, int resColor)
    {
      RemainingBg.setBackgroundResource(resColor);
      Resources res = context.getResources();
      Remaining.setTextColor(res.getColor(resColor));
    }
  }
}