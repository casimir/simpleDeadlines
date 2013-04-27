package com.casimirlab.simpleDeadlines;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.casimirlab.simpleDeadlines.data.DeadlineAdapter;
import com.casimirlab.simpleDeadlines.data.DeadlinesContract;
import java.util.ArrayList;
import java.util.List;

public class DeadlineListFragment extends ListFragment implements LoaderCallbacks<Cursor>
{
  public static final String EXTRA_GROUP = "deadlinelistfragment.group";
  public static final String EXTRA_TYPE = "deadlinelistfragment.type";
  private int _type;
  private CursorAdapter _adapter;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    _type = getArguments().getInt(EXTRA_TYPE, DeadlinesContract.Deadlines.TYPE_IN_PROGRESS);
    _adapter = new DeadlineAdapter(getActivity(), null);
    setListAdapter(_adapter);

    getLoaderManager().initLoader(0, getArguments(), this);
  }

  @Override
  public void onStart()
  {
    super.onStart();

    final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener()
    {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
	Intent i = new Intent(getActivity(), DeadlineEditor.class);
	i.putExtra(EditorDialogFragment.EXTRA_ID, (int)id);
	startActivity(i);
      }
    };
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
	mode.setTitle(_selected.size() + " items"); // TODO res
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
	final ContentResolver cr = getActivity().getContentResolver();

	if (item.getItemId() == R.id.act_delete)
	{
	  for (int id : _selected)
	    cr.delete(ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, id),
		      null, null);
	  mode.finish();
	  return true;
	}
	else if (item.getItemId() == R.id.act_group)
	{
	  final ActionMode actMode = mode;
	  final EditText tInput = new EditText(getActivity());
	  DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
	  {
	    public void onClick(DialogInterface dialog, int which)
	    {
	      if (which == DialogInterface.BUTTON_POSITIVE)
	      {
		ContentValues values = new ContentValues(1);
		values.put(DeadlinesContract.DeadlinesColumns.GROUP, tInput.getText().toString());

		for (int id : _selected)
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
	}

	return false;
      }

      public void onDestroyActionMode(ActionMode mode)
      {
	_selected = null;
      }
    });
  }

  public Loader<Cursor> onCreateLoader(int id, Bundle args)
  {
    Uri.Builder builder = DeadlinesContract.Deadlines.CONTENT_URI.buildUpon();
    if (_type == DeadlinesContract.Deadlines.TYPE_ARCHIVED)
      builder.appendPath(DeadlinesContract.Deadlines.FILTER_ARCHIVED);

    String group = args.getString(EXTRA_GROUP);
    if (!TextUtils.isEmpty(group))
    {
      builder.appendPath(DeadlinesContract.Deadlines.FILTER_GROUP);
      builder.appendPath(group);
    }

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
