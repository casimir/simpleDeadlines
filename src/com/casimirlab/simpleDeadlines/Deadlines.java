package com.casimirlab.simpleDeadlines;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class Deadlines extends ListActivity
{
  private static final int MAGIC = 0x4242;
  private ActionBar _actionBar;
  private DrawerLayout _drawer;
  private ListView _grouplist;
  private View _filterReset;
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
	return true;
      }
    };
    _actionBar.setListNavigationCallbacks(navAdapter, navListener);

    _drawer = new DrawerLayout(this, R.layout.drawer);
    _grouplist = (ListView)_drawer.findViewById(R.id.grouplist);
    _filterReset = _drawer.findViewById(R.id.filter_reset);

    _grouplist.setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
	TextView label = (TextView)view.findViewById(R.id.group);
	updateFilter(label.getText().toString());
      }
    });
    getListView().setOnItemClickListener(new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
	Intent i = new Intent(Deadlines.this, DeadlineEditor.class);
	i.putExtra(DeadlineEditor.MODEL_ID, (int)id);
	startActivityForResult(i, MAGIC);
      }
    });
    getListView().setOnItemLongClickListener(null);
    registerForContextMenu(getListView());

    _db = new DataHelper(this);
    _currentType = DataHelper.TYPE_PENDING;
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
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
  {
    super.onCreateContextMenu(menu, v, menuInfo);

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.ctxt_deadlines, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item)
  {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    ListAdapter adapter = getListAdapter();

    if (item.getItemId() == R.id.act_edit)
    {
      Intent i = new Intent(Deadlines.this, DeadlineEditor.class);
      i.putExtra(DeadlineEditor.MODEL_ID, (int)adapter.getItemId(info.position));
      startActivityForResult(i, MAGIC);
      return true;
    }
    if (item.getItemId() == R.id.act_delete)
    {
      _db.delete((int)adapter.getItemId(info.position));
      resetAdapters();
      return true;
    }
    return super.onContextItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == MAGIC && resultCode == RESULT_OK)
    {
      _currentType = DataHelper.TYPE_PENDING;
      resetAdapters();
    }
  }

  public void actionNew(View v)
  {
    Intent i = new Intent(this, DeadlineEditor.class);
    i.putExtra(DeadlineEditor.TYPE_NEW, true);
    startActivityForResult(i, MAGIC);
  }

  private void resetAdapters()
  {
    GroupAdapter groups = new GroupAdapter(this, _db.groups(_currentType), _currentGroup);
    _actionBar.setIcon(groups.getCount() > 0 ? R.drawable.ic_act_filter : R.drawable.app_icon);
    _actionBar.setDisplayHomeAsUpEnabled(groups.getCount() > 0);
    _grouplist.setAdapter(groups);
    setListAdapter(new DeadlineAdapter(this, _db, _currentType, _currentGroup));
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