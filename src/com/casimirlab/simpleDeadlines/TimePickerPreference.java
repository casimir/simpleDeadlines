package com.casimirlab.simpleDeadlines;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.TimePicker;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimePickerPreference extends DialogPreference
{
  private static final String DEFAULT_VALUE = Resources.getSystem().getString(R.string.pref_default_notif_hour);
  private Context _context;
  private TimePicker _picker;

  public TimePickerPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setDialogLayoutResource(R.layout.time_picker_preference);

    _context = context;
  }

  @Override
  protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
  {
    super.onSetInitialValue(restorePersistedValue, defaultValue);

    String value = restorePersistedValue
	    ? getPersistedString(DEFAULT_VALUE) : String.valueOf(defaultValue);
    String[] timetab = value.split(":");
    setSummary(Integer.valueOf(timetab[0]), Integer.valueOf(timetab[1]));
  }

  @Override
  protected void onBindDialogView(View view)
  {
    super.onBindDialogView(view);
    _picker = (TimePicker)view.findViewById(R.id.time_picker);

    String[] timetab = getPersistedString(DEFAULT_VALUE).split(":");
    _picker.setCurrentHour(Integer.valueOf(timetab[0]));
    _picker.setCurrentMinute(Integer.valueOf(timetab[1]));
  }

  @Override
  protected void onDialogClosed(boolean positiveResult)
  {
    super.onDialogClosed(positiveResult);

    if (positiveResult)
    {
      String s = String.format("%02d:%02d", _picker.getCurrentHour(), _picker.getCurrentMinute());
      persistString(s);
      setSummary(_picker.getCurrentHour(), _picker.getCurrentMinute());

      Intent i = new Intent(NotificationCenter.ACTION_SET);
      i.putExtra(NotificationCenter.EXTRA_HOUR, _picker.getCurrentHour());
      i.putExtra(NotificationCenter.EXTRA_MINUTE, _picker.getCurrentMinute());
      _context.sendBroadcast(i);
    }
  }

  private void setSummary(int hour, int minute)
  {
    String format = DateFormat.is24HourFormat(_context) ? "HH:mm" : "h:mm aa";
    SimpleDateFormat df = new SimpleDateFormat(format);
    setSummary(df.format(new Date(0, 0, 0, hour, minute)));
  }
}
