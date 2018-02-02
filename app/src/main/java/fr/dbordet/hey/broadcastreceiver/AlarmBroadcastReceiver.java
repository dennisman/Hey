package fr.dbordet.hey.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;

import fr.dbordet.hey.helper.InitHelper;

/**
 * Recoit l'intent de l'alarme, met le son a fond, joue le son courant enregistré et remet le niveau du son
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    MediaPlayer mp;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        mp = InitHelper.initMediaPlayer(context);

        final AudioManager audioManager = InitHelper.initAudioManager(context);
        assert audioManager != null;
        final int initVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mp.start();
        Toast.makeText(context, "Alarm....", Toast.LENGTH_LONG).show();

        mp.setOnCompletionListener(new MyOnCompletionListener(audioManager, initVolume));

    }

    private static class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        private final AudioManager audioManager;
        private final int initVolume;

        MyOnCompletionListener(final AudioManager audioManager, final int initVolume) {
            this.audioManager = audioManager;
            this.initVolume = initVolume;
        }

        @Override
        public void onCompletion(final MediaPlayer mp) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, initVolume, 0);
            mp.stop();
            mp.reset();
            mp.release();
        }
    }
}
