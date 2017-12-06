package fr.dbordet.hey.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import fr.dbordet.hey.R;
import fr.dbordet.hey.helper.InitHelper;
import fr.dbordet.hey.widget.HeyWidget;

public class MediaService extends Service {
    private MediaPlayer mediaPlayer;
    private String audioFile;
    private Handler handler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d(this.toString(), "KILL");
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            MediaService.this.stopSelf();
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        this.mediaPlayer = MediaPlayer.create(this, R.raw.hey);
        InitHelper.initNotifForeground(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((intent == null || intent.getAction() == null || !HeyWidget.HEY_SERVICE.equals(intent.getAction()))) {
            return Service.START_STICKY_COMPATIBILITY;
        }
        killIfUnused();
        final String localAudioFile = intent.getDataString();
        if (localAudioFile != null && !localAudioFile.equals(this.audioFile)) {
            this.audioFile = localAudioFile;
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            this.mediaPlayer = MediaPlayer.create(this, getApplicationContext().getResources().getIdentifier(localAudioFile, "raw", getPackageName()));
        }
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.seekTo(0);
        }
        this.mediaPlayer.start();
        return Service.START_STICKY_COMPATIBILITY;
    }

    private void killIfUnused() {
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 4 * 1000);
    }
}
