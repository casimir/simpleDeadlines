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
import android.widget.SpinnerAdapter;

public class Deadlines extends ListActivity
{
  private static final int MAGIC = 0x4242;
  private DrawerLayout _drawer;
  private DataHelper _db;
  private int _currentType;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.deadlines);

    ActionBar bar = getActionBar();
    bar.setDisplayShowTitleEnabled(false);
    bar.setDisplayHomeAsUpEnabled(true);
    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    SpinnerAdapter navAdapter = ArrayAdapter.createFromResource(this,
								R.array.act_nav_list,
								android.R.layout.simple_spinner_dropdown_item);
    ActionBar.OnNavigationListener navListener = new ActionBar.OnNavigationListener()
    {
      public boolean onNavigationItemSelected(int position, long itemId)
      {
	_currentType = position;
	resetListAdapter();
	return true;
      }
    };
    bar.setListNavigationCallbacks(navAdapter, navListener);

    _drawer = new DrawerLayout(this, R.layout.drawer);
    _db = new DataHelper(this);
    _currentType = DataHelper.TYPE_PENDING;
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
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    resetListAdapter();
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
      onActNew(null);
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
      resetListAdapter();
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
      resetListAdapter();
    }
  }

  public void onActNew(View v)
  {
    Intent i = new Intent(this, DeadlineEditor.class);
    i.putExtra(DeadlineEditor.TYPE_NEW, true);
    startActivityForResult(i, MAGIC);
  }

  private void resetListAdapter()
  {
    setListAdapter(new DeadlineAdapter(this, _db, _currentType));
  }
}