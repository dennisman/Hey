package fr.dbordet.hey;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    /**
     * Identifiant de ton téléphone.
     * Utile pour empecher d'avoir des pubs et fausser les résultats publicitaires.
     * <p>
     * "Testing with real ads (even if you never tap on them) is against AdMob policy and can cause your account to be suspended".
     * https://developers.google.com/admob/android/banner?hl=fr
     */
    private static final String MY_OP3T_TESTDEVICE_ID = "44572ECFAADF7A1CC5E67FB1FB0CA747";

    /**
     * Identifiant adMob de l'application, différent de l'id adMob de la adView
     */
    private static final String MY_APP_ADS_ID = "ca-app-pub-4011387854346003~9386990030";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the Mobile Ads SDK
        MobileAds.initialize(this, MY_APP_ADS_ID);
        // Load an ad into the AdMob banner view.
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template")
                .addTestDevice(MY_OP3T_TESTDEVICE_ID)
                .build();
        adView.loadAd(adRequest);
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.hey);
        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(0);
                }
                mediaPlayer.start();
            }
        });
    }

}