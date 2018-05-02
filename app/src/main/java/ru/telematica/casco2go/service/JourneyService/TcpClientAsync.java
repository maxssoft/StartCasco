package ru.telematica.casco2go.service.JourneyService;
import android.util.Log;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Базовый клиент для передачи данных по TCP
 */
public class TcpClientAsync extends AsyncTask {

    private final String TAG = TcpClientAsync.class.getSimpleName();
    private Socket mSocket;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private OutputStream mBufferOut;

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public boolean sendMessage(byte[] message) {
        if (mBufferOut != null) {
            try {
                mBufferOut.write(message);
                mBufferOut.flush();
                return true;
            } catch (Exception e) {
                Log.d(TAG, "e:" + e);
            }
        }
        return false;
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        mRun = false;
        try {
            if (mBufferOut != null) {
                mBufferOut.flush();
                mBufferOut.close();
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
        mBufferOut = null;
        closeSocket();
    }

    public void closeSocket() {
        Log.d(TAG, "closeSocket");

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        }
        mSocket = null;
    }

    public void run(final String host, final int port, final Interface i) {
        mTaskExecutor.execute(() -> {
            mRun = true;
            try {
                InetAddress addr = InetAddress.getByName(host);
                Socket socket = new Socket(addr, port, true);
                mBufferOut = socket.getOutputStream();
                if (mRun) {
                    i.onConnect();
                }
                while (mRun) {
                    i.onRequest();
                    Thread.sleep(100);
                }

            } catch (Exception e) {
                Log.e(TAG, "", e);
                TcpClientAsync.this.closeSocket();
                if (mRun) {
                    i.onException(e);
                }
            }
        });
    }

    public interface Interface {
        void onConnect();

        void onRequest();

        void onResponse(char... response);

        void onException(Exception e);
    }
}
