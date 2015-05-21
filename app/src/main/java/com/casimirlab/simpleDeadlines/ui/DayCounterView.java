package com.casimirlab.simpleDeadlines.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.casimirlab.simpleDeadlines.R;
import com.casimirlab.simpleDeadlines.data.DeadlinesUtils;

/**
 * Provide a simple view to display a count of days.
 */
public class DayCounterView extends FrameLayout {
    private FrameLayout _backgroundLayout;
    private TextView _countView;
    private boolean _automaticBackground;

    public DayCounterView(Context context) {
        this(context, null);
    }

    public DayCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.day_counter, this, true);

        _backgroundLayout = (FrameLayout) getChildAt(0);
        _countView = (TextView) _backgroundLayout.getChildAt(0);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DayCounter);
        try {
            _automaticBackground = a.getBoolean(R.styleable.DayCounter_automaticBackground, true);
            int bg = a.getInt(R.styleable.DayCounter_backgroundColor, -1);
            if (bg != -1)
                _backgroundLayout.setBackgroundColor(bg);

            int lvl = a.getInt(R.styleable.DayCounter_level, 0);
            if (lvl != 0)
                setLevel(lvl);
        } finally {
            a.recycle();
        }
    }

    /**
     * Change counter's background mode. When not in automatic mode, counter's background stay the same color.
     * This method has to be called before {@link #setDate(long)}. Default value is true.
     *
     * @param automatic True to active automatic mode.
     */
    public void setAutomaticBackground(boolean automatic) {
        _automaticBackground = automatic;
    }

    /**
     * Manually set counter value.
     *
     * @param count Value to set.
     */
    public void setCount(int count) {
        _countView.setText(String.valueOf(count));
        if (_automaticBackground)
            setLevel(DeadlinesUtils.dayCountToLvl(count));
    }

    /**
     * Set the date to compute the difference of days.
     *
     * @param time Date as long.
     */
    public void setDate(long time) {
        setCount(DeadlinesUtils.timeToDayCount(time));
    }

    /**
     * Set the background of the view. The color is defined from a level value.
     *
     * @param lvl The level to set.
     */
    public void setLevel(int lvl) {
        _backgroundLayout.setBackgroundResource(DeadlinesUtils.LVL_ALL.get(lvl));
    }
}
