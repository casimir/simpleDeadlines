package com.casimirlab.simpleDeadlines;

import android.app.*;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;
import com.casimirlab.simpleDeadlines.ui.MainActivity;

import java.util.Calendar;

public class NotificationCenter extends BroadcastReceiver {
    private static final String TAG = NotificationCenter.class.getSimpleName();
    public static final String ACTION_HIDE = TAG + ".ACTION_HIDE";
    public static final String ACTION_SET = TAG + ".ACTION_SET";
    public static final String ACTION_SHOW = TAG + ".ACTION_SHOW";
    public static final String ACTION_TOGGLE = TAG + ".ACTION_TOGGLE";
    public static final String EXTRA_HOUR = TAG + ".EXTRA_HOUR";
    public static final String EXTRA_MINUTE = TAG + ".EXTRA_MINUTE";

    private static final int ALARM_ID = 0x4321;
    private static final int NOTIFICATION_ID = 0x1234;
    private static boolean _notificationShown = false;

    private Context _context;
    private NotificationManager _nm;
    private ContentResolver _cr;
    private ContentObserver _co;
    private Notification.Builder _builder;

    @Override
    public void onReceive(Context context, Intent intent) {
        _context = context;
        _nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        _cr = context.getContentResolver();

        _co = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_context);
                boolean persist = sp.getBoolean(_context.getString(R.string.pref_key_notif_persist), false);

                if (persist)
                    displayNotification(ACTION_SHOW);
            }
        };
        _cr.registerContentObserver(DeadlinesContract.Count.CONTENT_URI, false, _co);

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        _builder = new Notification.Builder(context);
        _builder.setSmallIcon(R.drawable.ic_status);
        _builder.setContentTitle(TAG);
        _builder.setContentIntent(pi);

        String action = intent.getAction();
        if (action.equals(ACTION_SET) || action.equals(Intent.ACTION_BOOT_COMPLETED))
            setAlarm(intent);
        else
            displayNotification(action);
    }

    private void displayNotification(String action) {
        if (action.equals(ACTION_TOGGLE))
            action = _notificationShown ? ACTION_HIDE : ACTION_SHOW;

        if (action.equals(ACTION_HIDE)) {
            _nm.cancel(NOTIFICATION_ID);
            _notificationShown = false;
            return;
        }

        Cursor c = _cr.query(DeadlinesContract.Count.CONTENT_URI, null, null, null, null);
        if (!c.moveToFirst())
            return;

        RemoteViews view = new RemoteViews(_context.getPackageName(), R.layout.notif);
        view.setTextViewText(R.id.count_today, String.valueOf(c.getInt(0)));
        view.setTextViewText(R.id.count_urgent, String.valueOf(c.getInt(1)));
        view.setTextViewText(R.id.count_worrying, String.valueOf(c.getInt(2)));
        view.setTextViewText(R.id.count_nice, String.valueOf(c.getInt(3)));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_context);
        boolean persist = sp.getBoolean(_context.getString(R.string.pref_key_notif_persist), false);

        _builder.setContent(view);
        _builder.setAutoCancel(!persist);
        _builder.setOngoing(persist);
        _builder.setOnlyAlertOnce(persist);

        _nm.notify(NOTIFICATION_ID, _builder.getNotification());
        _notificationShown = true;
    }

    private void setAlarm(Intent intent) {
        Bundle extras = intent.getExtras();
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 0);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);

        if (extras != null
                && extras.containsKey(EXTRA_HOUR) && extras.containsKey(EXTRA_MINUTE)) {
            time.set(Calendar.HOUR_OF_DAY, extras.getInt(EXTRA_HOUR));
            time.set(Calendar.MINUTE, extras.getInt(EXTRA_MINUTE));
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(_context);
            String timekey = _context.getString(R.string.pref_key_notif_hour);
            String timedefault = _context.getString(R.string.pref_default_notif_hour);
            String[] timetab = sp.getString(timekey, timedefault).split(":");
            time.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timetab[0]));
            time.set(Calendar.MINUTE, Integer.valueOf(timetab[1]));
        }

        AlarmManager am = (AlarmManager) _context.getSystemService(Service.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(_context, ALARM_ID,
                new Intent(ACTION_SHOW),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
}