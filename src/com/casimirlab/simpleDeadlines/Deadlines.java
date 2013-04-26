package com.casimirlab.simpleDeadlines;

import android.app.ActionBar;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.casimirlab.simpleDeadlines.data.DeadlinesContract;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;

public class Deadlines extends FragmentActivity implements LoaderCallbacks<Cursor>
{
  private ActionBar _actionBar;
  private DrawerLayout _drawer;
  private ListView _grouplist;
  private GroupAdapter _groupAdapter;
  private ViewPager _pager;
  private DeadlinePagerAdapter _pagerAdapter;
  private int _currentGroupIdx;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.deadlines);

    _actionBar = getActionBar();
    _actionBar.setDisplayShowTitleEnabled(false);
    _actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    SpinnerAdapter navAdapter = ArrayAdapter.createFromResource(this,
								R.array.act_nav_list,
								android.R.layout.simple_spinner_dropdown_item);
    ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener()
    {
      public boolean onNavigationItemSelected(int position, long itemId)
      {
	_pager.setCurrentItem(position);
	return true;
      }
    };
    _actionBar.setListNavigationCallbacks(navAdapter, navListener);

    _drawer = new DrawerLayout(this, R.layout.drawer);
    _drawer.setCallback(new DrawerLayout.Callback()
    {
      @Override
      public void open()
      {
	TextView drawerTitle = (TextView)_drawer.findViewById(R.id.title);
	_drawer.setMaxWidth(drawerTitle.getMeasuredWidth());
      }

      @Override
      public void close()
      {
      }
    });
    _groupAdapter = new GroupAdapter(this, null);
    _grouplist = (ListView)_drawer.findViewById(R.id.grouplist);
    _grouplist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    _grouplist.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
	boolean wasSelected = _currentGroupIdx == position;
	TextView label = (TextView)view.findViewById(R.id.group);
	DeadlineListFragment frag = (DeadlineListFragment)_pagerAdapter.getItem(_pager.getCurrentItem());

	_grouplist.setItemChecked(position, !wasSelected);
	_currentGroupIdx = _grouplist.getCheckedItemPosition();
	frag.setGroupFilter(wasSelected ? null : label.getText().toString());
	_drawer.close();
      }
    });
    _grouplist.setAdapter(_groupAdapter);

    _pager = (ViewPager)findViewById(R.id.pager);
    _pagerAdapter = new DeadlinePagerAdapter(getSupportFragmentManager());
    _pager.setAdapter(_pagerAdapter);
    _pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
    {
      @Override
      public void onPageSelected(int position)
      {
	_actionBar.setSelectedNavigationItem(position);
	getLoaderManager().restartLoader(0, null, Deadlines.this);
      }
    });

    _currentGroupIdx = ListView.INVALID_POSITION;

    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

//    updateDrawer();
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    Uri.Builder builder = DeadlinesContract.Deadlines.CONTENT_URI.buildUpon();
    if (_actionBar.getSelectedNavigationIndex() == 1)
      builder.appendPath(DeadlinesContract.Deadlines.FILTER_ARCHIVED);
    builder.appendPath(DeadlinesContract.GROUPS_PATH);

    return new CursorLoader(this, builder.build(), null,
			    null, null, null);
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor c)
  {
    _groupAdapter.swapCursor(c);

    _actionBar.setIcon(_groupAdapter.getCount() > 0 ? R.drawable.ic_act_filter : R.drawable.app_icon);
    _actionBar.setDisplayHomeAsUpEnabled(_groupAdapter.getCount() > 0);
  }

  public void onLoaderReset(Loader<Cursor> loader)
  {
    _groupAdapter.swapCursor(null);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.deadlines, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == android.R.id.home)
    {
      _drawer.toggle();
      return true;
    }
    if (item.getItemId() == R.id.act_new)
    {
      actionNew(null);
      return true;
    }
    else if (item.getItemId() == R.id.act_settings)
    {
      startActivity(new Intent(this, Settings.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed()
  {
    if (!_drawer.isOpened())
      super.onBackPressed();
    else
      _drawer.close();
  }

  public void actionNew(View v)
  {
    Intent i = new Intent(this, DeadlineEditor.class);
    i.putExtra(EditorDialogFragment.EXTRA_ISNEW, true);
    startActivity(i);
  }
}