package com.casimirlab.simpleDeadlines;

import android.content.Intent;
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

	Preference pref = findPreference(getString(R.string.pref_key_notif_toggle));
	pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
	{
	  public boolean onPreferenceClick(Preference preference)
	  {
	    sendBroadcast(new Intent(NotificationCenter.ACTION_TOGGLE));
	    return true;
	  }
	});
      }
    };

    getFragmentManager().beginTransaction()
	    .replace(android.R.id.content, frag)
	    .commit();
  }
}
