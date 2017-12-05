package fr.dbordet.hey.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
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
    private AudioManager audioManager;

    /**
     * Pour communiquer avec l'activité et lui faire jouer "hey"
     */
    private DialogSoundManagerOwner callback;

    /**
     * Surcharge pour récupérer l'activité appelante
     *
     * @param context l'activité appelante
     */
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
    private void initSeekbar(SeekBar volumeSeekbar) {
        //initalisation du pas et de la taille grâces aux paramètres sonores
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        volumeSeekbar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekbar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                //noAction
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                //noAction
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
    }
}
