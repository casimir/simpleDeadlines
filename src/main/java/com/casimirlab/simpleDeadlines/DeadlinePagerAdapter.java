package com.casimirlab.simpleDeadlines;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;
import java.util.LinkedList;
import java.util.List;

public class DeadlinePagerAdapter extends FragmentStatePagerAdapter
{
  private List<DeadlineListFragment> _frags;

  public DeadlinePagerAdapter(FragmentManager fm)
  {
    super(fm);

    _frags = new LinkedList<DeadlineListFragment>();
    _frags.add(DeadlineListFragment.newInstance(DeadlinesContract.Deadlines.TYPE_IN_PROGRESS));
    _frags.add(DeadlineListFragment.newInstance(DeadlinesContract.Deadlines.TYPE_ARCHIVED));
  }

  @Override
  public Fragment getItem(int i)
  {
    return _frags.get(i);
  }

  @Override
  public int getCount()
  {
    return _frags.size();
  }
}
