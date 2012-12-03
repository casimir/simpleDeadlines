package com.casimirlab.simpleDeadlines;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class NotificationCenter extends BroadcastReceiver
{
  public static final String TAG = "NotificationCenter";
  public static final String ACTION_SET = "NotificationCenter.ACTION_SET";
  public static final String ACTION_SHOW = "NotificationCenter.ACTION_SHOW";
  public static final String ACTION_HIDE = "NotificationCenter.ACTION_HIDE";
  public static final String ACTION_TOGGLE = "NotificationCenter.ACTION_TOGGLE";
  public static final String EXTRA_HOUR = "NotificationCenter.EXTRA_HOUR";
  public static final String EXTRA_MINUTE = "NotificationCenter.EXTRA_MINUTE";
  private static final int NOTIFICATION_ID = 0x1234;

  @Override
  public void onReceive(Context context, Intent intent)
  {
    String action = intent.getAction();
    if (action.equals(ACTION_SET))
      setAlarm(context, intent);
    else if (action.equals(ACTION_SHOW) || action.equals(ACTION_HIDE)
	     || action.equals(ACTION_TOGGLE))
      displayNotification(context, intent);
  }

  private void setAlarm(Context context, Intent intent)
  {
    Bundle extras = intent.getExtras();
    int hour = 0;
    int minute = 0;

    if (extras.containsKey(EXTRA_HOUR) && extras.containsKey(EXTRA_MINUTE))
    {
      hour = extras.getInt(EXTRA_HOUR);
      minute = extras.getInt(EXTRA_MINUTE);
    }
    else
    {
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
      String timekey = context.getString(R.string.pref_key_notif_hour);
      String timedefault = context.getString(R.string.pref_default_notif_hour);
      String[] timetab = sp.getString(timekey, timedefault).split(":");
      hour = Integer.valueOf(timetab[0]);
      minute = Integer.valueOf(timetab[1]);
    }

    Log.i(TAG, "Setting alarm " + hour + ":" + minute);
  }

  private void displayNotification(Context context, Intent intent)
  {
    String action = intent.getAction();
    if (action.equals(ACTION_SHOW))
      Toast.makeText(context, "Show notification", Toast.LENGTH_SHORT).show();
    else if (action.equals(ACTION_HIDE))
      Toast.makeText(context, "Hide notification", Toast.LENGTH_SHORT).show();
    else if (action.equals(ACTION_TOGGLE))
      Toast.makeText(context, "Toggle notification", Toast.LENGTH_SHORT).show();
  }
}
