package com.casimirlab.simpleDeadlines;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import com.casimirlab.simpleDeadlines.data.DeadlineAdapter;
import com.casimirlab.simpleDeadlines.data.DeadlineListLoader;

public class DeadlineListFragment extends ListFragment implements LoaderCallbacks<DeadlineAdapter>
{
  public static final String EXTRA_GROUP = "deadlinelistfragment.group";
  public static final String EXTRA_TYPE = "deadlinelistfragment.type";
  public static final int TYPE_IN_PROGRESS = 0;
  public static final int TYPE_ARCHIVED = 1;
  private int _type;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    _type = getArguments().getInt(EXTRA_TYPE, TYPE_IN_PROGRESS);
  }

  @Override
  public void onAttach(Activity activity)
  {
    super.onAttach(activity);

    getLoaderManager().initLoader(0, getArguments(), this);
  }

  public Loader<DeadlineAdapter> onCreateLoader(int id, Bundle args)
  {
    String group = args.getString(EXTRA_GROUP);
    return new DeadlineListLoader(getActivity(), _type, group);
  }

  public void onLoadFinished(Loader<DeadlineAdapter> loader, DeadlineAdapter data)
  {
    setListAdapter(data);
  }

  public void onLoaderReset(Loader<DeadlineAdapter> loader)
  {
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
