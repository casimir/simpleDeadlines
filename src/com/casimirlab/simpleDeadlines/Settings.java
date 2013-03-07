package com.casimirlab.simpleDeadlines;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceActivity
{
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

	Preference prefVersion = findPreference(getString(R.string.pref_key_about_version));
	try
	{
	  PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	  prefVersion.setSummary(pkgInfo.versionName);
	}
	catch (PackageManager.NameNotFoundException ex)
	{
	  prefVersion.setSummary("Unknown version");
	}
      }
    };

    getFragmentManager().beginTransaction()
	    .replace(android.R.id.content, frag)
	    .commit();
  }
}
