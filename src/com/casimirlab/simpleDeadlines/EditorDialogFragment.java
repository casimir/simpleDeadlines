package com.casimirlab.simpleDeadlines;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.casimirlab.simpleDeadlines.data.DeadlineModel;
import com.casimirlab.simpleDeadlines.data.DeadlinesContract;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class EditorDialogFragment extends DialogFragment
{
  public static final String EXTRA_ID = "editorfragment.id";
  public static final String EXTRA_ISNEW = "editorfragment.typenew";
  private int _modelId;
  private boolean _isNew;
  private ContentResolver _cr;
  private Uri _currentUri;
  private TextView _labelView;
  private AutoCompleteTextView _groupView;
  private DatePicker _dueDateView;
  private boolean _isDone;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    _modelId = args.getInt(EXTRA_ID);
    _isNew = args.getBoolean(EXTRA_ISNEW);

    _cr = getActivity().getContentResolver();
    _currentUri = ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, _modelId);

    _isDone = !_isNew;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState)
  {
    LayoutInflater inflater = LayoutInflater.from(getActivity());
    View v = inflater.inflate(R.layout.deadline_editor, null);
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
    boolean showCalendar = sp.getBoolean(getString(R.string.pref_key_editor_calendar), false);

    _labelView = (EditText)v.findViewById(R.id.label);
    _groupView = (AutoCompleteTextView)v.findViewById(R.id.group);
    _dueDateView = (DatePicker)v.findViewById(R.id.due_date);
    _dueDateView.setSpinnersShown(!showCalendar);
    _dueDateView.setCalendarViewShown(showCalendar);
    _dueDateView.getCalendarView().setFirstDayOfWeek(Calendar.getInstance().getFirstDayOfWeek());

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface dialog, int which)
      {
	if (which == Dialog.BUTTON_NEGATIVE)
	{
	  dialog.cancel();
	  getActivity().finish();
	  return;
	}

	if (TextUtils.isEmpty(_labelView.getText().toString()))
	{
	  Toast.makeText(getActivity(), R.string.empty_label, Toast.LENGTH_SHORT).show();
	  getActivity().finish();
	  return;
	}

	if (which == Dialog.BUTTON_POSITIVE)
	{
	  if (_isNew)
	    _cr.insert(DeadlinesContract.Deadlines.CONTENT_URI, getValues());
	  else
	    _cr.update(_currentUri, getValues(), null, null);
	}
	else if (which == Dialog.BUTTON_NEUTRAL)
	  _cr.delete(_currentUri, null, null);

	dialog.dismiss();
	getActivity().finish();
      }
    };

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
	    .setTitle(_isNew ? R.string.act_new : R.string.act_edit)
	    .setView(v)
	    .setCancelable(true)
	    .setPositiveButton(android.R.string.ok, listener)
	    .setNegativeButton(android.R.string.cancel, listener);

    builder.setOnKeyListener(new DialogInterface.OnKeyListener()
    {
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
      {
	if (keyCode == KeyEvent.KEYCODE_BACK)
	  getActivity().finish();
	return false;
      }
    });


    if (!_isNew)
      builder.setNeutralButton(R.string.act_delete, listener);

    return builder.create();
  }

  @Override
  public void onStart()
  {
    super.onStart();

    if (!_isNew)
    {
      Cursor c = _cr.query(_currentUri, null, null, null, null);
      c.moveToFirst();
      DeadlineModel model = DeadlineModel.fromCursor(c);

      _labelView.setText(model.Label());

      _groupView.setText(model.Group());
      Cursor cGroups = _cr.query(DeadlinesContract.Groups.CONTENT_URI, null, null, null, null);
      _groupView.setAdapter(new GroupAdapter(getActivity(), cGroups));

      Calendar dateValue = Calendar.getInstance();
      dateValue.setTime(model.DueDate());
      _dueDateView.updateDate(dateValue.get(Calendar.YEAR),
			      dateValue.get(Calendar.MONTH),
			      dateValue.get(Calendar.DAY_OF_MONTH));

      _isDone = model.Done();
    }
  }

  private ContentValues getValues()
  {
    ContentValues values = new ContentValues();
    Date dueDateValue = new GregorianCalendar(_dueDateView.getYear(),
					      _dueDateView.getMonth(),
					      _dueDateView.getDayOfMonth()).getTime();

    if (!_isNew)
      values.put(DeadlinesContract.DeadlinesColumns.ID, _modelId);

    values.put(DeadlinesContract.DeadlinesColumns.DONE, _isDone ? 1 : 0);
    values.put(DeadlinesContract.DeadlinesColumns.DUE_DATE, dueDateValue.getTime());
    values.put(DeadlinesContract.DeadlinesColumns.GROUP, _groupView.getText().toString());
    values.put(DeadlinesContract.DeadlinesColumns.LABEL, _labelView.getText().toString());

    return values;
  }

  public static EditorDialogFragment newInstance(int id, boolean isNew)
  {
    Bundle args = new Bundle();
    args.putInt(EXTRA_ID, id);
    args.putBoolean(EXTRA_ISNEW, isNew);

    EditorDialogFragment f = new EditorDialogFragment();
    f.setArguments(args);
    return f;
  }
}
