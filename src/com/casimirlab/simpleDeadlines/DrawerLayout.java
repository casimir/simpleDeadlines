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
  private static final int SHADOW_COLOR = Color.DKGRAY;
  private static final int SHADOW_WIDTH = 8;
  private boolean _isInGesture;
  private boolean _isMoving;
  private boolean _isOpened;
  /* Graphics */
  private ViewGroup _contentView;
  private ViewGroup _rootView;
  private ViewGroup _drawerContentView;
  private Drawable _shadow;
  /* Moves */
  private Callback _callback;
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
  private Runnable _closeRunnable;
  private Runnable _openRunnable;
  private Handler _scrollerHandler;

  public DrawerLayout(Activity act, int drawerLayout)
  {
    super(act);

    ViewGroup decorView = (ViewGroup)act.getWindow().getDecorView();
    ViewGroup contentView = (ViewGroup)decorView.findViewById(android.R.id.content);
    _rootView = (ViewGroup)contentView.getParent();
    _contentView = contentView;
    _contentView.setBackgroundDrawable(decorView.getBackground());
    _contentView.setOnClickListener(new OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
      }
    });
    _drawerContentView = (ViewGroup)LayoutInflater.from(act).inflate(drawerLayout, null);
    _drawerContentView.setVisibility(INVISIBLE);
    int contentIdx = _rootView.indexOfChild(contentView);

    int[] colors = new int[]
    {
      SHADOW_COLOR, SHADOW_COLOR & 0x00FFFFFF
    };
    final DisplayMetrics dm = act.getResources().getDisplayMetrics();
    _shadow = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
    _shadow.setBounds(-SHADOW_WIDTH, 0, 0, dm.heightPixels);

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

    _rootView.removeViewAt(contentIdx);
    addView(_drawerContentView,
	    new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    addView(_contentView,
	    new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    _rootView.addView(this, contentIdx);
  }

  @Override
  protected void dispatchDraw(Canvas canvas)
  {
    super.dispatchDraw(canvas);

    if (_isOpened || _isMoving)
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
	_isInGesture = (_startX < _minWidth && !_isOpened)
		       || (_startX > _drawerWidth && _isOpened);
	return false;
      case MotionEvent.ACTION_MOVE:
	if (!_isInGesture)
	  return false;
	if (!_isOpened && (ev.getX() < _currentX || ev.getX() < _startX))
	{
	  _isInGesture = false;
	  return false;
	}

	_currentX = (int)ev.getX();
	_currentY = (int)ev.getY();

	double abs = Math.hypot(_currentX - _startX, _currentY - _startY);
	return abs >= _vc.getScaledTouchSlop();
      case MotionEvent.ACTION_UP:
	if (_startX > _drawerWidth && _isOpened)
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
    _rootView.getWindowVisibleDisplayFrame(window);

    _drawerContentView.layout(left, 0, right, bottom);
    _contentView.layout(_contentView.getLeft(), _contentView.getTop(),
			_contentView.getLeft() + right, bottom);

    _drawerWidth = _drawerContentView.getMeasuredWidth();
    if (_drawerWidth > right - _minWidth)
    {
      _drawerContentView.setPadding(0, 0, _minWidth, 0);
      _drawerWidth -= _minWidth;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    int step = (int)(event.getX()) - _currentX;
    _currentX = (int)event.getX();
    _currentY = (int)event.getY();

    _vt.addMovement(event);
    switch (event.getAction())
    {
      case MotionEvent.ACTION_MOVE:
	_drawerContentView.setVisibility(VISIBLE);
	_isMoving = true;
	if (_offset + step > _drawerWidth)
	{
	  _isOpened = true;
	  _contentView.offsetLeftAndRight(_drawerWidth - _offset);
	  _offset = _drawerWidth;
	}
	else if (_offset + step < 0 && _offset != 0)
	{
	  _isOpened = false;
	  _contentView.offsetLeftAndRight(0 - _contentView.getLeft());
	  _offset = 0;
	}
	else
	{
	  _contentView.offsetLeftAndRight(step);
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
	  _isOpened = _vt.getXVelocity() <= 0;
	  toggle();
	}
	else
	{
	  _isOpened = _offset < (_drawerWidth / 2);
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
    if (!_isOpened)
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
	_contentView.offsetLeftAndRight(_scroller.getCurrX() - _offset);
	_offset = _scroller.getCurrX();
	postInvalidate();

	if (!scrolling)
	{
	  _drawerContentView.setVisibility(INVISIBLE);
	  _isMoving = false;
	  _isOpened = false;

	  if (_callback != null)
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
    return _drawerContentView;
  }

  /**
   * True if the drawer is opened false if it is closed.
   *
   * @return The drawer state.
   */
  public boolean isOpened()
  {
    return _isOpened;
  }

  /**
   * Opens drawer.
   */
  public void open()
  {
    if (_isOpened)
      return;

    if (_isMoving)
    {
      _scrollerHandler.removeCallbacks(_openRunnable);
      _scrollerHandler.removeCallbacks(_closeRunnable);
    }
    _drawerContentView.setVisibility(VISIBLE);
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
	_contentView.offsetLeftAndRight(_scroller.getCurrX() - _offset);
	_offset = _scroller.getCurrX();
	postInvalidate();

	if (!scrolling)
	{
	  _isMoving = false;
	  _isOpened = true;

	  if (_callback != null)
	    _callback.open();
	}
	else
	  _scrollerHandler.post(this);
      }
    };
    _scrollerHandler.post(_openRunnable);
  }

  public void setCallback(Callback callback)
  {
    _callback = callback;
  }

  public void setMaxWidth(int width)
  {
    _drawerContentView.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));
  }

  /**
   * Toggles drawer.
   */
  public void toggle()
  {
    if (!_isOpened)
      open();
    else
      close();

    if (_callback != null)
      _callback.toggle(_isOpened);
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