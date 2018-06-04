package com.mrozpoyraz.bluetoothproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by Burak on 29.11.2017.
 */

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionSrvc";
    private static final UUID connectionUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothConnectionService instanceBCS;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private InputStream inputStream;
    private OutputStream outputStream;
    String incomingMessage;

    private OnBluetoothConnectingListener listener;
    private OnBluetoothConnectionListener listener2;

    public static BluetoothConnectionService getInstanceBCS() {
        if (instanceBCS == null) {
            instanceBCS = new BluetoothConnectionService();
        }
        return instanceBCS;
    }

    public void setDevice(BluetoothDevice device) {
        bluetoothDevice = device;
    }

    public BluetoothConnectionService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setOnBluetoothConnectingListener(OnBluetoothConnectingListener l) {
        listener = l;
    }

    public void setOnBluetoothConnectionListener(OnBluetoothConnectionListener l) {
        listener2 = l;
    }

    public void startClient() {
        Log.d(TAG, "startClient: Started.");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        connectThread = new ConnectThread();
        connectThread.start();
    }

    private class ConnectThread extends Thread {

        public ConnectThread() {
            Log.d(TAG, "ConnectThread: Started.");
        }

        public void run() {
            Log.i(TAG, "ConnectThread: run(), started.");
            bluetoothAdapter.cancelDiscovery();
            BluetoothSocket tmp = null;
            try {
                Log.d(TAG, "ConnectThread: Trying to create RFcommSocket using UUID: " + connectionUUID);
                tmp = createBluetoothSocket(bluetoothDevice);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "ConnectThread: Couldn't create RFcommSocket." + e.getMessage());
                listener.onFailure();
            }

            if (tmp != null) {
                bluetoothSocket = tmp;
                bluetoothAdapter.cancelDiscovery();
                try {
                    bluetoothSocket.connect();
                    Log.d(TAG, "ConnectThread: run(), socket is connected.");
                    listener.onSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onFailure();
                    try {
                        bluetoothSocket.close();
                        Log.d(TAG, "ConnectThread: run(), socket is closed.");
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        Log.e(TAG, "ConnectThread: run(), Unable to close connection in socket." + e1.getMessage());
                    }
                    Log.d(TAG, "ConnectThread: run(), Could not connect to UUID: " + connectionUUID);
                }
            }
        }

        public void cancel() {
            try {
                if (bluetoothSocket != null) {
                    Log.d(TAG, "ConnectThread: cancel(), closing client socket.");
                    bluetoothSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "ConnectThread: cancel(), close of bluetoothSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    public void connected() {
        Log.d(TAG, "connected: Started.");
        connectedThread = new ConnectedThread();
        connectedThread.start();
    }

    private class ConnectedThread extends Thread {

        public ConnectedThread() {
            Log.d(TAG, "ConnectedThread: Started.");
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            Log.d(TAG, "ConnectedThread: Read is started.");
            byte[] readBuffer = new byte[1024];
            int readBytes;

            while (true) {
                try {
                    readBytes = inputStream.read(readBuffer);
                    incomingMessage = new String(readBuffer, 0, readBytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    if (!incomingMessage.equals(null)) {
                        listener2.onRead();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "ConnectedThread: Error reading from inputStream." + e.getMessage());
                    listener2.onConnectionLost();
                    break;
                }
            }
        }

        public void write(byte[] writeBytes) {
            String text = new String(writeBytes, Charset.defaultCharset());
            Log.d(TAG, "ConnectedThread: write(), writing to outputStream: " + text);
            try {
                outputStream.write(writeBytes);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "ConnectedThread: write(), error writing to outputStream." + e.getMessage());
            }
        }

        public void cancel() {
            if (bluetoothSocket != null) {
                try {
                    Log.d(TAG, "ConnectedThread: cancel(), closing client socket.");
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ConnectedThread: cancel(), close of bluetoothSocket in ConnectedThread failed. " + e.getMessage());
                }
            }

            if (inputStream != null) {
                try {
                    Log.d(TAG, "ConnectedThread: cancel(), closing input stream.");
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }
            if (outputStream != null) {
                Log.d(TAG, "ConnectedThread: cancel(), closing output stream.");
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
        }
    }

    public void write (byte[] out) {
        Log.d(TAG, "ConnectedThread: write(), write Called.");
        connectedThread.write(out);
    }

    public void stop() {
        Log.d(TAG, "stop: Connection is finished.");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
    }

    public String getIncomingMessage() {
        Log.d(TAG, "ConnectedThread: read(), getIncomingMessage called Called.");
        return incomingMessage;
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
            throws IOException {
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, connectionUUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(connectionUUID);
    }

    public interface OnBluetoothConnectingListener {
        public void onSuccess();
        public void onFailure();
    }

    public interface OnBluetoothConnectionListener {
        public void onConnectionLost();
        public void onRead();
    }
}
