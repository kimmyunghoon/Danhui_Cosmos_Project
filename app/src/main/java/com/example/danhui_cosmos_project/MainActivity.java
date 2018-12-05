package com.example.danhui_cosmos_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // Debugging
    private static final String TAG = "Main";

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    //time
    long now;
    private AsyncTask<Void, Void, Void> mTask;
    long now1;
    private AsyncTask<Void, Void, Void> mTask1;
    //gps 변수들선언
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;







    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter btAdapter;
   static String dName="";
    BluetoothDevice device;
    private int mState;
    public static final int STATE_NONE = 0; // 아무것도 하지 않을 때
    public static final int STATE_LISTEN = 1; // 연결을 위해 리스닝에 들어갈 때
    public static final int STATE_CONNECTING = 2; // 연결 과정이 이루어 질 때
    public static final int STATE_CONNECTED = 3; // 기기 사이에서의 연결이 이루어 졌을 때
    public static final int STATE_FAIL = 7; // 연결이 실패 했을 때

    // GPSTracker class
    private GpsInfo gps;


    // Layout
    // Header
    private Button btn_Connect;
    private ImageView img_time1;
    private ImageView img_time2;
    //3번은 콜론이미지
    private ImageView img_time4;
    private ImageView img_time5;
    private ImageView img_time6;
    private ImageView img_time7;
    //main
    private Button btn_Select1;
    private Button btn_Select2;
    private Button btn_Select3;
    private Button btn_Select4;
    private Button btn_Select5;
    private Button btn_Select6;
    //footer
    private TextView tx_state;
    private TextView tx_location;
    private TextView tx_receive;

    Sub1Activity subAC;
    private View sub_tx;
    static Button bt_send;
    private View header;

    //문자열 받는 부분 다른 액티비티에서 구현할 예정
    private TextView receiveString;
    private EditText sendString;
    private Button btn_Clear;

    //time
    private char[] DataCh=new char[10];

    //블루투스 관련
    static BluetoothService btService = null;
    boolean SendMs = false;
    static int STATE;
    static byte[] buffer;
    String fullWord = "";
    int newMsg = 1;
    double value[] = new double[4];
    String[] strArr = new String[4];

    //미정
    private ProgressBar progress2 ;
    InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //시간 출력
        ShowTimeMethod();
        // BtnOnClickListener의 객체 생성.
        BtnOnClickListener onClickListener = new BtnOnClickListener() ;
        // BluetoothService
        if (btService == null) {
            btService =  new BluetoothService(this,mHandler);

        }
        subAC = new Sub1Activity();
        //button 선언
        btn_Connect = (Button) findViewById(R.id.btn_connect);
        btn_Connect.setOnClickListener(onClickListener);
        btn_Select1 = (Button) findViewById(R.id.btn_select1);
        btn_Select1.setOnClickListener(onClickListener);
        btn_Select2 = (Button) findViewById(R.id.btn_select2);
        btn_Select2.setOnClickListener(onClickListener);
        btn_Select3 = (Button) findViewById(R.id.btn_select3);
        btn_Select3.setOnClickListener(onClickListener);
        btn_Select4 = (Button) findViewById(R.id.btn_select4);
        btn_Select4.setOnClickListener(onClickListener);
        btn_Select5 = (Button) findViewById(R.id.btn_select5);
        btn_Select5.setOnClickListener(onClickListener);
        btn_Select6 = (Button) findViewById(R.id.btn_select6);
        btn_Select6.setOnClickListener(onClickListener);

        //textview 선언
        tx_location = (TextView) findViewById(R.id.tx_location);
        tx_location.setTextColor(getResources().getColor(R.color.fontcolor, getResources().newTheme()));
        tx_state = (TextView) findViewById(R.id.tx_state);
        tx_state.setTextColor(getResources().getColor(R.color.fontcolor, getResources().newTheme()));

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //imgview 선언
        img_time1 = (ImageView)findViewById(R.id.img_time1);
        img_time2 = (ImageView)findViewById(R.id.img_time2);
        img_time4 = (ImageView)findViewById(R.id.img_time4);
        img_time5 = (ImageView)findViewById(R.id.img_time5);
        img_time6 = (ImageView)findViewById(R.id.img_time6);
        img_time7 = (ImageView)findViewById(R.id.img_time7);
        //기타
     /*   header = getLayoutInflater().inflate(R.layout.activity_sub1, null, false);
        bt_send = (Button) header.findViewById(R.id.btn_send);
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBtnClickSend(v);

            }
        });*/

        //bt_send =



        //이미지파일 휴대폰에 저장하기
        SaveBitmapFile();
        //로딩후 gps 권한 획득하기
        int allow=0;
        if (!isPermission&&allow ==0) {
            callPermission();
        }
        else
            allow =1;





    }
    //버튼 클릭 이벤트
    class BtnOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_connect :
                    if (btService.getDeviceState()) {
                        dName=btService.getName();
                        btService.enableBluetooth();
                        if (btService.getName() != null)
                            btn_Connect.setText(btService.getName());
                        else
                            btn_Connect.setText("연결");
                    }
                    break ;
                case R.id.btn_select1 :
                    Intent sub1intent = new Intent(MainActivity.this,Sub1Activity.class);
                    sub1intent.putExtra("dvName",btService.getName());

                    if(strArr[1]!=""){
                    sub1intent.putExtra("RECEIVEMSG",strArr[1]);
                    //strArr[1]="";
                    }

                    startActivity(sub1intent);
                    break ;
                case R.id.btn_select2 :
                    Intent sub2intent = new Intent(MainActivity.this,Sub2Activity.class);
                    startActivity(sub2intent);
                    break ;
                case R.id.btn_select3 :
                    Toast.makeText(getApplicationContext(),"아직 사용되지 않는 버튼입니다.",Toast.LENGTH_SHORT).show();
                    break ;
                case R.id.btn_select4 :
                    Toast.makeText(getApplicationContext(),"아직 사용되지 않는 버튼입니다.",Toast.LENGTH_SHORT).show();
                    break ;
                case R.id.btn_select5 :
                    Toast.makeText(getApplicationContext(),"아직 사용되지 않는 버튼입니다.",Toast.LENGTH_SHORT).show();
                    break ;
                case R.id.btn_select6 :

                    ShowGps();
                    callPermission();
                    break ;

            }
        }
    }
