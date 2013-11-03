package com.casimirlab.simpleDeadlines.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.*;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.data.DeadlinesUtils;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class EditorDialogFragment extends DialogFragment implements DatePicker.OnDateChangedListener {
    private static final String TAG = EditorDialogFragment.class.getSimpleName();
    public static final String EXTRA_ID = TAG + ".id";
    public static final String EXTRA_ISNEW = TAG + ".typenew";
    public static final String EXTRA_URI = TAG + ".uri";

    private int _modelId;
    private boolean _isNew;
    private Uri _sourceUri;
    private ContentResolver _cr;
    private Uri _currentUri;
    private DayCounterView _counterView;
    private TextView _labelView;
    private AutoCompleteTextView _groupView;
    private DatePicker _dueDateView;
    private int _state;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        _modelId = args.getInt(EXTRA_ID);
        _isNew = args.getBoolean(EXTRA_ISNEW);
        _sourceUri = args.getParcelable(EXTRA_URI);

        _cr = getActivity().getContentResolver();
        _currentUri = ContentUris.withAppendedId(DeadlinesContract.Deadlines.CONTENT_URI, _modelId);

        _state = _isNew
                ? DeadlinesContract.Deadlines.STATE_NOT_DONE
                : DeadlinesContract.Deadlines.STATE_DONE;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.deadline_editor, null);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean showCalendar = sp.getBoolean(getString(R.string.pref_key_editor_calendar), false);

        _counterView = (DayCounterView) v.findViewById(R.id.day_counter);

        _labelView = (EditText) v.findViewById(R.id.label);

        _groupView = (AutoCompleteTextView) v.findViewById(R.id.group);
        Cursor cGroups = _cr.query(DeadlinesContract.Groups.CONTENT_URI, null, null, null, null);
        _groupView.setAdapter(new GroupAdapter(getActivity(), cGroups));

        Calendar now = Calendar.getInstance();
        _dueDateView = (DatePicker) v.findViewById(R.id.due_date);
        _dueDateView.setSpinnersShown(!showCalendar);
        _dueDateView.setCalendarViewShown(showCalendar);
        _dueDateView.getCalendarView().setFirstDayOfWeek(Calendar.getInstance().getFirstDayOfWeek());
        _dueDateView.init(0, 0, 0, this);
        _dueDateView.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_NEGATIVE) {
                    dialog.cancel();
                    getActivity().finish();
                    return;
                }

                if (TextUtils.isEmpty(_labelView.getText().toString())) {
                    Toast.makeText(getActivity(), R.string.empty_label, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    return;
                }

                if (which == Dialog.BUTTON_POSITIVE) {
                    if (_isNew) {
                        _cr.insert(DeadlinesContract.Deadlines.CONTENT_URI, getValues());
                        if (_sourceUri != null) {
                            String msg = getString(R.string.msg_added, _labelView.getText());
                            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                        }
                    } else
                        _cr.update(_currentUri, getValues(), null, null);
                } else if (which == Dialog.BUTTON_NEUTRAL)
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

        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
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
    public void onStart() {
        super.onStart();

        if (_sourceUri != null || !_isNew) {
            ContentValues values;
            if (_sourceUri != null)
                values = DeadlinesUtils.shareUriToContentValues(_sourceUri);
            else {
                values = new ContentValues();
                Cursor c = _cr.query(_currentUri, null, null, null, null);
                c.moveToFirst();
                DatabaseUtils.cursorRowToContentValues(c, values);
            }

            _labelView.setText(values.getAsString(DeadlinesContract.Deadlines.LABEL));

            _groupView.setText(values.getAsString(DeadlinesContract.Deadlines.GROUP));

            Calendar dateValue = Calendar.getInstance();
            dateValue.setTimeInMillis(values.getAsLong(DeadlinesContract.Deadlines.DUE_DATE));
            _dueDateView.updateDate(dateValue.get(Calendar.YEAR),
                    dateValue.get(Calendar.MONTH),
                    dateValue.get(Calendar.DAY_OF_MONTH));

            _state = values.getAsInteger(DeadlinesContract.Deadlines.DONE);
        }
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar date = new GregorianCalendar(
                year,
                monthOfYear,
                dayOfMonth);

        _counterView.setDate(date.getTimeInMillis());
    }

    private ContentValues getValues() {
        ContentValues values = new ContentValues();
        Date dueDateValue = new GregorianCalendar(
                _dueDateView.getYear(),
                _dueDateView.getMonth(),
                _dueDateView.getDayOfMonth()).getTime();

        if (!_isNew)
            values.put(DeadlinesContract.Deadlines.ID, _modelId);

        values.put(DeadlinesContract.Deadlines.DONE, _state);
        values.put(DeadlinesContract.Deadlines.DUE_DATE, dueDateValue.getTime());
        values.put(DeadlinesContract.Deadlines.GROUP, _groupView.getText().toString());
        values.put(DeadlinesContract.Deadlines.LABEL, _labelView.getText().toString());

        return values;
    }

    public static EditorDialogFragment newInstance(int id, boolean isNew) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_ID, id);
        args.putBoolean(EXTRA_ISNEW, isNew);

        EditorDialogFragment f = new EditorDialogFragment();
        f.setArguments(args);
        return f;
    }

    public static EditorDialogFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ISNEW, true);
        args.putParcelable(EXTRA_URI, uri);

        EditorDialogFragment f = new EditorDialogFragment();
        f.setArguments(args);
        return f;
    }
}
