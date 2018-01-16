package fr.dbordet.hey.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import fr.dbordet.hey.R;
import fr.dbordet.hey.helper.InitHelper;
import fr.dbordet.hey.service.MediaService;

/**
 * Implementation of App Widget functionality.
 */
public class HeyWidget extends AppWidgetProvider {

    public static final String HEY_SERVICE = "heySoundService";
    private static final String HEY_ACTION = "heySoundAction";
    private static final String DEFAULT_SOUND_NAME = "hey";
    private static final String FEMALE_SOUND_NAME = "hey2";


    private static void updateAppWidget(@NonNull final Context context, @NonNull final AppWidgetManager appWidgetManager,
                                        final int appWidgetId) {

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hey_widget);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            views.setImageViewResource(R.id.appwidget_btn, R.drawable.ic_widget);
        } else {
            views.setImageViewResource(R.id.appwidget_btn, R.mipmap.ic_launcher_red);
        }
        final Intent intent = new Intent(context, HeyWidget.class);
        intent.setAction(HEY_ACTION);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_btn, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(@NonNull final Context context, @NonNull final AppWidgetManager appWidgetManager, @NonNull final int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (final int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDisabled(@NonNull final Context context) {
        // Enter relevant functionality for when the last widget is disabled
        context.stopService(new Intent(context, MediaService.class));

        super.onDisabled(context);
    }

    @Override
    public void onReceive(@NonNull final Context context, @Nullable final Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && HEY_ACTION.equals(intent.getAction())) {
            final Intent serviceIntent = new Intent(context, MediaService.class);
            serviceIntent.setAction(HEY_SERVICE);
            serviceIntent.setData(Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_male", true) ? DEFAULT_SOUND_NAME : FEMALE_SOUND_NAME));
            InitHelper.launchService(context, serviceIntent);
        }
    }
}

