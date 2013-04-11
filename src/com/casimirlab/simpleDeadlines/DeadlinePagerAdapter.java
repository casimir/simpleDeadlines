package com.casimirlab.simpleDeadlines;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class DeadlinePagerAdapter extends FragmentStatePagerAdapter
{
  private DeadlineListFragment _inProgressFragment;
  private DeadlineListFragment _archivedFragment;

  public DeadlinePagerAdapter(FragmentManager fm)
  {
    super(fm);

    _inProgressFragment = DeadlineListFragment.newInstance(DeadlineListFragment.TYPE_IN_PROGRESS);
    _archivedFragment = DeadlineListFragment.newInstance(DeadlineListFragment.TYPE_ARCHIVED);
  }

  @Override
  public Fragment getItem(int i)
  {
    if (i == DeadlineListFragment.TYPE_IN_PROGRESS)
      return _inProgressFragment;
    else if (i == DeadlineListFragment.TYPE_ARCHIVED)
      return _archivedFragment;
    return null;
  }

  @Override
  public int getCount()
  {
    return 2;
  }
}
