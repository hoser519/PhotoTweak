package com.example.mike.phototweak;


import android.support.v4.app.Fragment;

/**
 * A {@link Fragment} subclass. Used to display content to user.
 */
public abstract class ContentFragment extends Fragment {

    public abstract  String getFragmentTag();
    public abstract void deviceConnectionActive(boolean isActive);

    public ContentFragment() {
        // Required empty public constructor
    }


}
