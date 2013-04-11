package com.casimirlab.simpleDeadlines.data;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class DeadlineListLoader extends AsyncTaskLoader<DeadlineAdapter>
{
  private DataHelper _db;
  private int _type;
  private String _group;
  private DeadlineAdapter _result;

  public DeadlineListLoader(Context context)
  {
    super(context);
  }

  public DeadlineListLoader(Context context, int type, String group)
  {
    super(context);

    _db = new DataHelper(context);
    _type = type;
    _group = group;
  }

  @Override
  protected void onStartLoading()
  {
    if (_result != null)
      deliverResult(_result);
    else if (takeContentChanged() || _result == null)
      forceLoad();
  }

  @Override
  protected void onStopLoading()
  {
    super.onStopLoading();

    cancelLoad();
  }

  @Override
  protected void onReset()
  {
    super.onReset();

    onStopLoading();
    _result = null;
  }

  @Override
  public DeadlineAdapter loadInBackground()
  {
    return new DeadlineAdapter(getContext(), _db.deadlines(_type, _group));
  }

  @Override
  public void deliverResult(DeadlineAdapter data)
  {
    _result = data;
    super.deliverResult(data);
  }
}