// method
    //bt객체 리턴해주는 method
    public boolean isServiceRunningCheck() {
    ActivityManager manager = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);
        Log.d("test212222222222","");
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        Log.d("test212222222222",""+service.service.getClassName());
        if ("com.example.danhui_cosmos_project.Sub1Activity".equals(service.service.getClassName())) {

            return true;
        }
    }
    return false;
}



    public void onBtnClickSend() {

        String getText =   subAC.etx_send.getText().toString();
        //Log.d("테스트 보네기 메세지",dName);
        if (dName!=""){
            //imm.hideSoftInputFromWindow( new View(this).getWindowToken(), 0);
            Log.d("테스트 보네기 메세지",getText);
            SpannableStringBuilder builder = new SpannableStringBuilder("사용자 : " + getText + "\n");
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#ff0000")), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            subAC.textV(""+builder);
            subAC.sendMsgClear();

            if (getText.equals("")) {
                Toast.makeText(MainActivity.this, "보낼 값이 없습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                buffer = new byte[1024];
                buffer = getText.getBytes();
                btService.writeData(buffer);
            }

        }
        else {
            Toast.makeText(MainActivity.this, "연결된 기기가 없습니다..", Toast.LENGTH_LONG).show();
        }
    }

    //loction convert address
    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
        return nowAddress;
    }

    //bt connect method + receive msg
    private final Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    Toast.makeText(getApplicationContext(), "연결되었습니다.", Toast.LENGTH_SHORT).show();
                    btn_Connect.setText(btService.getName() );
                    SendMs = true;
                    break;
                case BluetoothService.STATE_FAIL:
                    Toast.makeText(getApplicationContext(), "연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    btn_Connect.setText("연결");
                    SendMs = false;
                    break;
            }


            if (SendMs && msg.obj != null) {
                Log.d("핸들러 테스트", "" + msg.obj);
                String stD = "" + (String) msg.obj;
                fullWord = fullWord + stD;
                Log.d("핸들러 총 단어 테스트", "" + fullWord);
                if (fullWord.length() > 0 && fullWord.charAt(fullWord.length() - 1) == '\n') {

                    subAC.textV(btService.getName() + " : " + fullWord);
                    strArr[1] += btService.getName() + " : " + fullWord;
                    fullWord = fullWord.substring(0, fullWord.length() - 1);
                    strArr[0] +=fullWord;


                    if (strArr[0] != null)
                        //textView1.setText("최근 데이터1\n-" + strArr[0]);

                        fullWord = "";
                }


            }


        }

    };

    //realtime method
    @SuppressLint("StaticFieldLeak")
    public void ShowTimeMethod() {

        mTask = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                while (true) {
                    try {
                        publishProgress();
                        Thread.sleep(500);
                        value = calculateCpu();
                        now = System.currentTimeMillis();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


            @SuppressLint("SetTextI18n")
            protected void onProgressUpdate(Void... progress) {
                //textView4.setText("현재시간\n-" + DateFormat.getDateTimeInstance().format(new Date(now)));
                String DataStr = DateFormat.getDateTimeInstance().format(new Date(now)).toString();
                setTimeView(DataStr);
                //여기에 비트맵 이미지 변경하는게 들어가야됨

                // textView2.setText("배터리 잔량\n-" +getBatteryPercentage(getApplicationContext())+"%");
                //progress2.setProgress(getBatteryPercentage(getApplicationContext()));
                // Log.d("테스트","시간이 돌아가는중");


                String st;
                st = String.format("User :%.2f ", value[0]);
                tx_state.setText(st);
                st = String.format("\tNice :%.2f ", value[1]);
                tx_state.append(st);
                st= getRamUsageRate();
                tx_state.append("\tRAM : "+st);
                st = String.format("\nKernel:%.2f ", value[2]);
                tx_state.append(st);
                st = String.format("\tIdle :%.2f ", value[3]);
                tx_state.append(st);


            }

        };
        mTask.execute();
    }

    //배터리 method
    public static int getBatteryPercentage(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;
        return (int)(batteryPct * 100);
    }

    //real time -> main convert method
    public void setTimeView(String DataStr){
        int count=0;
        DataCh = DataStr.substring(14,DataStr.length()).replace(":","").toCharArray();
        int [] DataInt = new int[6];
        Bitmap numBm[] = new Bitmap[6];
       Log.d("넘버 테스트",""+DataStr.substring(14,DataStr.length()).replace(":",""));
        for (int i = 0; i < 6; i++) {
            DataInt[i] = 0;
        }

        if (DataCh[1] == '후') {
            DataInt[0] += 1;
            DataInt[1] += 2;

        }

        if(DataCh.length>=8) {
            //Log.d("값테스트", "");
            int stnum=0;
            int rr=2;
            if(DataCh.length==8 &&DataCh[1]=='전') {
                rr=2;
                stnum=1;
            }
            else if(DataCh.length==9 ) {
                rr=3;
                stnum=0;
            }
            for (int i = stnum; i < 6; i++) {
                int temp = 0;

                if (DataCh[3] != ' ') {
                    temp = (int) DataCh[i+rr] - 48;


                   // Log.d("시간 문자 테스트",i+"/"+temp );

                    if(i>=1){
                        DataInt[i] += temp;
                        //Log.d("DataInt[i] 시간 문자 테스트",DataInt[i]+"" );
                        numBm[i] = LoadNumberFile(DataInt[i]);
                        //Log.d("넘버 테스트2",""+DataInt[i] );
                    }
                    else if(i==0){

                    if(DataCh.length==8 &&DataCh[1]=='전'){
                      //  Log.d("넘버 테스트1",""+DataInt[i] );
                       //DataInt[i] += temp;
                        numBm[i] = LoadNumberFile(DataInt[i]);
                    }
                        if(DataCh.length==8 &&DataCh[1]=='후'){
                            //Log.d("넘버 테스트1",""+DataInt[i] );
                           // DataInt[i] += temp;
                            numBm[i] = LoadNumberFile(DataInt[i]);
                        }
                    else  if(DataCh.length==9 &&DataCh[1]=='전') {
                        DataInt[i] += temp;
                        numBm[i] = LoadNumberFile(DataInt[i]);
                    }
                        else  if(DataCh.length==9 &&DataCh[1]=='후') {
                            DataInt[i] += temp;
                            numBm[i] = LoadNumberFile(DataInt[i]);
                        }
                    }

                    //Log.d("테스트", "" + DataInt[i-1]);


                //0번 전이면 오전 후면 오후

                }
            }
            img_time1.setImageBitmap(numBm[0]);
            img_time2.setImageBitmap(numBm[1]);
            img_time4.setImageBitmap(numBm[2]);
            img_time5.setImageBitmap(numBm[3]);
            img_time6.setImageBitmap(numBm[4]);
            img_time7.setImageBitmap(numBm[5]);
        }

        //1-2 시간
        //3-4 분
        //5-6 초

    }

    //number img load method
    public Bitmap LoadNumberFile(int number){
        String imgpath = "data/data/com.example.danhui_cosmos_project/files/number"+number+".png";
        return BitmapFactory.decodeFile(imgpath);
    }

    //current memory Cal method
    public double[] calculateCpu() {

        long prev_cpu[] = new long[10];

        long cur_cpu[] = new long[10];

        long calc_cpu[] = new long[5];

        double value[] = new double[5];

        long total = 0;

        getCpuStatFromFile(prev_cpu);

        MatrixTime(500);



        getCpuStatFromFile(cur_cpu);

        for (int k = 0; k < 5; k++) {

            calc_cpu[k] = cur_cpu[k] - prev_cpu[k];

            total += calc_cpu[k];

        }


        for (int k = 0; k < 5; k++) {

            value[k] = 100 * calc_cpu[k] / (double) total;

        }

        return value;

    }
    //시간계산? method
    public void MatrixTime(int delayTime) {

        long saveTime = System.currentTimeMillis();

        long currTime = 0;



        while (currTime - saveTime < delayTime) {

            currTime = System.currentTimeMillis();

        }

    }

    //current cpu state get method
    public void getCpuStatFromFile(long[] value) {

        ProcessBuilder cmd;

        String tempStr = null;

        try {

            String[] args = {"/system/bin/cat", "/proc/stat"};

            cmd = new ProcessBuilder(args);

            java.lang.Process process = cmd.start();

            InputStream in = process.getInputStream();

            byte[] re = new byte[1024];

            in.read(re);

            tempStr = new String(re);

            StringTokenizer st = new StringTokenizer(tempStr, " ");

            String arrPrint[] = new String[st.countTokens()];



            int i = 0;

            for (int k = 0; st.hasMoreTokens(); k++) {

                arrPrint[k] = st.nextToken();

                if (k == 0)

                    continue;

                value[i] = Long.parseLong(arrPrint[k]);

                i++;

                if (k == 6)

                    break;

            }



            in.close();

        } catch (IOException ex) {

            ex.printStackTrace();

        }
    }
    //RAM 사용량 구하기
    public String getRamUsageRate(){
        //Ram 사용량 구하기
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(memoryInfo);
        //전체 RAM용량
        double totalMem = memoryInfo.totalMem;  //API 16부터
        //사용가능한 RAM용량
        double availMem = memoryInfo.availMem;

        //사용량
        DecimalFormat df = new DecimalFormat("#,###");
        double ram = 100*(totalMem-availMem)/totalMem;
        return df.format(ram)+"%";
    }
    //저장된 파일불러오기(지정 경로)
    public void LoadBitmaFile(int i){
        try{
            String imgpath="";
            if(i==0)
                imgpath = "data/data/com.example.hoon.myapplication/files/background1.png";
            else if(i==1)
                imgpath = "data/data/com.example.hoon.myapplication/files/background2.png";
            else if(i==2)
                imgpath = "data/data/com.example.hoon.myapplication/files/background3.png";

            Bitmap bm = BitmapFactory.decodeFile(imgpath);

            //receiveString.append("현재 이미지 파일이 저장된 경로 : "+imgpath+"\n");
            //Toast.makeText(getApplicationContext(), "load ok", Toast.LENGTH_SHORT).show();
        }catch(Exception e){Toast.makeText(getApplicationContext(), "load error", Toast.LENGTH_SHORT).show();}

    }
    //GPS 관련 method
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);


        switch (requestCode) {
            /** �߰��� �κ� ���� **/
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    btService.getDeviceInfo(data);
                    Log.d("test11", "get Device");
                    if (btService.getDeviceState()) {
                        Toast.makeText(this, "연결중입니다..", Toast.LENGTH_LONG).show();

                    }
                }
                break;
            /** �߰��� �κ� �� **/
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Next Step
                    btService.scanDevice();
                    if (btService.getDeviceState()) {
                        Toast.makeText(this, "연결중입니다..", Toast.LENGTH_LONG).show();

                    }

                } else {

                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }

    }
    //GPS 관련 method
    public void ShowGps() {
        // 권한 요청을 해야 함
        Log.d("gps","함수테스트");
        gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            //Log.d("GPS 함수 테스트","되고 있는중");
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            tx_location.setText("위도 : " + String.valueOf(latitude) + "\n" +
                    "경도 : " + String.valueOf(longitude) + "\n" +
                    "주소 : " + getAddress(getApplicationContext(), latitude, longitude));

        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }



    }


    //GPS 관련 method
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        }
        if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }
    //GPS 관련 method
    // 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }



    //file save method
    public void SaveBitmapFile(){
        //파일저장하는 구간

        /*Bitmap bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.main_background);
        Bitmap bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.main_background2);
        Bitmap bm3 = BitmapFactory.decodeResource(getResources(), R.drawable.main_background3);*/
        Bitmap bm4;


        try{

           /* File file = new File("background1.png");
            FileOutputStream fos = openFileOutput("background1.png" , 0);
            bm1.compress(Bitmap.CompressFormat.PNG, 100 , fos);

            fos.flush();
            fos.close();

            File file2 = new File("background2.png");
            FileOutputStream fos2 = openFileOutput("background2.png" , 0);
            bm2.compress(Bitmap.CompressFormat.PNG, 100 , fos2);

            fos2.flush();
            fos2.close();

            File file3 = new File("background3.png");
            FileOutputStream fos3 = openFileOutput("background3.png" , 0);
            bm3.compress(Bitmap.CompressFormat.PNG, 100 , fos3);

            fos3.flush();
            fos3.close();*/
            File filearr[] = new File[10];
            for(int i = 0;i<10;i++) {
                bm4 = BitmapFactory.decodeResource(getResources(), R.drawable.number0+i);

                filearr[i] =new File("number"+i+".png");
                FileOutputStream fos4 = openFileOutput("number"+i+".png", 0);
                bm4.compress(Bitmap.CompressFormat.PNG, 100, fos4);
                fos4.flush();
                fos4.close();
            }
            //Toast.makeText(this, "file ok", Toast.LENGTH_SHORT).show();
        }catch(Exception e) { Toast.makeText(this, "file error", Toast.LENGTH_SHORT).show();}



    }

}