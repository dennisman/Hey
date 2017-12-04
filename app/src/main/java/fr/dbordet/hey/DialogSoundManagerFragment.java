package fr.dbordet.hey;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Fragment Custom pour gérer le son
 */

public class DialogSoundManagerFragment extends DialogFragment {

    private SeekBar volumeSeekbar;
    private AudioManager audioManager;
    private AlertDialog.Builder alertDialogBuilder;
    private DialogSoundManagerOwner callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            callback = (DialogSoundManagerOwner) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DialogSoundManagerOwner");
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_soundmanager, null);
        volumeSeekbar = view.findViewById(R.id.seekBar);
        initAudioManager();
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        volumeSeekbar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekbar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));


        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
                if (callback != null) {
                    callback.play();
                }
            }
        });
        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view)
                .setNegativeButton(R.string.back, null);

        return alertDialogBuilder.create();
    }

    public void initAudioManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager = getActivity().getApplicationContext().getSystemService(AudioManager.class);
        } else {
            audioManager = (AudioManager) getActivity().getApplicationContext().getSystemService(AUDIO_SERVICE);
        }
    }
}
