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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.data.DeadlineUtils;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
    private String[] _TITLES;
    private DrawerLayout _drawerLayout;
    private ActionBarDrawerToggle _drawerToggle;
    private ListView _grouplist;
    private GroupAdapter _groupAdapter;
    private ViewPager _pager;
    private DeadlinePagerAdapter _pagerAdapter;
    private int _currentGroupIdx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deadlines);

        _TITLES = getResources().getStringArray(R.array.act_nav_list);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.ic_app);
        getActionBar().setTitle(_TITLES[0]);

        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        _drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close);
        _drawerLayout.setDrawerListener(_drawerToggle);

        _groupAdapter = new GroupAdapter(this, null);
        _grouplist = (ListView) findViewById(R.id.grouplist);
        _grouplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean wasSelected = _currentGroupIdx == position;
                TextView label = (TextView) view.findViewById(R.id.group);
                DeadlineListFragment frag = (DeadlineListFragment) _pagerAdapter.getItem(_pager.getCurrentItem());

                _grouplist.setItemChecked(position, !wasSelected);
                _currentGroupIdx = _grouplist.getCheckedItemPosition();
                frag.setGroupFilter(wasSelected ? null : label.getText().toString());
                _drawerLayout.closeDrawers();
            }
        });
        _grouplist.setAdapter(_groupAdapter);

        _pager = (ViewPager) findViewById(R.id.pager);
        _pagerAdapter = new DeadlinePagerAdapter(getSupportFragmentManager());
        _pager.setAdapter(_pagerAdapter);
        _pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActionBar().setTitle(_TITLES[position]);
                _currentGroupIdx = ListView.INVALID_POSITION;
                ((DeadlineListFragment) _pagerAdapter.getItem(position)).setGroupFilter(null);
                getLoaderManager().restartLoader(position, null, MainActivity.this);
            }
        });

        _currentGroupIdx = ListView.INVALID_POSITION;

        getLoaderManager().initLoader(0, null, this);

        final Uri backupUri = getIntent().getData();
        if (backupUri != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.msg_confirm_recover);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int count = DeadlineUtils.performRecover(getApplicationContext(), backupUri);
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
        if (id == 1)
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (_drawerToggle.onOptionsItemSelected(item))
            return true;

        if (item.getItemId() == R.id.act_new) {
            Intent i = new Intent(this, DeadlineEditor.class);
            i.putExtra(EditorDialogFragment.EXTRA_ISNEW, true);
            startActivity(i);
            return true;
        }

        if (item.getItemId() == R.id.act_settings) {
            startActivity(new Intent(this, Settings.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}