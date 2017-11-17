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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((intent == null || intent.getAction() == null || !HeyWidget.HEY_SERVICE.equals(intent.getAction()))) {
            return Service.START_STICKY_COMPATIBILITY;
        }
        if (intent.getDataString() != null) {
            mediaPlayer.stop();
            this.mediaPlayer = MediaPlayer.create(this, getApplicationContext().getResources().getIdentifier(intent.getDataString(), "raw", getPackageName()));
        }
        if (this.mediaPlayer.isPlaying()) {
            this.mediaPlayer.seekTo(0);
        }
        this.mediaPlayer.start();
        return Service.START_STICKY_COMPATIBILITY;
    }
}
