package com.casimirlab.simpleDeadlines;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

public class Deadlines extends ListActivity
{
  private static final int MAGIC = 0x4242;
  private DataHelper _db;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.deadlines);

    if (savedInstanceState == null)
      _db = new DataHelper(this);

    setListAdapter(new DeadlineListAdapter(this, _db));
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
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.deadlines, menu);
    MenuItemCompat.setShowAsAction(menu.findItem(R.id.act_new), MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
    MenuItemCompat.setShowAsAction(menu.findItem(R.id.act_new), MenuItemCompat.SHOW_AS_ACTION_NEVER);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == R.id.act_new)
      onActNew(null);
    else if (item.getItemId() == R.id.act_settings)
      startActivity(new Intent(this, Settings.class));
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
      i.putExtra(DeadlineEditor.MODEL_ID, ((int)adapter.getItemId(info.position)));
      startActivityForResult(i, MAGIC);
      return true;
    }
    if (item.getItemId() == R.id.act_delete)
    {
      _db.delete((int)adapter.getItemId(info.position));
      setListAdapter(new DeadlineListAdapter(this, _db));
      return true;
    }
    return super.onContextItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == MAGIC && resultCode == RESULT_OK)
      setListAdapter(new DeadlineListAdapter(this, _db));
  }

  public void onActNew(View v)
  {
    Intent i = new Intent(this, DeadlineEditor.class);
    i.putExtra(DeadlineEditor.TYPE_NEW, true);
    startActivityForResult(i, MAGIC);
  }
}