package com.example.mike.phototweak;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


/**
 * A {@link DialogFragment} subclass.
 * Displays an info screen about the apps controls
 * Use the {@link InfoScreenFragment#getInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoScreenFragment extends DialogFragment {

    public static final String TAG = "InfoScreenFragment";
    // Help/Info screen
    WebView mHelpView;
    View infoView;

    public InfoScreenFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fragmentManager fragmentManager.
     * @return A new instance of fragment InfoScreenFragment.
     */
    public static InfoScreenFragment getInstance(FragmentManager fragmentManager) {

        InfoScreenFragment infoScreenFragment = (InfoScreenFragment) fragmentManager.findFragmentByTag(InfoScreenFragment.TAG);

        if (infoScreenFragment == null) {
            infoScreenFragment = new InfoScreenFragment();
            Bundle args = new Bundle();
            infoScreenFragment.setArguments(args);
        }
        return infoScreenFragment;
    }

    // Boileplate Fragment life-cycle callbacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        infoView =  inflater.inflate(R.layout.fragment_info_screen, container, false);

        // Load webpage
        mHelpView = (WebView) infoView.findViewById(R.id.webview);
        mHelpView.loadUrl("file:///android_asset/info.html");
        //   String infoPage = getString(R.string.infopage);
        // mHelpView.loadData(infoPage,"text/html","utf-8");
        //mHelpView.setVisibility(View.VISIBLE);
        return infoView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}