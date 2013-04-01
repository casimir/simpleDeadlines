package com.casimirlab.simpleDeadlines;

import android.app.Activity;
import android.os.Bundle;

public class DeadlineEditor extends Activity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    int id = extras.getInt(EditorDialogFragment.EXTRA_ID, -1);
    boolean isNew = extras.getBoolean(EditorDialogFragment.EXTRA_ISNEW, false);

    EditorDialogFragment dialog = EditorDialogFragment.newInstance(id, isNew);
    dialog.show(getFragmentManager(), "EditorDialog");
  }
}
