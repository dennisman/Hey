package fr.dbordet.hey.helper;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Classe factorisant les initialisation de variables
 */

public class InitHelper {

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
}
