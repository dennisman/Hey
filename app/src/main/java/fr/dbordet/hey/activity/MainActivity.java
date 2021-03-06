package fr.dbordet.hey.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import fr.dbordet.hey.R;
import fr.dbordet.hey.broadcastreceiver.AlarmBroadcastReceiver;
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
    private static final int ALARMREQUESTCODE = "ALARMREQUESTCODE".hashCode();

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
    private boolean trollMode;

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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        final RadioGroup gender = (RadioGroup) menu.findItem(R.id.action_gender).getActionView();
        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putBoolean(getString(R.string.is_male), id == R.id.male);
                editor.apply();
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = InitHelper.initMediaPlayer(MainActivity.this, id == R.id.male);
            }
        });
        final boolean isMale = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.is_male), true);
        gender.check(isMale ? R.id.male : R.id.female);

        return true;
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
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        audioManager = InitHelper.initAudioManager(this.getApplicationContext());
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || !this.getSystemService(ActivityManager.class).isLowRamDevice()) {
            initAdView();
        }
        trollMode = InitHelper.initTrollMode(this);//TODO modif le trollMode au clic
        mediaPlayer = InitHelper.initMediaPlayer(this);
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
                final SharedPreferences.Editor editor = MainActivity.this.getPreferences(Context.MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.is_user_ad_clicker), true);
                editor.apply();
                //register in DB
            }
        };

        // initialize the Mobile Ads SDK
        MobileAds.initialize(this, MY_APP_ADS_ID);
        // Load an ad into the AdMob banner view.
        //https://developer.android.com/training/data-storage/shared-preferences.html
        final boolean alreadyClickOnAd = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.is_user_ad_clicker), false);

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
    @SuppressLint("ShowToast")
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

    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_alarm:
                final Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
                final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this.getApplicationContext(), ALARMREQUESTCODE, intent, 0);
                final AlarmManager alarmManager = InitHelper.initAlarmManager(this);
                assert alarmManager != null;
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + (5 * 1000), pendingIntent);
                Toast.makeText(this, "Alarm set in " + 5 + " seconds", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}