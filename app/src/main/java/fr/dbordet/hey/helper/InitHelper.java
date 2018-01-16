package fr.dbordet.hey.helper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import fr.dbordet.hey.R;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Classe factorisant les initialisation de variables
 */

public class InitHelper {

    /**
     * Identifiant de la notif d'action en Foreground
     */
    private static final int NOTIF_FOREGRD_IDENTIFIER = 1;

    private static final String FEMALE_SOUND_NAME = "hey2";
    private static final String MALE_SOUND_NAME = "hey";

    /**
     * Pas de constructeur pour cette classe Helper
     */
    private InitHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Initialisation de l'audioManager selon la version d'android
     */
    @Nullable
    public static AudioManager initAudioManager(@NonNull final Context context) {
        final AudioManager audioManager;
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
    public static void launchService(@NonNull final Context context, final Intent serviceIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    public static void initNotifForeground(@NonNull final Service context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            final Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("title")
                    .setContentText("text").build();

            context.startForeground(NOTIF_FOREGRD_IDENTIFIER, notification);
        }
    }

    public static MediaPlayer initMediaPlayer(final Context context) {
        return initMediaPlayer(context, PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getBoolean(context.getString(R.string.is_male), true));
    }


    public static MediaPlayer initMediaPlayer(final Context context, final boolean isMale) {
        final String sound;
        if (isMale) {
            sound = MALE_SOUND_NAME;
        } else {
            sound = FEMALE_SOUND_NAME;

        }
        return MediaPlayer.create(context, context.getApplicationContext().getResources().getIdentifier(sound, "raw", context.getPackageName()));
    }
}
