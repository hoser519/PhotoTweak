package com.example.mike.phototweak;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * "headless" network management fragment. The fragment is retained across config changes and
 * is accessible from all activities and fragments that do network operations.
 */

public class NetworkIOFragment extends Fragment {

    public static final String TAG = "NetworkIOFragment";

    // Keys for saving/restoring
    private static final String KEY_HOSTIP = "HostIP";
    private static final String KEY_HOSTPORT = "HostPort";
    private static final String KEY_DEMOMODE = "DemoMode";

    // Network related classes and variables
    private NetworkIOFragment.NetworkIOCallback mCallback;
    private NetworkIOTask mNetworkIOTask;
    private TcpClient mTcpClient;
    // Network address of host
    private String mIPString;
    private int mPort;
    // Indicates user requires network transmission
    private boolean userNetworkTaskActive;
    // Demo mode will suppress any actual network ops from occuring
    private boolean demoMode;

    // Queue of device commands to send when the task thread is started. Use the concurrent queue because the UI
    // thread may be adding to queue at same time as it's being dequeued by the task thread.
    private ConcurrentLinkedQueue<String> outGoing;

    /**
     * Static factory method for NetwvorkFragment that sets the IP and Port of the host device it will be interfacing with.
     *
     * @param fragmentManager fragmentManager.
     * @param hostIP          the Host IP address
     * @param hostPort        the Host port
    * @param demoMode        Are we in demoMode-if so we won't launch the network task

     * @return A new or already running instance of NetworkIOFragment fragment.
     */
    public static NetworkIOFragment getInstance(FragmentManager fragmentManager, String hostIP, int hostPort, boolean demoMode) {
        // Recover NetworkFragment in case we are re-creating the Activity due to a config change.
        // This is necessary because NetworkFragment might have a task that began running before
        // the config change and has not finished yet.
        // The NetworkFragment is recoverable via this method because it calls
        // setRetainInstance(true) upon creation.
        NetworkIOFragment networkFragment = (NetworkIOFragment) fragmentManager.findFragmentByTag(NetworkIOFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkIOFragment();
            Bundle args = new Bundle();
            args.putString(KEY_HOSTIP, hostIP);
            args.putInt(KEY_HOSTPORT, hostPort);
            args.putBoolean(KEY_DEMOMODE, demoMode);
            networkFragment.setArguments(args);
            fragmentManager.beginTransaction().add(networkFragment, NetworkIOFragment.TAG).commit();
            //  execute the add operation immediately so it's visible next time we call fragmnetManager.getFragmentByTag() looking for it.
            fragmentManager.executePendingTransactions();
        }
        return networkFragment;
    }

    /**
     * Start non-blocking execution of NetworkIOTask.
     */
    public synchronized void startNetworkIO() {
        stopNetworkIO();
        if (mNetworkIOTask == null) {
            if (!demoMode) {
                mNetworkIOTask = new NetworkIOTask();
                userNetworkTaskActive = true;
                mNetworkIOTask.execute();
            }
        }
    }

    /**
     * Indicate we are finished doing network I/O
     */
    public synchronized void stopNetworkIO() {
        if (mNetworkIOTask != null) {
            userNetworkTaskActive = false;
            if (mNetworkIOTask.getStatus() == AsyncTask.Status.FINISHED) {
                mNetworkIOTask = null;
            }    // NOTE BON:  If we are waiting for Socket.connect it won't kill the task until Socket.connect returns, and then will be killed calling onCancelled).

        }
    }

    /**
     * Add device "commands" to the queue for network transmission\
     *
     * @param cmd the command
     */
    public void addToSendQueue(String cmd) {
        outGoing.add(cmd);
    }

