package com.example.danhui_cosmos_project;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

public class Sub1Activity extends MainActivity{
    private static final String TAG = "BluetoothConnect";


    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter btAdapter;
    private String dName;
    BluetoothDevice device;
    private int mState;
    public static final int STATE_NONE = 0; // 아무것도 하지 않을 때
    public static final int STATE_LISTEN = 1; // 연결을 위해 리스닝에 들어갈 때
    public static final int STATE_CONNECTING = 2; // 연결 과정이 이루어 질 때
    public static final int STATE_CONNECTED = 3; // 기기 사이에서의 연결이 이루어 졌을 때
    public static final int STATE_FAIL = 7; // 연결이 실패 했을 때


    private BluetoothService btService;
    private BluetoothDevice btDevice = null;

    static TextView tx_recevie;
    static Button btn_send;
    static EditText etx_send;
    static String sendstr;
    String deviceName;
    boolean SendMs = false;
    static int STATE;
    static byte[] buffer;
    String fullWord = "";
    int newMsg = 1;
    double value[] = new double[4];
    String[] strArr = new String[4];

    private ConnectThread mConnectThread; // ������ �ٽ�
    private ConnectedThread mConnectedThread; // ������ �ٽ�
    //MainActivity mc;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tx_recevie = (TextView) findViewById(R.id.tx_receive);

        tx_recevie.setMovementMethod(new ScrollingMovementMethod());
        btn_send = (Button) findViewById(R.id.btn_send);
        etx_send = (EditText)  findViewById(R.id.etx_send);
        //btDevice=  btService.getBtConnection();

       // btAdapter = BluetoothAdapter.getDefaultAdapter();
         Intent intent=new Intent(this.getIntent());
         //if(intent!=null)
         //    getDeviceInfo(intent);
         recevieData(intent);

        // mc = new MainActivity();
        btn_send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                onBtnClickSend();

                sendMsgClear();
            }
        });







    }
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:

                // NavUtils.navigateUpFromSameTask(this);

                finish();

                return true;

        }

        return super.onOptionsItemSelected(item);


    };

    public static void sendMsgClear(){
        etx_send.setText("");
    }
    public static void textV(String str){

        tx_recevie.append(str);

    }


    public void getDeviceInfo(Intent data) {
        // Get the device MAC address

            String address = data.getExtras().getString(
                    DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // Get the BluetoothDevice object
            // BluetoothDevice device = btAdapter.getRemoteDevice(address);
            device = btAdapter.getRemoteDevice(address);
            dName = device.getName();
            Log.d(TAG, "Get Device Info \n" + "address : " + address);

            connect(device);

    }
    public void recevieData(Intent i){

        String dName = i.getStringExtra("dvName");
        if(dName==null)
            tx_recevie.setText("현재 연결된 장치가 없습니다\n");

        else {

            tx_recevie.setText( dName+" 장치가 연결되어 있습니다.\n");
            if(i.getStringExtra("RECEIVEMSG")!=null){
            String strMsg = i.getStringExtra("RECEIVEMSG");
            tx_recevie.append(strMsg);
            }
            else
                Log.d(TAG, "Get" + ""+i.getStringExtra("RECEIVEMSG") );
        }

    }

    private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);


            if (SendMs && msg.obj != null) {
                Log.d("핸들러 테스트", "" + msg.obj);
                String stD = "" + (String) msg.obj;

                fullWord = fullWord + stD;
                Log.d("핸들러 총 단어 테스트", "" + fullWord);
                if (fullWord.length() > 0 && fullWord.charAt(fullWord.length() - 1) == '\n') {

                    //받은 명령에 맨마지막 다음 줄로가는 문자를 삭제함.
                    tx_recevie.append(deviceName+ " : " + fullWord);
                    fullWord = fullWord.substring(0, fullWord.length() - 1);
                    strArr[0] +=fullWord;


                    if (strArr[0] != null)

                        fullWord = "";
                }


            }


        }

    };


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
            this.start();
            return;
        }

        // ConnectThread Ŭ������ reset�Ѵ�.
        synchronized (this) {
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
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mHandler.obtainMessage(0, state,-1).sendToTarget();
        mState = state;
    }
    // ���� ����������
    private void connectionFailed() {
        setState(STATE_LISTEN);
    }

    // ������ �Ҿ��� ��
    private void connectionLost() {
        setState(STATE_LISTEN);

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
