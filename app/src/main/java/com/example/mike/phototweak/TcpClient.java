package com.example.mike.phototweak;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A class to manage all low-level socket transactions and connections
 */

public class TcpClient {

    // message to send to the server
    private String mServerMessage;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    // The socket
    Socket mSocket;
    // Network paramters
    private String mIPString;
    private int mPort;

    Context context;

    /**
     * Sends the message entered by client to the server
     *
     * @param mIPString the IP string
     * @param mPort     the IP port
     * @param context   the
     */
    public TcpClient(String mIPString, int mPort, Context context) {
        this.mIPString = mIPString;
        this.mPort = mPort;
        //  THis is a non-actitivuty class so we need context to access context.getResources() date
        this.context = context;

    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) throws IOException {

        if (mBufferOut.checkError()) {
            throw new IOException("Server unreachable - connection severed");
        }
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.print(message);
            mBufferOut.flush();
        }
    }

    /**
     * Is socket connected?
     */

    public boolean isConnected() {
        if (mSocket != null) return mSocket.isConnected();
        return false;
    }

    /**
     * Close the connection and release the members
     */
    public void disconnect() {

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        //mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
        if (mSocket != null)
            try {
                mSocket.close();
            } catch (Exception e) {
                Log.e("TCP", "C: Error", e);

            }
    }

    /**
     * Attempt to connect,
     */

    public void connect() throws Exception {

        InetAddress serverAddress = InetAddress.getByName(mIPString);

        //create a socket to make the connection with the server
        mSocket = new Socket();
        mSocket.connect(new InetSocketAddress(serverAddress, mPort), context.getResources().getInteger(R.integer.socket_connect_timeoutms));
        //sends the message to the server
        mBufferOut = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(mSocket.getOutputStream())), true);
        //receives the message which the server sends back
        mBufferIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
    }

}

