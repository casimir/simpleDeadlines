package com.casimirlab.simpleDeadlines.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.casimirlab.simpleDeadlines.DayCounterView;
import com.casimirlab.simpleDeadlines.R;

import java.text.DateFormat;
import java.util.Date;

public class DeadlineAdapter extends CursorAdapter {
    private boolean _archived;

    public DeadlineAdapter(Context context, Cursor c, boolean archived) {
        super(context, c, false);

        _archived = archived;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Holder holder = (Holder) view.getTag();
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);

        long time = values.getAsLong(DeadlinesContract.Deadlines.DUE_DATE);
        holder.DayCounter.setAutomaticBackground(!_archived);
        holder.DayCounter.setDate(time);

        holder.Label.setText(values.getAsString(DeadlinesContract.Deadlines.LABEL));
        setStrikeText(holder.Label, values.getAsInteger(DeadlinesContract.Deadlines.DONE));

        holder.Group.setText(values.getAsString(DeadlinesContract.Deadlines.GROUP));

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        holder.DueDate.setText(df.format(new Date(time)));

        holder.Done.setOnCheckedChangeListener(null);
        holder.Done.setChecked(
                values.getAsInteger(DeadlinesContract.Deadlines.DONE)
                        == DeadlinesContract.Deadlines.STATE_DONE);
        final ContentResolver cr = context.getContentResolver();
        final int id = values.getAsInteger(DeadlinesContract.Deadlines.ID);
        holder.Done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Uri uri = ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id);
                ContentValues values = new ContentValues();
                int state = isChecked
                        ? DeadlinesContract.Deadlines.STATE_DONE
                        : DeadlinesContract.Deadlines.STATE_NOT_DONE;
                values.put(DeadlinesContract.Deadlines.DONE, state);
                cr.update(uri, values, null, null);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.deadline_entry, parent, false);
        Holder holder = new Holder();
        holder.DayCounter = (DayCounterView) v.findViewById(R.id.day_counter);
        holder.Label = (TextView) v.findViewById(R.id.label);
        holder.Group = (TextView) v.findViewById(R.id.group);
        holder.DueDate = (TextView) v.findViewById(R.id.due_date);
        holder.Done = (CheckBox) v.findViewById(R.id.done);
        v.setTag(holder);
        return v;
    }

    private static void setStrikeText(TextView text, int state) {
        if (state == DeadlinesContract.Deadlines.STATE_DONE)
            text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private static class Holder {
        public DayCounterView DayCounter;
        public TextView Label;
        public TextView Group;
        public TextView DueDate;
        public CheckBox Done;
    }
}