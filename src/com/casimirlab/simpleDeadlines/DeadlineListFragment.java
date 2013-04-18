package com.casimirlab.simpleDeadlines;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.CursorAdapter;
import com.casimirlab.simpleDeadlines.data.DeadlineAdapter;
import com.casimirlab.simpleDeadlines.data.DeadlinesContract;

public class DeadlineListFragment extends ListFragment implements LoaderCallbacks<Cursor>
{
  public static final String EXTRA_GROUP = "deadlinelistfragment.group";
  public static final String EXTRA_TYPE = "deadlinelistfragment.type";
  public static final int TYPE_ARCHIVED = DeadlinesContract.Deadlines.TYPE_ARCHIVED;
  public static final int TYPE_IN_PROGRESS = DeadlinesContract.Deadlines.TYPE_IN_PROGRESS;
  private int _type;
  private CursorAdapter _adapter;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    _type = getArguments().getInt(EXTRA_TYPE, TYPE_IN_PROGRESS);
    _adapter = new DeadlineAdapter(getActivity(), null);
    setListAdapter(_adapter);

    getLoaderManager().initLoader(0, getArguments(), this);
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    Uri.Builder builder = DeadlinesContract.Deadlines.CONTENT_URI.buildUpon();
    if (_type == TYPE_ARCHIVED)
      builder.appendPath(DeadlinesContract.Deadlines.FILTER_ARCHIVED);

    String group = args.getString(EXTRA_GROUP);
    if (!TextUtils.isEmpty(group))
      builder.appendQueryParameter(DeadlinesContract.Deadlines.FILTER_GROUP, group);

    return new CursorLoader(getActivity(), builder.build(), null, null, null, null);
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor c)
  {
    _adapter.swapCursor(c);
  }

  public void onLoaderReset(Loader<Cursor> loader)
  {
    _adapter.swapCursor(null);
  }

  public void setGroupFilter(String group)
  {
    Bundle args = new Bundle();
    args.putInt(EXTRA_TYPE, _type);
    args.putString(EXTRA_GROUP, group);
    getLoaderManager().restartLoader(0, args, this);
  }

  public static DeadlineListFragment newInstance(int type)
  {
    Bundle args = new Bundle();
    args.putInt(EXTRA_TYPE, type);

    DeadlineListFragment f = new DeadlineListFragment();
    f.setArguments(args);

    return f;
  }
}
