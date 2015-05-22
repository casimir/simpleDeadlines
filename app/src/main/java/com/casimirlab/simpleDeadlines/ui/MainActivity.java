package com.casimirlab.simpleDeadlines.ui;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.data.DeadlinesUtils;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;

public class MainActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {
    private DrawerLayout _drawerLayout;
    private ActionBarDrawerToggle _drawerToggle;
    private DeadlineListFragment _frag;
    private ListView _groupList;
    private GroupAdapter _groupAdapter;
    private int _currentGroupIdx;
    private boolean _archiveMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.deadlines);

        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, R.string.drawer_open, R.string.drawer_close);
        _drawerLayout.setDrawerListener(_drawerToggle);

        _groupAdapter = new GroupAdapter(this, null);
        _groupList = (ListView) findViewById(R.id.grouplist);
        _groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean wasSelected = _currentGroupIdx == position;
                TextView label = (TextView) view.findViewById(R.id.group);

                _groupList.setItemChecked(position, !wasSelected);
                _currentGroupIdx = _groupList.getCheckedItemPosition();
                _frag.setGroupFilter(wasSelected ? null : label.getText().toString());
                _drawerLayout.closeDrawers();
            }
        });
        _groupList.setAdapter(_groupAdapter);

        _frag = DeadlineListFragment.newInstance(_archiveMode);
        getSupportFragmentManager().beginTransaction().replace(R.id.deadlines, _frag).commit();

        _currentGroupIdx = ListView.INVALID_POSITION;

        getLoaderManager().initLoader(0, null, this);

        final Uri backupUri = getIntent().getData();
        if (backupUri != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.msg_confirm_recover);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int count = DeadlinesUtils.performRecover(getApplicationContext(), backupUri);
                    String msg = getString(R.string.msg_recover, count);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        _drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        _drawerToggle.onConfigurationChanged(newConfig);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri.Builder builder = DeadlinesContract.Deadlines.CONTENT_URI.buildUpon();
        if (_archiveMode)
            builder.appendPath(DeadlinesContract.Deadlines.FILTER_ARCHIVED);
        builder.appendPath(DeadlinesContract.GROUPS_PATH);

        return new CursorLoader(this, builder.build(), null,
                null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        _groupAdapter.swapCursor(c);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        _groupAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deadlines, menu);

        MenuItem toggleItem = menu.findItem(R.id.act_toggle);
        if (_archiveMode) {
            menu.removeItem(R.id.act_new);
            toggleItem.setIcon(R.drawable.ic_act_in_progress);
            toggleItem.setTitle(R.string.act_in_progress);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (_drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.act_new:
                Intent intent = new Intent(this, DeadlineEditor.class);
                intent.putExtra(EditorDialogFragment.EXTRA_ISNEW, true);
                startActivity(intent);
                break;
            case R.id.act_toggle:
                toggleArchivedMode();
                break;
            case R.id.act_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void toggleArchivedMode() {
        _archiveMode = !_archiveMode;
        _frag.setArchivedMode(_archiveMode);
        getLoaderManager().restartLoader(0, null, this);
        setTitle(_archiveMode ? R.string.act_archived : R.string.act_in_progress);
        invalidateOptionsMenu();
    }
}