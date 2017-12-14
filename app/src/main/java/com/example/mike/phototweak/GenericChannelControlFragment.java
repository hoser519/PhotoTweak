package com.example.mike.phototweak;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

// TODO: This class is incomplete and not currently used
/**
 * A {@link ContentFragment} subclass. Implements the seekbar and other widgets
 * required to control
 */
public class GenericChannelControlFragment<T,U> extends Fragment {

    // fragment TAG for purposes of FragmentManager tracking
    public static final String TAG = "LightingLevelFragment";

    // widgets
    private VerticalSeekBar ch0SeekBar, ch1SeekBar;
    private TextView ch0PercentView, ch1PercentView, ch0Label, ch1Label;
    private View chView;

    // Keys for saving/restoring
    private static final String ARG_ch0PwrPercent = "ch0PwrPercent";
    private static final String ARG_ch1PwrPercent = "ch1PwrPercent";

    //Reference to our network fragment
    NetworkIOFragment mNetwork;

    public GenericChannelControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment or return an existing one
     *
     * @param fragmentManager fragmentManager.
     * @return A new instance of ContentFragment LightingLevelFragment.
     */

    public static GenericChannelControlFragment getInstance(FragmentManager fragmentManager) {

        GenericChannelControlFragment genericChannelControlFragment = (GenericChannelControlFragment) fragmentManager.findFragmentByTag(GenericChannelControlFragment.TAG);

        if (genericChannelControlFragment == null) {
            genericChannelControlFragment = new GenericChannelControlFragment();
            Bundle args = new Bundle();
            genericChannelControlFragment.setArguments(args);
        }
        return genericChannelControlFragment;
    }

    /**
     * Implement required abstract function to return TAG of this fragmnet
     *
     * @return FragmentManager Tag of this type of Fragment
     */
    public String getFragmentTag() {
        return TAG;
    }

    /**
     * Invoked by parent Activity when device becomes available on network
     *
     * @param isActive The status of the network
     * @return A new instance of ContentFragment LightingLevelFragment.
     */
    //
    public void deviceConnectionActive(boolean isActive) {

        // enable the seekbars
        ch0SeekBar.setEnabled(isActive);
        ch0PercentView.setEnabled(isActive);
        ch0Label.setEnabled(isActive);

        ch1SeekBar.setEnabled(isActive);
        ch1PercentView.setEnabled(isActive);
        ch1Label.setEnabled(isActive);

    }

    /**
     * Setup the channel send out network commands based on SeekBar changes
     * and update the chPwrPercentView TextView.
     *
     * @param chPwrSeekBar The VerticalSeekBar for this channel
     * @param chPwrPercentView The percent TextView for this channel
     */

    public void chSetup(VerticalSeekBar chPwrSeekBar, final TextView chPwrPercentView) {

        // Set inital value to 0
        chPwrPercentView.setText(0 + " %");

        // Setup the SeekBar to send out network commands and update the chPwrPercentView TextView
        chPwrSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int startProgress;
            private int lastProgress;
            private boolean justStarted;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String percentProgress = progress + " %";
                // Only do a change if the callback was triggered  by the user input and not from a configuration change (e.g. screen rotation)
                if (fromUser) {
                    // If we justStarted moving the slider
                    if (justStarted) {
                        if (progress > startProgress) {
                            for (int i = 0; i < (progress - startProgress); i++) {
                                mNetwork.addToSendQueue("U");

                            }
                        } else if (progress < startProgress) {
                            for (int i = 0; i < (startProgress - progress); i++) {
                                mNetwork.addToSendQueue("D");
                            }
                        }
                        justStarted = false;
                    } else {
                        // If already moving the slider
                        if (progress > lastProgress) {
                            for (int i = 0; i < (progress - lastProgress); i++) {
                                mNetwork.addToSendQueue("U");

                            }
                        } else if (progress < lastProgress) {
                            for (int i = 0; i < (lastProgress - progress); i++) {
                                mNetwork.addToSendQueue("D");
                            }
                        }
                    }
                    lastProgress = progress;
                    chPwrPercentView.setText(percentProgress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                startProgress = seekBar.getProgress();
                justStarted = true;
                mNetwork.startNetworkIO();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mNetwork.stopNetworkIO();
            }
        });
    }


    // Boilerplate Fragment lifecycle methods ....

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onCreate");
        if (getArguments() != null) {
            //
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chView = inflater.inflate(R.layout.fragment_lighting_power_level, container, false);

        // get Ch0
        ch0SeekBar = (VerticalSeekBar) chView.findViewById(R.id.ch0PwrSeekbar);
        ch0Label = (TextView) chView.findViewById(R.id.ch0PwrLabel);
        ch0PercentView = (TextView) chView.findViewById(R.id.ch0PwrPercentView);
        // get Ch1
        ch1SeekBar = (VerticalSeekBar) chView.findViewById(R.id.ch1PwrSeekbar);
        ch1Label = (TextView) chView.findViewById(R.id.ch1PwrLabel);
        ch1PercentView = (TextView) chView.findViewById(R.id.ch1PwrPercentView);

        // Set focus on to Seekbar so we don't auto go to the TextView and get the soft-keyboard coming up
        ch0SeekBar.setFocusable(true);
        ch0SeekBar.setFocusableInTouchMode(true);
        ch0SeekBar.requestFocus();

        return chView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // We can be sure at  this point that activity.create()
        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onActivityCreated");

        if (savedInstanceState != null) {
            //Restore the fragment's state here
            ch0SeekBar.setProgress( savedInstanceState.getInt(ARG_ch0PwrPercent) );
            ch1SeekBar.setProgress( savedInstanceState.getInt(ARG_ch1PwrPercent) );
        }
        // get network, i assume here the network fragment already exists (it will)
        mNetwork = NetworkIOFragment.getInstance(getFragmentManager(),"",-1,false);
        // setup the controls for channels (SeekBar & TextView)
        chSetup(ch0SeekBar, ch0PercentView);
        chSetup(ch1SeekBar, ch1PercentView);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onStart");
        // put server in correct mode for adjusting Light Power Level - cmd is "d"
        if (!mNetwork.isConnected()) {
            deviceConnectionActive(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(GenericChannelControlFragment.class.getSimpleName(), "onDestroy");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
        outState.putInt(ARG_ch0PwrPercent, ch0SeekBar.getProgress());
        outState.putInt(ARG_ch1PwrPercent, ch1SeekBar.getProgress());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        int ch0PwrPercent, ch1PwrPercent;
        // Restore the fragment's state here
        if (savedInstanceState != null) {
            ch0PwrPercent = savedInstanceState.getInt(ARG_ch0PwrPercent);
            ch0SeekBar.setProgress(ch0PwrPercent);
            ch0PercentView.setText(ch0PwrPercent + "%");

            ch1PwrPercent = savedInstanceState.getInt(ARG_ch1PwrPercent);
            ch1SeekBar.setProgress(ch1PwrPercent);
            ch1PercentView.setText(ch1PwrPercent + "%");
        }
    }

}
