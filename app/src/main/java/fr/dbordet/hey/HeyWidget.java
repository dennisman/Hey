package fr.dbordet.hey;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class HeyWidget extends AppWidgetProvider {

    public static final String HEY_ACTION = "heySoundAction";
    public static final String HEY_SERVICE = "heySoundService";
    public static final String DEFAULT_SOUND_NAME = "hey";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hey_widget);
        views.setImageViewResource(R.id.appwidget_btn, R.drawable.ic_widget);
        Intent intent = new Intent(context, HeyWidget.class);
        intent.setAction(HEY_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_btn, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(getClass().getCanonicalName(), "foregroundService");
            context.startForegroundService(new Intent(context, MediaService.class));
        } else {
            context.startService(new Intent(context, MediaService.class));
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        context.stopService(new Intent(context, MediaService.class));
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && HEY_ACTION.equals(intent.getAction())) {
            final Intent serviceIntent = new Intent(context, MediaService.class);
            serviceIntent.setAction(HEY_SERVICE);
            serviceIntent.setData(Uri.parse(DEFAULT_SOUND_NAME));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}

