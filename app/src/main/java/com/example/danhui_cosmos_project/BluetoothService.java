package com.example.danhui_cosmos_project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.EditText;
import android.widget.Toast;


public class BluetoothService extends Application implements Serializable {
    // Debugging
    private static final String TAG = "BluetoothService";

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter btAdapter;

    private Activity mActivity;
    private Handler mHandler;
    private String dName;
    private ConnectThread mConnectThread; // ������ �ٽ�
    private ConnectedThread mConnectedThread; // ������ �ٽ�


    Context context;

    BluetoothDevice device;
    // ���¸� ��Ÿ���� ���� ����
    private int mState;
    public static final int STATE_NONE = 0; // 아무것도 하지 않을 때
    public static final int STATE_LISTEN = 1; // 연결을 위해 리스닝에 들어갈 때
    public static final int STATE_CONNECTING = 2; // 연결 과정이 이루어 질 때
    public static final int STATE_CONNECTED = 3; // 기기 사이에서의 연결이 이루어 졌을 때
    public static final int STATE_FAIL = 7; // 연결이 실패 했을 때
    // Constructors
    public BluetoothService(Activity ac, Handler h) {
        mActivity = ac;
        mHandler = h;

        // BluetoothAdapter ���
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void writeData(byte[] bf){
        mConnectedThread.write(bf);
    }
    public String readData(){
        return mConnectedThread.setText();
    }
    public String getName(){
        return dName;
    }

    /*
     * Check the Bluetooth support
     *
     * @return boolean
     */
    public boolean getDeviceState() {
        Log.i(TAG, "Check the Bluetooth support");

        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");

            return false;

        } else {
            Log.d(TAG, "Bluetooth is available");

            return true;
        }
    }

    /**
     * Check the enabled Bluetooth
     */
    public void enableBluetooth() {
        Log.i(TAG, "Check the enabled Bluetooth");

        if (btAdapter.isEnabled()) {
            // ����� ������� ���°� On�� ���
            Log.d(TAG, "Bluetooth Enable Now");

            // Next Step
            scanDevice();
        } else {
            // ����� ������� ���°� Off�� ���
            Log.d(TAG, "Bluetooth Enable Request");

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(i, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Available device search
     */
    public void scanDevice() {
        Log.d(TAG, "Scan Device");

        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);

        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

    }

    /**
     * after scanning and get device info
     *
     * @param
     */

    public void getDeviceInfo(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(
                DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        // BluetoothDevice device = btAdapter.getRemoteDevice(address);
        device = btAdapter.getRemoteDevice(address);
        dName=device.getName();
        Log.d(TAG, "Get Device Info \n" + "address : " + address);

        connect(device);
    }

    // Bluetooth ���� set
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mHandler.obtainMessage(0, state,-1).sendToTarget();
        mState = state;
    }

    // Bluetooth ���� get
    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    // ConnectThread �ʱ�ȭ device�� ��� ���� ����
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);

        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    // ConnectedThread �ʱ�ȭ
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);



    }

    // ��� thread stop
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    // ���� ���� �κ�(������ �κ�)
    public void write(byte[] out) { // Create temporary object
        ConnectedThread r; // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        } // Perform the write unsynchronized r.write(out); }
    }

    // ���� ����������
    private void connectionFailed() {
        setState(STATE_LISTEN);
    }

    // ������ �Ҿ��� ��
    private void connectionLost() {
        setState(STATE_LISTEN);

    }

    public int getmState() {
        return mState;
    }

    private class ConnectThread extends Thread  {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            /*
             * / // Get a BluetoothSocket to connect with the given
             * BluetoothDevice try { // MY_UUID is the app's UUID string, also
             * used by the server // code tmp =
             * device.createRfcommSocketToServiceRecord(MY_UUID);
             *
             * try { Method m = device.getClass().getMethod(
             * "createInsecureRfcommSocket", new Class[] { int.class }); try {
             * tmp = (BluetoothSocket) m.invoke(device, 15); } catch
             * (IllegalArgumentException e) { // TODO Auto-generated catch block
             * e.printStackTrace(); } catch (IllegalAccessException e) { // TODO
             * Auto-generated catch block e.printStackTrace(); } catch
             * (InvocationTargetException e) { // TODO Auto-generated catch
             * block e.printStackTrace(); }
             *
             * } catch (NoSuchMethodException e) { // TODO Auto-generated catch
             * block e.printStackTrace(); } } catch (IOException e) { } /
             */

            // ����̽� ������ �� BluetoothSocket ����
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            //소켓 생성 부분
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread11");
            setName("ConnectThread");

            // ������ �õ��ϱ� ������ �׻� ��� �˻��� �����Ѵ�.
            // ��� �˻��� ��ӵǸ� ����ӵ��� �������� �����̴�.
            btAdapter.cancelDiscovery();

            // BluetoothSocket ���� �õ�
            try {
                // RFCOMM 채널을 통한 연결
                mmSocket.connect();

                Log.d(TAG, "Connect Success");

            } catch (IOException e) {
                connectionFailed(); // ���� ���н� �ҷ����� �޼ҵ�
                Log.d(TAG, "Connect Fail");

                // socket�� �ݴ´�.
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // ������? Ȥ�� ���� �������� �޼ҵ带 ȣ���Ѵ�.
                BluetoothService.this.start();
                return;
            }

            // ConnectThread Ŭ������ reset�Ѵ�.
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // ConnectThread�� �����Ѵ�.
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        String dataMs;
        private EditText receivetext;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");


            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket�� inputstream �� outputstream�� ��´�.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();


            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread22");

            byte[] buffer = new byte[2048];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    Message msg = mHandler.obtainMessage();

                    // InputStream���κ��� ���� �޴� �д� �κ�(���� �޴´�)
                    bytes = mmInStream.read(buffer);
                    dataMs = new String(buffer,0,bytes);

                    msg.obj =dataMs;
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }

        }


        /**
         * Write to the connected OutStream.
         *
         * @param buffer
         *            The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                // ���� ���� �κ�(���� ������)
                mmOutStream.write(buffer);
                Log.d("테스트"," : "+ buffer.toString());
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public String setText(){
            return dataMs;
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}