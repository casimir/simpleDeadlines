package com.casimirlab.simpleDeadlines.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.format.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * @see DeadlinesContract
 */
public class DeadlineProvider extends ContentProvider {
    /**
     * Matcher ID for the count pattern.
     */
    private static final int MATCH_COUNTS = 0;
    /**
     * Matcher ID for the deadlines pattern.
     */
    private static final int MATCH_DEADLINES = 10;
    /**
     * Matcher ID for the archived deadlines pattern.
     */
    private static final int MATCH_DEADLINES_ARCHIVED = 11;
    /**
     * Matcher ID for a list of specific (by group) archived deadlines pattern.
     */
    private static final int MATCH_DEADLINES_ARCHIVED_GROUP_LABEL = 12;
    /**
     * Matcher ID for a list of specific (by group) deadlines pattern.
     */
    private static final int MATCH_DEADLINES_GROUP_LABEL = 13;
    /**
     * Matcher ID for a unique (by ID) deadline.
     */
    private static final int MATCH_DEADLINE_ID = 14;
    /**
     * Matcher ID for the group pattern.
     */
    private static final int MATCH_GROUPS = 20;
    /**
     * Matcher ID for the archived groups pattern.
     */
    private static final int MATCH_GROUP_ARCHIVED = 21;
    /**
     * Matcher ID for the in progress groups pattern.
     */
    private static final int MATCH_GROUP_IN_PROGRESS = 22;

    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Count.TABLE_NAME,
                MATCH_COUNTS);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME,
                MATCH_DEADLINES);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME + "/archived",
                MATCH_DEADLINES_ARCHIVED);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME + "/archived/group/*",
                MATCH_DEADLINES_ARCHIVED_GROUP_LABEL);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME + "/group/*",
                MATCH_DEADLINES_GROUP_LABEL);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME + "/#",
                MATCH_DEADLINE_ID);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Groups.TABLE_NAME,
                MATCH_GROUPS);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME + "/archived/groups",
                MATCH_GROUP_ARCHIVED);
        MATCHER.addURI(DeadlinesContract.AUTHORITY,
                DeadlinesContract.Deadlines.TABLE_NAME + "/groups",
                MATCH_GROUP_IN_PROGRESS);
    }

    private DBHelper _dbHelper;

    @Override
    public boolean onCreate() {
        _dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (MATCHER.match(uri) != MATCH_DEADLINE_ID)
            throw new IllegalArgumentException("Unknown or malformed URI. {uri: " + uri + "}");

        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        String where = DeadlinesContract.Deadlines.ID + " = " + uri.getLastPathSegment();
        int ret = db.delete(DeadlinesContract.Deadlines.TABLE_NAME, where, null);

        if (ret != -1)
            getContext().getContentResolver().notifyChange(DeadlinesContract.AUTHORITY_URI, null);
        return ret;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (MATCHER.match(uri) != MATCH_DEADLINES)
            throw new IllegalArgumentException("Unknown or malformed URI. {uri: " + uri + "}");

        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        long id = db.insert(DeadlinesContract.Deadlines.TABLE_NAME, null, values);

        if (id != -1) {
            getContext().getContentResolver().notifyChange(DeadlinesContract.AUTHORITY_URI, null);
            return ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id);
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] groupCols = {DeadlinesContract.Deadlines.ID, DeadlinesContract.Deadlines.GROUP};
        int matchCode = MATCHER.match(uri);
        SQLiteDatabase db = _dbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        Cursor cursor = null;

        builder.setTables(DeadlinesContract.Deadlines.TABLE_NAME);

        /**
         * Specific cases.
         */
        if (matchCode == MATCH_COUNTS)
            cursor = queryCount();
        else if (matchCode == MATCH_DEADLINE_ID) {
            builder.appendWhere(DeadlinesContract.Deadlines.ID + " = " + uri.getLastPathSegment());
            cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        } else if (matchCode == MATCH_GROUPS) {
            String having = DeadlinesContract.Deadlines.GROUP + " != ''";
            cursor = builder.query(db, groupCols, selection, selectionArgs,
                    DeadlinesContract.Deadlines.GROUP, having, sortOrder);
        }

        /**
         * No need to continue if it was a specific case.
         */
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        /**
         * General cases.
         */
        String archivedSelection = DeadlinesContract.Deadlines.DONE + " = 1 AND "
                + DeadlinesContract.Deadlines.DUE_DATE + " < " + String.valueOf(new Date().getTime());
        if (matchCode != MATCH_DEADLINES_ARCHIVED
                && matchCode != MATCH_DEADLINES_ARCHIVED_GROUP_LABEL
                && matchCode != MATCH_GROUP_ARCHIVED)
            archivedSelection = "NOT(" + archivedSelection + ")";
        builder.appendWhere(archivedSelection);

        if (matchCode == MATCH_DEADLINES_GROUP_LABEL
                || matchCode == MATCH_DEADLINES_ARCHIVED_GROUP_LABEL) {
            String group = DatabaseUtils.sqlEscapeString(uri.getLastPathSegment());
            builder.appendWhere("AND " + DeadlinesContract.Deadlines.GROUP + " = " + group);
        }

        if (matchCode == MATCH_GROUP_ARCHIVED
                || matchCode == MATCH_GROUP_IN_PROGRESS) {
            String having = DeadlinesContract.Deadlines.GROUP + " != ''";
            cursor = builder.query(db, groupCols, selection, selectionArgs,
                    DeadlinesContract.Deadlines.GROUP, having, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public Cursor queryCount() {
        MatrixCursor c = new MatrixCursor(DeadlinesContract.CountColumns.ALL, 1);
        // FIXME avoidable SQL ?
        String sql = "SELECT COUNT(*) FROM " + DeadlinesContract.Deadlines.TABLE_NAME
                + " WHERE " + DeadlinesContract.Deadlines.DUE_DATE
                + " <= ? AND " + DeadlinesContract.Deadlines.DONE + " = 0;";
        SQLiteStatement req = _dbHelper.getReadableDatabase().compileStatement(sql);
        String[] param = new String[1];
        int[] levels = new int[]{
                DeadlineUtils.LVL_TODAY,
                DeadlineUtils.LVL_URGENT,
                DeadlineUtils.LVL_WORRYING,
                DeadlineUtils.LVL_NICE
        };
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);

        int count = 0;
        MatrixCursor.RowBuilder builder = c.newRow();
        for (int lvl : levels) {
            long diff = today.getTimeInMillis() + lvl * DateUtils.DAY_IN_MILLIS;
            param[0] = String.valueOf(diff);
            int value = (int) DatabaseUtils.longForQuery(req, param) - count;
            builder.add(value);
            count += value;
        }

        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (MATCHER.match(uri) != MATCH_DEADLINE_ID)
            throw new IllegalArgumentException("Unknown or malformed URI. {uri: " + uri + "}");

        SQLiteDatabase db = _dbHelper.getWritableDatabase();
        String where = DeadlinesContract.Deadlines.ID + " = " + uri.getLastPathSegment();
        int ret = db.update(DeadlinesContract.Deadlines.TABLE_NAME, values, where, null);

        if (ret != -1)
            getContext().getContentResolver().notifyChange(DeadlinesContract.AUTHORITY_URI, null);
        return ret;
    }
}
