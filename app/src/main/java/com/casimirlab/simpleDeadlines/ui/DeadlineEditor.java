package com.casimirlab.simpleDeadlines.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

public class DeadlineEditor extends Activity
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    EditorDialogFragment dialog;

    Uri uri = getIntent().getData();
    if (uri != null)
      dialog = EditorDialogFragment.newInstance(uri);
    else
    {
      Bundle extras = getIntent().getExtras();
      int id = extras == null ? -1 : extras.getInt(EditorDialogFragment.EXTRA_ID, -1);
      boolean isNew = extras != null && extras.getBoolean(EditorDialogFragment.EXTRA_ISNEW, false);
      dialog = EditorDialogFragment.newInstance(id, isNew);
    }

    dialog.show(getFragmentManager(), "EditorDialog");
  }
}
