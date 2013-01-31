package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.casimirlab.simpleDeadlines.R;
import java.text.DateFormat;
import java.util.Date;

public class DeadlineAdapter extends CursorAdapter
{
  private DataHelper _db;

  public DeadlineAdapter(Context context, Cursor c)
  {
    super(context, c);

    _db = new DataHelper(context);
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor)
  {
    Holder holder = (Holder)view.getTag();
    final DeadlineModel model = DeadlineModel.fromCursor(cursor);

    Date today = new Date();
    today.setHours(0);
    long days = model.DueDate().getTime() / DeadlineModel.DAY_LEN - today.getTime() / DeadlineModel.DAY_LEN;
    holder.Remaining.setText(String.valueOf(days));

    Resources res = context.getResources();
    if (days <= DeadlineModel.LVL_TODAY)
    {
      holder.RemainingBg.setBackgroundResource(R.color.simple_lvl_today);
      holder.Remaining.setTextColor(res.getColor(R.color.simple_lvl_today));
    }
    else if (days <= DeadlineModel.LVL_URGENT)
    {
      holder.RemainingBg.setBackgroundResource(R.color.simple_lvl_urgent);
      holder.Remaining.setTextColor(res.getColor(R.color.simple_lvl_urgent));
    }
    else if (days <= DeadlineModel.LVL_WORRYING)
    {
      holder.RemainingBg.setBackgroundResource(R.color.simple_lvl_worrying);
      holder.Remaining.setTextColor(res.getColor(R.color.simple_lvl_worrying));
    }
    else if (days <= DeadlineModel.LVL_NICE)
    {
      holder.RemainingBg.setBackgroundResource(R.color.simple_lvl_nice);
      holder.Remaining.setTextColor(res.getColor(R.color.simple_lvl_nice));
    }
    else
    {
      holder.RemainingBg.setBackgroundResource(R.color.simple_lvl_other);
      holder.Remaining.setTextColor(res.getColor(R.color.simple_lvl_other));
    }

    holder.Label.setText(model.Label());
    setStrikeText(holder.Label, model.Done());

    holder.Group.setText(model.Group());

    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
    holder.DueDate.setText(df.format(model.DueDate()));

    holder.Done.setChecked(model.Done());
    final TextView label = holder.Label;
    holder.Done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
    {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
      {
	model.setDone(isChecked);
	setStrikeText(label, isChecked);
	_db.update(model);
	// FIXME autorefresh
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
  }
}