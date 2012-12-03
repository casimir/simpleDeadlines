package com.casimirlab.simpleDeadlines;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import java.util.Calendar;

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
  private static final int ALARM_ID = 0x4321;
  private static boolean _notificationShown = false;
  private NotificationCompat.Builder _builder;

  @Override
  public void onReceive(Context context, Intent intent)
  {
    if (_builder == null)
      _builder = new NotificationCompat.Builder(context);

    String action = intent.getAction();
    if (action.equals(ACTION_SET)
	|| action.equals(Intent.ACTION_BOOT_COMPLETED))
      setAlarm(context, intent);
    else if (action.equals(ACTION_SHOW)
	     || action.equals(ACTION_HIDE)
	     || action.equals(ACTION_TOGGLE))
      displayNotification(context, intent);
  }

  private void setAlarm(Context context, Intent intent)
  {
    Bundle extras = intent.getExtras();
    int hour = 0;
    int minute = 0;

    if (extras != null
	&& extras.containsKey(EXTRA_HOUR) && extras.containsKey(EXTRA_MINUTE))
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

    Calendar time = Calendar.getInstance();
    time.set(Calendar.HOUR_OF_DAY, hour);
    time.set(Calendar.MINUTE, minute);

    AlarmManager am = (AlarmManager)context.getSystemService(Service.ALARM_SERVICE);
    PendingIntent pi = PendingIntent.getBroadcast(context, ALARM_ID, new Intent(ACTION_SHOW), PendingIntent.FLAG_CANCEL_CURRENT);
    am.cancel(pi);
    am.setRepeating(AlarmManager.RTC, time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    Log.i(TAG, "Setting alarm " + hour + ":" + minute);
  }

  private void displayNotification(Context context, Intent intent)
  {
    NotificationManager nm = (NotificationManager)context.getSystemService(Service.NOTIFICATION_SERVICE);
    String action = intent.getAction();

    if (action.equals(ACTION_TOGGLE))
      action = _notificationShown ? ACTION_HIDE : ACTION_SHOW;

    if (action.equals(ACTION_SHOW))
    {
      refreshBuilder(context);
      nm.notify(NOTIFICATION_ID, _builder.build());
      _notificationShown = true;
    }
    else if (action.equals(ACTION_HIDE))
    {
      nm.cancel(NOTIFICATION_ID);
      _notificationShown = false;
    }
  }

  private void refreshBuilder(Context context)
  {
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(Deadlines.class);
    stackBuilder.addNextIntent(new Intent(context, Deadlines.class));
    PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    _builder.setSmallIcon(R.drawable.app_icon);
    _builder.setContentTitle(TAG);
    _builder.setContentText(TAG);
    _builder.setContentIntent(pi);
    _builder.setAutoCancel(true);
    _builder.setLights(0xFF00F0F0, 300, 300);
  }
}