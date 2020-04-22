package com.example.mr_admin_forhanddy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Button button1; //버튼들
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    Button button10;
    Button button11;
    Button button12;
    Button button13;
    Button button14;

    // 블루투스 기능
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSerialService mSerialService = null;


    private TransmitThread mTransmitThread;

    private static final boolean D = false;
    private boolean mBtnStatusChanged = false;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private int mBtnStatus = 0;
    public static final int PACKET0_START = 0xf5;
    public static final int TRANSMIT_INTERVAL = 50;


    // 아두이노에 전송할 코드
    public static final int Mtn_A = 0x1;   //베이스 정
    public static final int Mtn_B = 0x2;
    public static final int Mtn_C = 0x4;   //링크 1 정
    public static final int Mtn_CA = 0x8;  //링크 1 역
    public static final int Mtn_2F = 0x10;  //링크 2
    public static final int MTR_2B = 0x20;
    public static final int MTR_FS = 0x40;  // 링크3
    public static final int MTR_3F = 0x100;
    public static final int MTR_3B = 0x200; // 엔드
    public static final int MTR_4F = 0x400;
    public static final int MTR_4B = 0x800;
    public static final int MTR_5F = 0x1000;
    public static final int MTR_5B = 0x2000;
    public static final int MTR_SS = 0x4000;

    private String mConnectedDeviceName = null;

    //수신버퍼
    char mCharDelimiter = '\n';
    String mStrDelimiter = "\n";
    BluetoothSocket mSocket = null;
    InputStream mInputStream = null;
    byte[] readBuffer;
    int readBufferPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        button10  = findViewById(R.id.button10);
        button11 = findViewById(R.id.button11);
        button12 = findViewById(R.id.button12);
        button13 = findViewById(R.id.button13);
        button14 = findViewById(R.id.button14);

        /*
        //라디오 버튼 클릭 리스너 (중복 가능)
        radiobutton1.setOnClickListener(radioButtonClickListener);
        radiobutton2.setOnClickListener(radioButtonClickListener);
        */


        // 안드로이드 안에 있는 블루투스 사용
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 블루투스 기능이 없으면 토스트 출력
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    // 주문 버튼 클릭시 작동하는 매서드
    private void setupSerial() {
        Log.d(TAG, "setupSerial()");

        button1.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= Mtn_A;
                        mBtnStatusChanged = true;
                        button1.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~Mtn_A;
                        mBtnStatusChanged = true;
                        button1.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button2.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= Mtn_B;
                        mBtnStatusChanged = true;
                        button2.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~Mtn_B;
                        mBtnStatusChanged = true;
                        button2.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button3.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= Mtn_C;
                        mBtnStatusChanged = true;
                        button3.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~Mtn_C;
                        mBtnStatusChanged = true;
                        button3.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button4.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= Mtn_CA;
                        mBtnStatusChanged = true;
                        button4.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~Mtn_CA;
                        mBtnStatusChanged = true;
                        button4.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button5.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= Mtn_2F;
                        mBtnStatusChanged = true;
                        button5.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~Mtn_2F;
                        mBtnStatusChanged = true;
                        button5.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button6.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_2B;
                        mBtnStatusChanged = true;
                        button6.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_2B;
                        mBtnStatusChanged = true;
                        button6.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button7.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_FS;
                        mBtnStatusChanged = true;
                        button7.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_FS;
                        mBtnStatusChanged = true;
                        button7.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button8.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_3F;
                        mBtnStatusChanged = true;
                        button8.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_3F;
                        mBtnStatusChanged = true;
                        button8.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button9.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_3B;
                        mBtnStatusChanged = true;
                        button9.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_3B;
                        mBtnStatusChanged = true;
                        button9.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button10.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_4F;
                        mBtnStatusChanged = true;
                        button10.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_4F;
                        mBtnStatusChanged = true;
                        button10.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button11.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_4B;
                        mBtnStatusChanged = true;
                        button11.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_4B;
                        mBtnStatusChanged = true;
                        button11.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button12.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_5F;
                        mBtnStatusChanged = true;
                        button12.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_5F;
                        mBtnStatusChanged = true;
                        button12.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button13.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_5B;
                        mBtnStatusChanged = true;
                        button13.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_5B;
                        mBtnStatusChanged = true;
                        button13.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        button14.setOnTouchListener(new Button.OnTouchListener() { //삭제
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBtnStatus |= MTR_SS;
                        mBtnStatusChanged = true;
                        button14.setBackgroundColor(Color.RED);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mBtnStatus &= ~MTR_SS;
                        mBtnStatusChanged = true;
                        button14.setBackgroundColor(0xffaaaaaa);
                        return true;
                }
                return false;
            }
        });

        Handler handler = StaticHandlerFactory.create(newHandler);
        mSerialService = new BluetoothSerialService(this, handler);
    }

    ///////////////////////
    // 수정할 필요 없음 ↓

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        if (mTransmitThread == null) {
            mTransmitThread = new TransmitThread();
            mTransmitThread.start();
        }

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mSerialService == null) setupSerial();
        }

//        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    // 아두이노로 코드를 전송하는 클래스(블루투스 통신)
    private class TransmitThread extends Thread {
        byte[] packet0 = new byte[4];
        int pauseCnt = 0;

        public void run() {
            setName("RepeatThread");

            while (true){
                if(mBtnStatusChanged==true){
                    if (mSerialService.getState() == BluetoothSerialService.STATE_CONNECTED) {
                        packet0[0] = (byte) PACKET0_START;
                        packet0[1] = (byte) (mBtnStatus & 0xff);
                        packet0[2] = (byte) (mBtnStatus >> 8);
                        packet0[3] = (byte) ((packet0[1] + packet0[2]) & 0x7f);

                        Log.e(TAG, "아두이노로 전송되는 코드 값 : " + String.valueOf(mBtnStatus));
                        mSerialService.write(packet0);
                        //mBtnStatus = 0;
                    }
                    if (mBtnStatus == 0){
                        if(++pauseCnt >= 10){
                            pauseCnt = 10;
                            mBtnStatusChanged = false;
                        }
                    } else {
                        pauseCnt = 0;
                    }
                    try {
                        Thread.sleep(TRANSMIT_INTERVAL - 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try{
                    Thread.sleep(1);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mSerialService != null) mSerialService.stop();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }


    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
//{{-Jjeong
//        if (mSerialService != null) {
        if ((mSerialService != null) && (mBluetoothAdapter.isEnabled())) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
                // Start the Bluetooth chat services
                mSerialService.start();
            }
        }
    }


    /////////////////
    // 액션바 부분 ↓

    // The Handler that gets information back from the BluetoothSerialService
//{{-Jjeong
//    private Handler mHandler = new Handler() {
    private IStaticHandler newHandler = new IStaticHandler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothSerialService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothSerialService.STATE_LISTEN:
                        case BluetoothSerialService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    private final void setStatus(int resId) {
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // 블루투스와 연결시 작동되는 코드
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupSerial();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    try {
                        mInputStream = mSocket.getInputStream();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                        finish();  // App 종료
                    }
                    finish();
                }
        }
    }

    // 디바이스와 블루투스 연결을 할때
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mSerialService.connect(device, secure);
    }

    // 액션바에 버튼을 추가
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }


    // 액션바의 버튼을 눌렀을때 작동하는 코드를 입력하는 부분
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

}