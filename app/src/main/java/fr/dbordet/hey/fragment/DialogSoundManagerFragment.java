package fr.dbordet.hey.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import fr.dbordet.hey.R;
import fr.dbordet.hey.activity.DialogSoundManagerOwner;
import fr.dbordet.hey.helper.InitHelper;

/**
 * Fragment Custom pour gérer le son
 */

public class DialogSoundManagerFragment extends DialogFragment {

    /**
     * Gere les paramètres sonores
     */
    @Nullable
    private AudioManager audioManager;

    /**
     * Pour communiquer avec l'activité et lui faire jouer "hey"
     */
    private DialogSoundManagerOwner callback;

    /**
     * Surcharge pour récupérer l'activité appelante
     * Attention : n'existe pas avant l'api 23
     *
     * @param context l'activité appelante
     */
    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            callback = (DialogSoundManagerOwner) context;
        } catch (final ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DialogSoundManagerOwner");
        }
    }

    /**
     * Surcharge pour récupérer l'activité appelante
     * deprecated < 23
     *
     * @param activity l'activité appelante
     */
    @Override
    public void onAttach(@NonNull final Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Verify that the host activity implements the callback interface
            try {
                callback = (DialogSoundManagerOwner) activity;
            } catch (final ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString()
                        + " must implement DialogSoundManagerOwner");
            }
        }
    }


    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //recupération de la seekbar
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_soundmanager, null);
        final SeekBar volumeSeekbar = view.findViewById(R.id.seekBar);

        //initialisation des composants
        audioManager = InitHelper.initAudioManager(getActivity().getApplicationContext());
        initSeekbar(volumeSeekbar);

        //création du dialog
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(view)
                .setNegativeButton(R.string.back, null);
        return alertDialogBuilder.create();
    }

    /**
     * initialisation de la seekbar
     *
     * @param volumeSeekbar la seekbar à initialiser
     */
    private void initSeekbar(@NonNull final SeekBar volumeSeekbar) {
        //initalisation du pas et de la taille grâces aux paramètres sonores
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        assert audioManager != null;
        volumeSeekbar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekbar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(final SeekBar arg0) {
                //noAction
            }

            @Override
            public void onStartTrackingTouch(final SeekBar arg0) {
                //noAction
            }

            @Override
            public void onProgressChanged(final SeekBar arg0, final int progress, final boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
                if (callback != null) {
                    callback.play();
                }
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        callback.back();
    }
}
