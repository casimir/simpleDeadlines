package com.casimirlab.simpleDeadlines.ui;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.data.DeadlineAdapter;
import com.casimirlab.simpleDeadlines.data.DeadlinesUtils;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;

import java.util.ArrayList;
import java.util.List;

public class DeadlineListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private static final String TAG = DeadlineListFragment.class.getSimpleName();
    public static final String EXTRA_ARCHIVED = TAG + ".archived";
    public static final String EXTRA_GROUP = TAG + ".group";
    private CursorAdapter _adapter;
    private boolean _archiveMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _archiveMode  = getArguments().getBoolean(EXTRA_ARCHIVED);
        _adapter = new DeadlineAdapter(getActivity(), null, _archiveMode);
        setListAdapter(_adapter);

        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onStart() {
        super.onStart();

        final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), DeadlineEditor.class);
                i.putExtra(EditorDialogFragment.EXTRA_ID, (int) id);
                startActivity(i);
            }
        };
        getListView().setOnItemClickListener(listener);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private List<Long> _selected;

            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked)
                    _selected.add(id);
                else
                    _selected.remove(id);
                mode.setTitle(String.valueOf(_selected.size()));
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                _selected = new ArrayList<>();

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.cab, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                final ContentResolver cr = getActivity().getContentResolver();

                if (item.getItemId() == R.id.act_share) {
                    String text = "";
                    for (long id : _selected) {
                        Cursor c = cr.query(ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id), null,
                                null, null, null);
                        c.moveToFirst();
                        ContentValues values = new ContentValues();
                        DatabaseUtils.cursorRowToContentValues(c, values);
                        text += (text.equals("") ? "" : "\n");
                        text += DeadlinesUtils.contentValuesToShareUri(values);
                    }

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);

                    mode.finish();
                    return true;
                } else if (item.getItemId() == R.id.act_group) {
                    final ActionMode actMode = mode;
                    final EditText tInput = new EditText(getActivity());
                    tInput.setSingleLine();
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                ContentValues values = new ContentValues(1);
                                values.put(DeadlinesContract.Deadlines.GROUP, tInput.getText().toString());

                                for (long id : _selected)
                                    cr.update(ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id),
                                            values, null, null);
                            }

                            dialog.dismiss();
                            actMode.finish();
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.hint_group);
                    builder.setView(tInput);
                    builder.setPositiveButton(android.R.string.ok, listener);
                    builder.setNegativeButton(android.R.string.cancel, listener);
                    builder.show();

                    return true;
                } else if (item.getItemId() == R.id.act_delete) {
                    for (long id : _selected)
                        cr.delete(ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id),
                                null, null);
                    mode.finish();
                    return true;
                }

                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                _selected = null;
            }
        });

        setEmptyText(getString(R.string.empty_list));
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri.Builder builder = DeadlinesContract.Deadlines.CONTENT_URI.buildUpon();
        String order = "ASC";

        if (_archiveMode){
            builder.appendPath(DeadlinesContract.Deadlines.FILTER_ARCHIVED);
            order = "DESC";
        }

        String group = args != null ? args.getString(EXTRA_GROUP) : "";
        if (!TextUtils.isEmpty(group)) {
            builder.appendPath(DeadlinesContract.Deadlines.FILTER_GROUP);
            builder.appendPath(group);
        }

        String orderBy = DeadlinesContract.Deadlines.DUE_DATE + " " + order;
        return new CursorLoader(getActivity(), builder.build(), null, null, null, orderBy);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        _adapter.swapCursor(c);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        _adapter.swapCursor(null);
    }

    public void setGroupFilter(String group) {
        Bundle args = new Bundle();
        args.putString(EXTRA_GROUP, group);
        getLoaderManager().restartLoader(0, args, this);
    }

    public void setArchivedMode(boolean enabled) {
       _archiveMode = enabled;
        getLoaderManager().restartLoader(0, null, this);
    }

    public static DeadlineListFragment newInstance(Boolean archived) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ARCHIVED, archived);

        DeadlineListFragment f = new DeadlineListFragment();
        f.setArguments(args);

        return f;
    }
}
