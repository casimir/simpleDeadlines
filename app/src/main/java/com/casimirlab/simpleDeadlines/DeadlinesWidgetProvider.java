package com.casimirlab.simpleDeadlines;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import com.casimirlab.simpleDeadlines.provider.DeadlinesContract;
import com.casimirlab.simpleDeadlines.ui.DeadlineEditor;
import com.casimirlab.simpleDeadlines.ui.EditorDialogFragment;
import com.casimirlab.simpleDeadlines.ui.MainActivity;

public class DeadlinesWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Cursor c = context.getContentResolver().query(DeadlinesContract.Count.CONTENT_URI, null, null, null, null);
        if (c == null || !c.moveToFirst())
            return;

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_horizontal);
        views.setTextViewText(R.id.count_today, String.valueOf(c.getInt(0)));
        views.setTextViewText(R.id.count_urgent, String.valueOf(c.getInt(1)));
        views.setTextViewText(R.id.count_worrying, String.valueOf(c.getInt(2)));
        views.setTextViewText(R.id.count_nice, String.valueOf(c.getInt(3)));

        Intent intentLaunch = new Intent(context, MainActivity.class);
        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingLaunch = PendingIntent.getActivity(context, 0, intentLaunch, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.content, pendingLaunch);

        Intent intentNew = new Intent(context, DeadlineEditor.class);
        intentNew.putExtra(EditorDialogFragment.EXTRA_ISNEW, true);
        PendingIntent pendingNew = PendingIntent.getActivity(context, 0, intentNew, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.act_new, pendingNew);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }
}
