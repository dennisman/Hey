package fr.dbordet.hey;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MediaService extends Service {
    private MediaPlayer mediaPlayer;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mediaPlayer = MediaPlayer.create(this, R.raw.hey);
        this.mediaPlayer.setOnPreparedListener(new CustomOnPreparedListener(this));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((intent == null || intent.getAction() == null || !HeyWidget.HEY_SERVICE.equals(intent.getAction()))) {
            return Service.START_STICKY_COMPATIBILITY;
        }
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.seekTo(0);
        }
        this.mediaPlayer.start();
        return Service.START_STICKY_COMPATIBILITY;
    }

    private class CustomOnPreparedListener implements MediaPlayer.OnPreparedListener {
        final MediaService mediaService;

        public CustomOnPreparedListener(MediaService mediaService) {
            this.mediaService = mediaService;
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    }
}
