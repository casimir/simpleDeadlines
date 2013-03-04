package com.casimirlab.simpleDeadlines;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class DrawerLayout extends FrameLayout
{
  private static final float BEVEL_WIDTH = 25.0f;
  private static final int SCROLL_DURATION = 400;
  private static final int SHADOW_WIDTH = 8;
  private boolean _isDrawerAdded;
  private boolean _isInGesture;
  private boolean _isMoving;
  private boolean _isOpen;
  /* Graphics */
  private ViewGroup _contentGroup;
  private ViewGroup _decorContentGroup;
  private ViewGroup _decorRootGroup;
  private ViewGroup _drawerContent;
  private ViewGroup _rootGroup;
  private Drawable _shadow;
  /* Moves */
  private int _currentX;
  private int _currentY;
  private int _drawerWidth;
  private int _minWidth;
  private int _offset;
  private Scroller _scroller;
  private int _startX;
  private int _startY;
  private ViewConfiguration _vc;
  private VelocityTracker _vt;
  /* Threads */
  private Callback _callback;
  private Handler _scrollerHandler;
  private Runnable _closeRunnable;
  private Runnable _openRunnable;

  public DrawerLayout(Activity act, int drawerLayout)
  {
    super(act);

    _rootGroup = (ViewGroup)act.getWindow().getDecorView();
    _contentGroup = (ViewGroup)_rootGroup.findViewById(android.R.id.content);
    _drawerContent = (ViewGroup)LayoutInflater.from(act).inflate(drawerLayout, null);
    _drawerContent.setVisibility(INVISIBLE);
    int[] colors = new int[]
    {
      Color.parseColor("#00000000"), Color.parseColor("#FF000000")
    };
    _shadow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);

    DisplayMetrics dm = act.getResources().getDisplayMetrics();
    float wBevel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BEVEL_WIDTH, dm);
    _minWidth = Math.round(wBevel);
    _vc = ViewConfiguration.get(getContext());
    _vt = VelocityTracker.obtain();

    _callback = null;
    _scrollerHandler = new Handler();
    _scroller = new Scroller(act, new Interpolator()
    {
      @Override
      public float getInterpolation(float input)
      {
	return (float)Math.pow(input - 1.0, 5.0) + 1.0f;
      }
    });

    updateHierarchy();
  }

  @Override
  protected void dispatchDraw(Canvas canvas)
  {
    super.dispatchDraw(canvas);

    if (_isOpen || _isMoving)
    {
      canvas.save();
      canvas.translate(_offset, 0);
      _shadow.draw(canvas);
      canvas.restore();
    }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev)
  {
    switch (ev.getAction())
    {
      case MotionEvent.ACTION_DOWN:
	_startX = (int)ev.getX();
	_startY = (int)ev.getY();
	_currentX = _startX;
	_currentY = _startY;
	_isInGesture = (_startX < _minWidth && !_isOpen)
		       || (_startX > _drawerWidth && _isOpen);
	return false;
      case MotionEvent.ACTION_MOVE:
	if (!_isInGesture)
	  return false;
	if (!_isOpen && (ev.getX() < _currentX || ev.getX() < _startX))
	{
	  _isInGesture = false;
	  return false;
	}

	_currentX = (int)ev.getX();
	_currentY = (int)ev.getY();

	double abs = Math.hypot(_currentX - _startX, _currentY - _startY);
	return abs >= _vc.getScaledTouchSlop();
      case MotionEvent.ACTION_UP:
	if (_startX > _drawerWidth && _isOpen)
	  close();

	_isInGesture = false;
	_startX = -1;
	_startY = -1;
	_currentX = -1;
	_currentY = -1;
	return false;
    }
    return false;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom)
  {
    Rect window = new Rect();
    _rootGroup.getWindowVisibleDisplayFrame(window);

    _drawerContent.layout(left, 0,
			  right, bottom);
    _decorContentGroup.layout(_decorContentGroup.getLeft(), 0,
			      _decorContentGroup.getLeft() + right, bottom);

    _drawerWidth = _drawerContent.getMeasuredWidth();
    if (_drawerWidth > right - _minWidth)
    {
      _drawerContent.setPadding(0, 0, _minWidth, 0);
      _drawerWidth -= _minWidth;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    int widthPixels = getResources().getDisplayMetrics().widthPixels;
    int step = (int)(event.getX()) - _currentX;
    _currentX = (int)event.getX();
    _currentY = (int)event.getY();

    _vt.addMovement(event);
    switch (event.getAction())
    {
      case MotionEvent.ACTION_MOVE:
	_drawerContent.setVisibility(VISIBLE);
	_isMoving = true;
	if (_offset + step > _drawerWidth)
	{
	  _isOpen = true;
	  _decorContentGroup.offsetLeftAndRight(_drawerWidth - _offset);
	  _offset = _drawerWidth;
	}
	else if (_offset + step < 0 && _offset != 0)
	{
	  _isOpen = false;
	  _decorContentGroup.offsetLeftAndRight(0 - _decorContentGroup.getLeft());
	  _offset = 0;
	}
	else
	{
	  _decorContentGroup.offsetLeftAndRight(step);
	  _offset += step;
	}
	invalidate();
	return true;
      case MotionEvent.ACTION_UP:
	_isInGesture = false;
	_isMoving = false;
	_vt.computeCurrentVelocity(1000);
	if (Math.abs(_vt.getXVelocity()) > _vc.getScaledMinimumFlingVelocity())
	{
	  _isOpen = _vt.getXVelocity() <= 0;
	  toggle();
	}
	else
	{
	  _isOpen = _offset < (_drawerWidth / 2);
	  toggle();
	}
	return true;
    }
    return false;
  }

  /**
   * Closes drawer.
   */
  public void close()
  {
    if (!_isOpen)
      return;

    if (_isMoving)
    {
      _scrollerHandler.removeCallbacks(_openRunnable);
      _scrollerHandler.removeCallbacks(_closeRunnable);
    }
    _isMoving = true;
    _scroller.startScroll(_offset, 0, -_offset, 0, SCROLL_DURATION);

    _closeRunnable = new Runnable()
    {
      @Override
      public void run()
      {
	final boolean scrolling = _scroller.computeScrollOffset();
	_decorContentGroup.offsetLeftAndRight(_scroller.getCurrX() - _offset);
	_offset = _scroller.getCurrX();
	postInvalidate();

	if (!scrolling)
	{
	  _drawerContent.setVisibility(INVISIBLE);
	  _isMoving = false;
	  _isOpen = false;
	  _callback.close();
	}
	else
	  _scrollerHandler.post(this);
      }
    };
    _scrollerHandler.post(_closeRunnable);
  }

  /**
   * Returns the ViewGroup of drawer content.
   *
   * @return Drawer content.
   */
  public ViewGroup getContent()
  {
    return _drawerContent;
  }

  /**
   * True if the drawer is open false if it is closed.
   *
   * @return
   */
  public boolean isOpen()
  {
    return _isOpen;
  }

  /**
   * Opens drawer.
   */
  public void open()
  {
    if (_isOpen)
      return;

    if (_isMoving)
    {
      _scrollerHandler.removeCallbacks(_openRunnable);
      _scrollerHandler.removeCallbacks(_closeRunnable);
    }
    _drawerContent.setVisibility(VISIBLE);
    _isMoving = true;

    final int widthPixels = getResources().getDisplayMetrics().widthPixels;
    if (_drawerWidth > widthPixels - _minWidth)
      _scroller.startScroll(_offset, 0, (widthPixels - _minWidth) - _offset, 0, SCROLL_DURATION);
    else
      _scroller.startScroll(_offset, 0, _drawerWidth - _offset, 0, SCROLL_DURATION);

    _openRunnable = new Runnable()
    {
      @Override
      public void run()
      {
	final boolean scrolling = _scroller.computeScrollOffset();
	_decorContentGroup.offsetLeftAndRight(_scroller.getCurrX() - _offset);
	_offset = _scroller.getCurrX();
	postInvalidate();

	if (!scrolling)
	{
	  _isMoving = false;
	  _isOpen = true;
	  _callback.open();
	}
	else
	  _scrollerHandler.post(this);
      }
    };
    _scrollerHandler.post(_openRunnable);
  }

  /**
   * Updates the window hierarchy. Add or delete the view of the drawer in the
   * window root.
   */
  public void updateHierarchy()
  {
    final DisplayMetrics dm = getResources().getDisplayMetrics();

    if (_drawerContent != null)
      removeView(_drawerContent);
    if (_decorContentGroup != null)
    {
      removeView(_decorContentGroup);
      _decorRootGroup.addView(_decorContentGroup);
      _decorContentGroup.setOnClickListener(null);
      _decorContentGroup.setBackgroundColor(Color.TRANSPARENT);
    }
    if (_isDrawerAdded)
      _decorRootGroup.removeView(this);

    _decorContentGroup = _contentGroup;
    _decorRootGroup = (ViewGroup)_contentGroup.getParent();
    _decorRootGroup.removeView(_decorContentGroup);
    addView(_drawerContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
    addView(_decorContentGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    _decorRootGroup.addView(this);
    _isDrawerAdded = true;

    _shadow.setBounds(-SHADOW_WIDTH, 0, 0, dm.heightPixels);
    _decorContentGroup.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
      }
    });
  }

  public void setCallback(Callback callback)
  {
    _callback = callback;
  }

  public void setMaxWidth(int width)
  {
    _drawerContent.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));
  }

  /**
   * Toggles drawer.
   */
  public void toggle()
  {
    if (!_isOpen)
      open();
    else
      close();

    _callback.toggle(_isOpen);
  }

  public static class Callback
  {
    public void toggle(boolean opening)
    {
    }

    public void open()
    {
    }

    public void close()
    {
    }
  }
}