package com.casimirlab.simpleDeadlines;

import com.casimirlab.simpleDeadlines.data.DataHelper;
import com.casimirlab.simpleDeadlines.data.GroupAdapter;
import com.casimirlab.simpleDeadlines.data.DeadlineModel;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;

public class DeadlineEditor extends Activity
{
  public static final String TYPE_NEW = "typeNew";
  public static final String MODEL_ID = "modelId";
  private boolean _isNew;
  private int _modelId;
  private DataHelper _db;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.deadline_editor);

    Bundle extras = getIntent().getExtras();
    _isNew = extras.getBoolean(TYPE_NEW, false);
    _modelId = extras.getInt(MODEL_ID, -1);
    _db = new DataHelper(this);

    setTitle(_isNew ? R.string.act_new : R.string.act_edit);
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    if (_modelId >= 0)
    {
      DeadlineModel model = _db.read(_modelId);

      EditText label = (EditText)findViewById(R.id.label);
      label.setText(model.Label());

      AutoCompleteTextView group = (AutoCompleteTextView)findViewById(R.id.group);
      group.setText(model.Group());
      group.setAdapter(new GroupAdapter(this, _db.groups(DataHelper.TYPE_ALL), null));

      DatePicker dueDate = (DatePicker)findViewById(R.id.due_date);
      Calendar dateValue = Calendar.getInstance();
      dateValue.setTime(model.DueDate());
      dueDate.updateDate(dateValue.get(Calendar.YEAR),
			 dateValue.get(Calendar.MONTH),
			 dateValue.get(Calendar.DAY_OF_MONTH));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.deadline_editor, menu);
    menu.findItem(R.id.act_delete).setVisible(!_isNew);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == R.id.act_cancel)
    {
      setResult(RESULT_CANCELED);
      finish();
      return super.onOptionsItemSelected(item);
    }

    DeadlineModel model = makeModel();
    if (model.Label().length() == 0)
    {
      Toast.makeText(this, R.string.empty_label, Toast.LENGTH_SHORT).show();
      return super.onOptionsItemSelected(item);
    }

    if (item.getItemId() == R.id.act_done)
    {
      if (model.Id() != -1)
	_db.update(model);
      else
	_db.create(model);
    }
    else if (item.getItemId() == R.id.act_delete)
      _db.delete(_modelId);

    setResult(RESULT_OK);
    finish();
    return super.onOptionsItemSelected(item);
  }

  private DeadlineModel makeModel()
  {
    EditText label = (EditText)findViewById(R.id.label);
    EditText group = (EditText)findViewById(R.id.group);
    DatePicker dueDate = (DatePicker)findViewById(R.id.due_date);

    DeadlineModel model = new DeadlineModel();

    model.setId(_modelId);
    model.setDone(false);
    model.setLabel(label.getText().toString());
    model.setGroup(group.getText().toString());
    Date dueDateValue = new Date(dueDate.getYear() - 1900, dueDate.getMonth(), dueDate.getDayOfMonth());
    model.setDueDate(dueDateValue);

    return model;
  }
}
