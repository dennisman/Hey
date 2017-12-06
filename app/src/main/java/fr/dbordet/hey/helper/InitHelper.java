package fr.dbordet.hey.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Classe factorisant les initialisation de variables
 */

public class InitHelper {

    /**
     * Identifiant de la notif d'action en Foreground
     */
    private static final int NOTIF_FOREGRD_IDENTIFIER = 1;
    /**
     * Pas de constructeur pour cette classe Helper
     */
    private InitHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Initialisation de l'audioManager selon la version d'android
     */
    public static AudioManager initAudioManager(Context context) {
        AudioManager audioManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = context.getSystemService(AudioManager.class);
        } else {
            audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        }
        return audioManager;
    }

    /**
     * Lancement d'un service selon la version d'android
     */
    public static void launchService(Context context, Intent serviceIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void initNotifForeground(Service context) {
        if (Build.VERSION.SDK_INT >= 26) {
            final String CHANNEL_ID = "my_channel_01";
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);
            channel.enableLights(false);
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("title")
                    .setContentText("text").build();

            context.startForeground(NOTIF_FOREGRD_IDENTIFIER, notification);
        }
    }
}