    /**
     * Is theire a network task currently running?
     */
    public boolean getIsNetworkBusy() {
        if (mNetworkIOTask != null) {
            if ((mNetworkIOTask.getStatus() == AsyncTask.Status.RUNNING) ||
                    (mNetworkIOTask.getStatus() == AsyncTask.Status.PENDING)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Are we connected to the device?
     */
    public boolean isConnected() {
        if (mTcpClient != null)
            return mTcpClient.isConnected();
        else
            return false;
    }

    /**
     * Change the network config. takes effect next time we try to connect
     *
     * @param hostIP   Host IP Address
     * @param hostPort Host port
     */
    public void setConfig(String hostIP, int hostPort, boolean demoMode) {
        mIPString = hostIP;
        mPort = hostPort;
        this.demoMode = demoMode;
    }

    // Fragment lifecycle callbacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this Fragment across configuration changes in the host Activity.
        setRetainInstance(true);
        Log.v(NetworkIOFragment.class.getSimpleName(), "onCreate");
        // Set the network parameters
        mIPString = getArguments().getString(KEY_HOSTIP);
        mPort = getArguments().getInt(KEY_HOSTPORT);
        demoMode = getArguments().getBoolean(KEY_DEMOMODE);
        outGoing = new ConcurrentLinkedQueue<String>();
    }

    @Override
    public void onAttach(Activity parent) {
        super.onAttach(parent);
        Log.v(NetworkIOFragment.class.getSimpleName(), "onAttach");
        // Host Activity will handle callbacks from NetworkIOFragment.NetworkIOCallback
        try {
            mCallback = (NetworkIOCallback) parent;
        } catch (ClassCastException e) {
            throw new ClassCastException(" must implement NetworIOCallback");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //  ch0SeekBar.setProgress(ch0Pwr);
        Log.v(NetworkIOFragment.class.getSimpleName(), "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.v(NetworkIOFragment.class.getSimpleName(), "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(NetworkIOFragment.class.getSimpleName(), "onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(NetworkIOFragment.class.getSimpleName(), "onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(NetworkIOFragment.class.getSimpleName(), "onDetach");
        // Clear reference to host Activity.
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        Log.v(NetworkIOFragment.class.getSimpleName(), "onDestroy");
        stopNetworkIO();
        if (mTcpClient != null)
            if (mTcpClient.isConnected())
                mTcpClient.disconnect();

        super.onDestroy();
    }


    /**
     * Implementation of AsyncTask that runs network operations on a background thread.
     */
    private class NetworkIOTask extends AsyncTask<Void, Integer, NetworkIOTask.Result> {
        /**
         * Wrapper class that serves as a union of a result value and an exception. When the
         * download task has completed, either the result value or exception can be a non-null
         * value. This allows you to pass exceptions to the UI thread that were thrown during
         * doInBackground().
         */
        class Result {
            public String mResultValue;
            public Exception mException;

            public Result(String resultValue) {
                mResultValue = resultValue;
            }

            public Result(Exception exception) {
                mException = exception;
            }
        }

        // Check for presence of active network connection and cancel this NetworkIOTask
        // if not present.
        @Override
        protected void onPreExecute() {
            if (mCallback != null) {

                NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    mCallback.updateFromDownload(null);
                    cancel(true);
                }
            }
        }

        /**
         * Background network transmitter thread.
         * 1. Establish a Socket conection if not already present.
         * 2. Send all data in outGoing queue. Remains alive as long as userNetworkTaskActive is set
         * which is controlled by the UI thread through the startNetworkIO/startNetworkIO functions
         */

        @Override
        protected Result doInBackground(Void... param) {
            Result result = null;
            if (!isCancelled()) {
                String resultString = null;
                try {
                    if (mTcpClient == null) {
                        mTcpClient = new TcpClient(mIPString, mPort, getContext());
                    }
                    if (mTcpClient.isConnected()) {
                        // Send any pending message. If we couldn't send close socket and throw the error
                        try {
                            while (userNetworkTaskActive || (!outGoing.isEmpty())) {
                                if (!outGoing.isEmpty()) {
//                                    Log.v(NetworkIOFragment.class.getSimpleName(), "sending = " + outGoing.peek());
                                    mTcpClient.sendMessage(outGoing.poll());
                                }

                            }
                        } catch (IOException e) {
                            mTcpClient.disconnect();
                            mTcpClient = null;
                            throw e;
                        }
                        // TODO: Receive any data

                    } else {
                        //   Connect if not we haven't done so already

                        publishProgress(NetworkIOCallback.Progress.CONNECT_SUCCESS, 0);
                        try {
                            mTcpClient.connect();
                        } catch (Exception e) {
                            publishProgress(NetworkIOCallback.Progress.ERROR, 0);
                            mTcpClient.disconnect();
                            mTcpClient = null;
                            throw e;
                        }
                        publishProgress(NetworkIOCallback.Progress.CONNECT_SUCCESS, 100);
                    }

                    if (resultString != null) {
                        result = new Result(resultString);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch (Exception e) {
                    result = new Result(e);
                }
            }
            return result;
        }

        /**
         * Send NetworkIOCallback a progress update.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //Log.v(NetworkFragment.class.getSimpleName(), "onProgressUpdate");
            if (values.length >= 2) {
                mCallback.onProgressUpdate(values[0], values[1]);
            }
        }

        /**
         * Updates the NetworkIOFragment.NetworkIOCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {

            if (result != null && mCallback != null) {
                //  Log.v(NetworkFragment.class.getSimpleName(),"onPostExecute=");

                if (result.mException != null) {
                    mCallback.updateFromDownload(result.mException.getMessage());
                } else if (result.mResultValue != null) {
                    mCallback.updateFromDownload(result.mResultValue);

                }
                mCallback.cancelNetworkIO();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
            Log.v(NetworkIOFragment.class.getSimpleName(), "onCancelled() called");
            mCallback.cancelNetworkIO();
        }
    }

    /**
     * Defines callbacks for network operations to be implemented by host Activity
     * to receive information about network events
     */
    public interface NetworkIOCallback {
        interface Progress {
            int ERROR = -1;
            int CONNECT_SUCCESS = 1;
            int GET_INPUT_STREAM_SUCCESS = 2;
            int PROCESS_INPUT_STREAM_IN_PROGRESS = 3;
            int PROCESS_INPUT_STREAM_SUCCESS = 4;
        }

        /**
         * Indicates that the callback handler needs to update its appearance or information based on
         * the result of the task. Expected to be called from the main thread.
         *
         * @param result result of network operation
         */
        void updateFromDownload(String result);

        /**
         * Get the device's active network status in the form of a NetworkInfo object.
         */
        NetworkInfo getActiveNetworkInfo();

        /**
         * Indicate to callback handler any progress update.
         *
         * @param progressCode    must be one of the constants defined in NetworkIOCallback.Progress.
         * @param percentComplete must be 0-100.
         */
        void onProgressUpdate(int progressCode, int percentComplete);

        /**
         * Indicates that the download operation has finished. This method is called even if the
         * download hasn't completed successfully.
         */
        void cancelNetworkIO();

    }

}
