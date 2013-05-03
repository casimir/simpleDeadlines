package com.casimirlab.simpleDeadlines;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import com.casimirlab.simpleDeadlines.data.DeadlineUtils;

public class Settings extends PreferenceActivity
{
  private static final String TAG = "Settings";

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    PreferenceFragment frag = new PreferenceFragment()
    {
      @Override
      public void onCreate(Bundle savedInstanceState)
      {
	super.onCreate(savedInstanceState);
	addPreferencesFromResource(R.xml.preferences);

	Preference prefPersit = findPreference(getString(R.string.pref_key_notif_persist));
	prefPersit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
	{
	  public boolean onPreferenceChange(Preference preference, Object newValue)
	  {
	    String act = (Boolean)newValue
		    ? NotificationCenter.ACTION_SHOW
		    : NotificationCenter.ACTION_HIDE;
	    sendBroadcast(new Intent(act));
	    return true;
	  }
	});

	Preference prefToggle = findPreference(getString(R.string.pref_key_notif_toggle));
	prefToggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
	{
	  public boolean onPreferenceClick(Preference preference)
	  {
	    sendBroadcast(new Intent(NotificationCenter.ACTION_TOGGLE));
	    return true;
	  }
	});

	Preference prefBackup = findPreference(getString(R.string.pref_key_backup_do));
	prefBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
	{
	  public boolean onPreferenceClick(Preference preference)
	  {
	    Intent i = new Intent();
	    i.setAction(Intent.ACTION_SEND);
	    i.setType("text/plain");
	    i.putExtra(Intent.EXTRA_STREAM, DeadlineUtils.performBackup(getApplicationContext()));
	    startActivity(i);
	    return true;
	  }
	});

	Preference prefVersion = findPreference(getString(R.string.pref_key_about_version));
	try
	{
	  PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	  prefVersion.setSummary(pkgInfo.versionName);
	}
	catch (PackageManager.NameNotFoundException ex)
	{
	  Log.e(TAG, "Failed to retrieve version number", ex);
	}
      }
    };

    getFragmentManager().beginTransaction()
	    .replace(android.R.id.content, frag)
	    .commit();
  }
}
