package com.casimirlab.simpleDeadlines;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity
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
}
