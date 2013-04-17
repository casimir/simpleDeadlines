package com.casimirlab.simpleDeadlines;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.casimirlab.simpleDeadlines.data.DataHelper;
import com.casimirlab.simpleDeadlines.data.DeadlineModel;
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
  private DataHelper _db;
  private TextView _labelView;
  private AutoCompleteTextView _groupView;
  private DatePicker _dueDateView;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    _modelId = args.getInt(EXTRA_ID, -1);
    _isNew = args.getBoolean(EXTRA_ISNEW);
    _db = new DataHelper(getActivity());
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
	  getActivity().setResult(Activity.RESULT_CANCELED);
	  getActivity().finish();
	  return;
	}

	DeadlineModel model = makeModel();
	if (model.Label().length() == 0)
	{
	  Toast.makeText(getActivity(), R.string.empty_label, Toast.LENGTH_SHORT).show();
	  getActivity().setResult(Activity.RESULT_CANCELED);
	  getActivity().finish();
	  return;
	}

	if (which == Dialog.BUTTON_POSITIVE)
	{
	  if (model.Id() != -1)
	    _db.update(model);
	  else
	    _db.create(model);
	}
	else if (which == Dialog.BUTTON_NEUTRAL)
	  _db.delete(_modelId);

	dialog.dismiss();
	getActivity().setResult(Activity.RESULT_OK);
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
	{
	  getActivity().setResult(Activity.RESULT_CANCELED);
	  getActivity().finish();
	}
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

    if (_modelId >= 0)
    {
      DeadlineModel model = _db.read(_modelId);

      _labelView.setText(model.Label());

      _groupView.setText(model.Group());
      _groupView.setAdapter(new GroupAdapter(getActivity(), _db.groups(DataHelper.TYPE_ALL)));

      Calendar dateValue = Calendar.getInstance();
      dateValue.setTime(model.DueDate());
      _dueDateView.updateDate(dateValue.get(Calendar.YEAR),
			      dateValue.get(Calendar.MONTH),
			      dateValue.get(Calendar.DAY_OF_MONTH));
    }
  }

  private DeadlineModel makeModel()
  {
    DeadlineModel model = new DeadlineModel();
    model.setId(_modelId);
    model.setDone(false);
    model.setLabel(_labelView.getText().toString());
    model.setGroup(_groupView.getText().toString());
    Date dueDateValue = new GregorianCalendar(_dueDateView.getYear(),
					      _dueDateView.getMonth(),
					      _dueDateView.getDayOfMonth()).getTime();
    model.setDueDate(dueDateValue);

    return model;
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
