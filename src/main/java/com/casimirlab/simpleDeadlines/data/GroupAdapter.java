package com.casimirlab.simpleDeadlines.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.widget.SimpleCursorAdapter;

import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;

public class GroupAdapter extends SimpleCursorAdapter {
    private static final String[] FromCols = {DeadlinesContract.Deadlines.GROUP};
    private static final int[] ToIDs = {R.id.group};
    private final ContentResolver _cr;

    public GroupAdapter(Context context, Cursor c) {
        super(context, R.layout.group_entry, c, FromCols, ToIDs, 0);

        _cr = context.getContentResolver();
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
}
