package fr.dbordet.hey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import static fr.dbordet.hey.HeyWidget.HEY_SERVICE;

public class MainActivity extends AppCompatActivity implements DialogSoundManagerOwner {
    /**
     * Identifiant de ton téléphone.
     * Utile pour empecher d'avoir des pubs et fausser les résultats publicitaires.
     * <p>
     * "Testing with real ads (even if you never tap on them) is against AdMob policy and can cause your account to be suspended".
     * https://developers.google.com/admob/android/banner?hl=fr
     */
    private static final String MY_OP3T_TESTDEVICE_ID = "5E82255F8F587F5AF93BC5DF8A99810E";

    /**
     * Identifiant adMob de l'application, différent de l'id adMob de la adView
     */
    private static final String MY_APP_ADS_ID = "ca-app-pub-4011387854346003~9386990030";

    /**
     * Toast son off
     */
    private Toast toast;

    /**
     * SnackBar son off + action sound max
     */
    private Snackbar snackbar;

    /**
     * Vue de la pub
     */
    private AdView adView;
    /**
     * Listener de la pub. cache la pub au retour sur l'appli.
     * Enregistre dans les sharedPref que l'utilisateur a deja cliqué à l'ouverture de la pub
     */
    private final AdListener adListener = new AdListener() {

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            adView.setVisibility(View.GONE);
        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();
            final SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.is_user_ad_clicker), true);
            editor.apply();
            //register in DB
        }
    };
    /**
     * Gere le son
     */
    private AudioManager audioManager;
    private final View.OnClickListener soundHalfMaxListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            upSound();
        }
    };

    private DialogSoundManagerFragment dialogSoundManager;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent createIntent = getIntent();
        if (createIntent != null && createIntent.getData() != null) { // appShortcut par exemple
            final Intent serviceIntent = new Intent(this, MediaService.class);
            serviceIntent.setAction(HEY_SERVICE);
            serviceIntent.setData(createIntent.getData());
            this.startService(serviceIntent);
            finish();
        }

        setContentView(R.layout.activity_main);

        init();

        // initialize the Mobile Ads SDK
        MobileAds.initialize(this, MY_APP_ADS_ID);
        // Load an ad into the AdMob banner view.
        //https://developer.android.com/training/data-storage/shared-preferences.html
        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final boolean alreadyClickOnAd = sharedPref.getBoolean(getString(R.string.is_user_ad_clicker), false);
        adView = findViewById(R.id.adView);
        if (!alreadyClickOnAd) {
            AdRequest adRequest = new AdRequest.Builder()
                    .setRequestAgent("android_studio:ad_template")
                    .addTestDevice(MY_OP3T_TESTDEVICE_ID)
                    .build();
            adView.loadAd(adRequest);
            adView.setAdListener(adListener);
        } else {
            adView.setVisibility(View.GONE);
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.hey);
        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
//                    snackbar.show();
//                    toast.show();
                    dialogSoundManager.show(getFragmentManager(), DialogSoundManagerFragment.class.getName());
//                    alertDialogBuilder.create().show();
                } else {
                    play();
                }
            }
        });
    }

    /**
     * initialisation des différents composants
     */
    private void init() {
        initAudioManager();
        initToast();
        initSnackbar();
        initAlertDialogBuilder();
    }

    private void initAlertDialogBuilder() {
        dialogSoundManager = new DialogSoundManagerFragment();
    }

    private void initSnackbar() {
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.youShouldActivateSound,
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.upSound, soundHalfMaxListener);

    }

    /**
     * initialise le toast
     */
    private void initToast() {
        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.youShouldActivateSound), Toast.LENGTH_SHORT);
    }

    /**
     * Initialise l'attribut audioManager selon la version d'android
     */
    public void initAudioManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = getApplicationContext().getSystemService(AudioManager.class);
        } else {
            audioManager = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
        }
    }

    private void upSound() {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
    }

    @Override
    public void play() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
        }
        mediaPlayer.start();
    }
}