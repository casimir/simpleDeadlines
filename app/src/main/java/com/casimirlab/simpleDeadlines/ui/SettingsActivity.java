package com.casimirlab.simpleDeadlines.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.casimirlab.simpleDeadlines.NotificationCenter;
import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.data.DeadlinesUtils;

public class SettingsActivity extends PreferenceActivity {
    private static final int RESULT_RECOVER_PICK = 1;
    private static final String TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceFragment frag = new PreferenceFragment() {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.preferences);

                Preference prefPersist = findPreference(getString(R.string.pref_key_notif_persist));
                prefPersist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String act = (Boolean) newValue
                                ? NotificationCenter.ACTION_SHOW
                                : NotificationCenter.ACTION_HIDE;
                        sendBroadcast(new Intent(act));
                        return true;
                    }
                });

                Preference prefToggle = findPreference(getString(R.string.pref_key_notif_toggle));
                prefToggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        sendBroadcast(new Intent(NotificationCenter.ACTION_TOGGLE));
                        return true;
                    }
                });

                Preference prefBackup = findPreference(getString(R.string.pref_key_backup_do));
                prefBackup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_STREAM, DeadlinesUtils.performBackup(getApplicationContext()));
                        startActivity(i);
                        return true;
                    }
                });

                Preference prefRecover = findPreference(getString(R.string.pref_key_recover_do));
                prefRecover.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                        i.setType("*/*");
                        SettingsActivity.this.startActivityForResult(i, RESULT_RECOVER_PICK);
                        return true;
                    }
                });

                Preference prefVersion = findPreference(getString(R.string.pref_key_about_version));
                try {
                    PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    prefVersion.setSummary(pkgInfo.versionName);
                } catch (PackageManager.NameNotFoundException ex) {
                    Log.e(TAG, "Failed to retrieve version number", ex); // FIXME translate
                }
            }
        };

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, frag)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_RECOVER_PICK == requestCode) {
            if (RESULT_OK == resultCode)
                DeadlinesUtils.performRecover(getApplicationContext(), data.getData());
            else
                Toast.makeText(this, "Failed to perform recover", Toast.LENGTH_SHORT).show(); // FIXME translate
        }
    }
}
