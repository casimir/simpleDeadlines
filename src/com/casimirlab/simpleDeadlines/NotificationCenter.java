package com.casimirlab.simpleDeadlines;

import android.app.AlarmManager;
import android.app.Notification;
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
import java.util.Calendar;

public class NotificationCenter extends BroadcastReceiver
{
  public static final String ACTION_SET = "NotificationCenter.ACTION_SET";
  public static final String ACTION_SHOW = "NotificationCenter.ACTION_SHOW";
  public static final String ACTION_HIDE = "NotificationCenter.ACTION_HIDE";
  public static final String ACTION_TOGGLE = "NotificationCenter.ACTION_TOGGLE";
  public static final String EXTRA_HOUR = "NotificationCenter.EXTRA_HOUR";
  public static final String EXTRA_MINUTE = "NotificationCenter.EXTRA_MINUTE";
  private static final String TAG = "NotificationCenter";
  private static final int NOTIFICATION_ID = 0x1234;
  private static final int ALARM_ID = 0x4321;
  private static boolean _notificationShown = false;
  private Context _context;

  @Override
  public void onReceive(Context context, Intent intent)
  {
    _context = context;
    String action = intent.getAction();
    if (action.equals(ACTION_SET)
	|| action.equals(Intent.ACTION_BOOT_COMPLETED))
      setAlarm(intent);
    else if (action.equals(ACTION_SHOW)
	     || action.equals(ACTION_HIDE)
	     || action.equals(ACTION_TOGGLE))
      displayNotification(intent);
  }

  private void setAlarm(Intent intent)
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
      SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_context);
      String timekey = _context.getString(R.string.pref_key_notif_hour);
      String timedefault = _context.getString(R.string.pref_default_notif_hour);
      String[] timetab = sp.getString(timekey, timedefault).split(":");
      hour = Integer.valueOf(timetab[0]);
      minute = Integer.valueOf(timetab[1]);
    }

    Calendar time = Calendar.getInstance();
    time.set(Calendar.HOUR_OF_DAY, hour);
    time.set(Calendar.MINUTE, minute);
    time.set(Calendar.SECOND, 0);

    AlarmManager am = (AlarmManager)_context.getSystemService(Service.ALARM_SERVICE);
    PendingIntent pi = PendingIntent.getBroadcast(_context, ALARM_ID, new Intent(ACTION_SHOW), PendingIntent.FLAG_CANCEL_CURRENT);
    am.cancel(pi);
    am.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
  }

  private void displayNotification(Intent intent)
  {
    NotificationManager nm = (NotificationManager)_context.getSystemService(Service.NOTIFICATION_SERVICE);
    String action = intent.getAction();

    if (action.equals(ACTION_TOGGLE))
      action = _notificationShown ? ACTION_HIDE : ACTION_SHOW;

    if (action.equals(ACTION_SHOW))
    {
      DataHelper db = new DataHelper(_context);
      NotificationAdapter adapter = new NotificationAdapter(_context, db, R.layout.notif);
      if (!adapter.isEmpty())
      {
	nm.notify(NOTIFICATION_ID, makeNotification(adapter));
	_notificationShown = true;
      }
    }
    else if (action.equals(ACTION_HIDE))
    {
      nm.cancel(NOTIFICATION_ID);
      _notificationShown = false;
    }
  }

  private Notification makeNotification(NotificationAdapter adapter)
  {
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(_context);
    stackBuilder.addParentStack(Deadlines.class);
    stackBuilder.addNextIntent(new Intent(_context, Deadlines.class));
    PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Builder builder = new Notification.Builder(_context);
    builder.setAutoCancel(true);
    builder.setSmallIcon(R.drawable.ic_status);
    builder.setContentTitle(TAG);
    builder.setContent(adapter.getView()); // bugged
    builder.setContentIntent(pi);

    Notification notif = builder.getNotification();
//    notif.contentView = adapter.getView();
    return notif;
  }
}