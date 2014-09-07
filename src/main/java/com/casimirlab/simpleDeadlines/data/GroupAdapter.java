package com.casimirlab.simpleDeadlines.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;

public class GroupAdapter extends CursorAdapter {
    private final ContentResolver _cr;

    public GroupAdapter(Context context, Cursor c) {
        super(context, c, false);
        _cr = context.getContentResolver();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Holder h = (Holder) view.getTag();
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        String label = values.getAsString(DeadlinesContract.Deadlines.GROUP);

        h.Color.setBackgroundColor(DeadlinesUtils.labelToColor(label));
        h.Label.setText(label);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = LayoutInflater.from(context).inflate(R.layout.group_entry, viewGroup, false);
        Holder h = new Holder();
        h.Color = (FrameLayout) v.findViewById(R.id.group_color);
        h.Label = (TextView) v.findViewById(R.id.group);
        v.setTag(h);
        return v;
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (constraint == null)
            return super.runQueryOnBackgroundThread(null);

        String filter = DatabaseUtils.sqlEscapeString(constraint + "%");
        String selection = DeadlinesContract.Deadlines.GROUP + " LIKE " + filter;
        return _cr.query(DeadlinesContract.Groups.CONTENT_URI, null, selection, null, null);
    }

    @Override
    public CharSequence convertToString(@NonNull Cursor c) {
        int idx = c.getColumnIndex(DeadlinesContract.Deadlines.GROUP);
        return c.getString(idx);
    }

    private static class Holder {
        public FrameLayout Color;
        public TextView Label;
    }
}
