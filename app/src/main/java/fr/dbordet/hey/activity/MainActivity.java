package fr.dbordet.hey.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import fr.dbordet.hey.R;
import fr.dbordet.hey.fragment.DialogSoundManagerFragment;
import fr.dbordet.hey.helper.InitHelper;
import fr.dbordet.hey.service.MediaService;

import static fr.dbordet.hey.widget.HeyWidget.HEY_SERVICE;

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
     * Gere le son
     */
    @Nullable
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private int noSoundActionNumber;
    private boolean isDialogLoaded = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent createIntent = getIntent();
        if (createIntent != null && createIntent.getData() != null) { // appShortcut par exemple
            final Intent serviceIntent = new Intent(this, MediaService.class);
            serviceIntent.setAction(HEY_SERVICE);
            serviceIntent.setData(createIntent.getData());
            this.startService(serviceIntent);
            finish();
        }

        setContentView(R.layout.activity_main);

        init();
        mediaPlayer = MediaPlayer.create(this, R.raw.hey);
        noSoundActionNumber = 0;
        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
                    handleNoSound();
                } else {
                    play();
                }
            }
        });
    }

    /**
     * Gere l'action a effectué lorsque le son est désactivé et que l'utilisateur souhaite appuyer sur
     * le bouton hey de l'activité
     */
    private void handleNoSound() {
        final int action = noSoundActionNumber % 3;
        switch (action) {
            case 0:
                toast.show();
                break;
            case 1:
                toast.cancel();
                snackbar.show();
                break;
            case 2:
            default:
                snackbar.dismiss();
                if (!isDialogLoaded) {
                    new DialogSoundManagerFragment().show(getFragmentManager(), DialogSoundManagerFragment.class.getName());
                }
                isDialogLoaded = true;
                break;

        }
        noSoundActionNumber++;
    }

    /**
     * initialisation des différents composants
     */
    private void init() {
        audioManager = InitHelper.initAudioManager(this.getApplicationContext());
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || !this.getSystemService(ActivityManager.class).isLowRamDevice()) {
            initAdView();
        }
        initToast();
        initSnackbar();
    }

    private void initAdView() {
        /*
         * Vue de la pub
         */
        final AdView adView = findViewById(R.id.adView);
                /*
         * Listener de la pub. cache la pub au retour sur l'appli.
         * Enregistre dans les sharedPref que l'utilisateur a deja cliqué à l'ouverture de la pub
         */
        final AdListener adListener = new AdListener() {

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                final SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.is_user_ad_clicker), true);
                editor.apply();
                //register in DB
            }
        };

        // initialize the Mobile Ads SDK
        MobileAds.initialize(this, MY_APP_ADS_ID);
        // Load an ad into the AdMob banner view.
        //https://developer.android.com/training/data-storage/shared-preferences.html
        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final boolean alreadyClickOnAd = sharedPref.getBoolean(getString(R.string.is_user_ad_clicker), false);

        if (!alreadyClickOnAd) {
            final AdRequest adRequest = new AdRequest.Builder()
                    .setRequestAgent("android_studio:ad_template")
                    .addTestDevice(MY_OP3T_TESTDEVICE_ID)
                    .build();
            adView.loadAd(adRequest);
            adView.setAdListener(adListener);
        } else {
            adView.setVisibility(View.GONE);
        }


    }

    private void initSnackbar() {
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.youShouldActivateSound,
                Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.upSound, new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                upSound();
            }
        });

    }

    /**
     * initialise le toast
     */
    private void initToast() {
        toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.youShouldActivateSound), Toast.LENGTH_SHORT);
    }

    private void upSound() {
        assert audioManager != null;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
    }

    @Override
    public void play() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
        }
        mediaPlayer.start();
    }

    @Override
    public void back() {
        isDialogLoaded = false;
    }
}