package com.example.mike.phototweak;


import android.support.v4.app.Fragment;

/**
 * A {@link Fragment} subclass. Used to display content to user.
 */
public abstract class ContentFragment extends Fragment {

    /**
     * Implementation of required abstract function to return TAG of this fragmnet
     *
     * @return the TAG name of this fragmnet in FragmentManager
     */
    public abstract  String getFragmentTag();

    /**
     * Invoked by parent Activity when device becomes available on network
     *
     * @param isActive The status of the network.
     */
    public abstract void deviceConnectionActive(boolean isActive);

    public ContentFragment() {
        // Required empty public constructor
    }


}
