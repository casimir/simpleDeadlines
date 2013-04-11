package com.casimirlab.simpleDeadlines;

import android.app.ActionBar;
import android.content.Intent;
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
import com.casimirlab.simpleDeadlines.data.DataHelper;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;

public class Deadlines extends FragmentActivity
{
  private static final int MAGIC = 0x4242;
  private ActionBar _actionBar;
  private DrawerLayout _drawer;
  private ListView _grouplist;
  private View _filterReset;
  private ViewPager _pager;
  private DataHelper _db;
  private int _currentType;
  private String _currentGroup;

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
	_currentType = position;
	resetAdapters();
	_pager.setCurrentItem(position);
	return true;
      }
    };
    _actionBar.setListNavigationCallbacks(navAdapter, navListener);

    _drawer = new DrawerLayout(this, R.layout.drawer);
    final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
	Intent i = new Intent(Deadlines.this, DeadlineEditor.class);
	i.putExtra(EditorDialogFragment.EXTRA_ID, (int)id);
	startActivityForResult(i, MAGIC);
      }
    };
    _drawer.setCallback(new DrawerLayout.Callback()
    {
      @Override
      public void open()
      {
	// TODO optimize
	TextView drawerTitle = (TextView)_drawer.findViewById(R.id.title);
	_drawer.setMaxWidth(drawerTitle.getMeasuredWidth());
//	getListView().setOnItemClickListener(null);
      }

      @Override
      public void close()
      {
//	getListView().setOnItemClickListener(listener);
      }
    });
    _grouplist = (ListView)_drawer.findViewById(R.id.grouplist);
    _filterReset = _drawer.findViewById(R.id.filter_reset);

    _grouplist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    _grouplist.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
	TextView label = (TextView)view.findViewById(R.id.group);
	updateFilter(label.getText().toString());
	_grouplist.setItemChecked(position, true);
      }
    });
    /*
     getListView().setOnItemClickListener(listener);
     getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
     getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener()
     {
     private List<Integer> _selected;

     public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
     {
     if (checked)
     _selected.add(Integer.valueOf((int)id));
     else
     _selected.remove(Integer.valueOf((int)id));
     mode.setTitle(_selected.size() + " selected items");
     }

     public boolean onCreateActionMode(ActionMode mode, Menu menu)
     {
     MenuInflater inflater = mode.getMenuInflater();
     inflater.inflate(R.menu.cab, menu);
     return true;
     }

     public boolean onPrepareActionMode(ActionMode mode, Menu menu)
     {
     _selected = new ArrayList<Integer>();
     return true;
     }

     public boolean onActionItemClicked(ActionMode mode, MenuItem item)
     {
     if (item.getItemId() == R.id.act_delete)
     {
     for (int id : _selected)
     _db.delete(id);
     resetAdapters();
     mode.finish();
     return true;
     }
     return false;
     }

     public void onDestroyActionMode(ActionMode arg0)
     {
     _selected = null;
     }
     }
     );
     */

    _pager = (ViewPager)findViewById(R.id.pager);
    _pager.setAdapter(new DeadlinePagerAdapter(getSupportFragmentManager()));
    _pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
    {
      @Override
      public void onPageSelected(int position)
      {
	getActionBar().setSelectedNavigationItem(position);
      }
    });

    _db = new DataHelper(this);
    _currentType = DataHelper.TYPE_IN_PROGRESS;
    updateFilter(null);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    resetAdapters();
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
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == MAGIC && resultCode == RESULT_OK)
    {
      _currentType = DataHelper.TYPE_IN_PROGRESS;
      resetAdapters();
    }
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
    i.putExtra(EditorDialogFragment.EXTRA_ISNEW,
	       true);
    startActivityForResult(i, MAGIC);
  }

  private void resetAdapters()
  {
    GroupAdapter groups = new GroupAdapter(this, _db.groups(_currentType));
    _actionBar.setIcon(groups.getCount() > 0 ? R.drawable.ic_act_filter : R.drawable.app_icon);
    _actionBar.setDisplayHomeAsUpEnabled(groups.getCount() > 0);
    _grouplist.setAdapter(groups);
//    setListAdapter(new DeadlineAdapter(this, _db.deadlines(_currentType, _currentGroup)));
  }

  public void resetFilter(View v)
  {
    updateFilter(null);
  }

  private void updateFilter(String group)
  {
    _filterReset.setVisibility(group == null ? View.GONE : View.VISIBLE);
    _currentGroup = group;
    _drawer.close();
    resetAdapters();
  }
}