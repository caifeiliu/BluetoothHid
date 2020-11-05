package com.dten.hidroid;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;


import com.dten.hidroid.mouse.MouseUtils;

import java.util.Date;
import java.util.TimerTask;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;

public class MainActivity extends BaseActivity implements View.OnTouchListener,View.OnClickListener{
    public static final String TAG = "hid_MainActivity";
    public static final int REQUEST_SELECT_DEVICE = 1;
    public static final int REQUEST_SELECT_PAIREDDEVICE = 2;
    TextView stateTv;
    TextView mouse;
    Button mMouseLeft;
    Button mMouseRight;
    Button mMouseMiddle;
    Toolbar toolbar;
    ImageView mSwitchKeyboard;
    ImageView mBTController;
    MouseUtils mMouseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mMouseUtils = new MouseUtils();
        initView();

        HidUitls.RegistApp(getApplicationContext());
        HidConsts.reporttrans(getApplicationContext());
    }

    private void initView(){
        mSwitchKeyboard = findViewById(R.id.switch_keyboard);
        mMouseLeft = findViewById(R.id.mouse_left_button);
        mMouseRight = findViewById(R.id.mouse_right_button);
        mMouseMiddle = findViewById(R.id.mouse_middle);
        mBTController = findViewById(R.id.bluetooth_status);
        mouse = findViewById(R.id.mouse_move_pad);
        mSwitchKeyboard.setOnClickListener(this);
        mBTController.setOnClickListener(this);
        mouse.setOnTouchListener(this);
        mMouseLeft.setOnTouchListener(this);
        mMouseRight.setOnTouchListener(this);
        mMouseMiddle.setOnTouchListener(this);
        registerReceiver(bluetoothReceiver, makePairIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume :"+ HidConsts.HidDevice +" HidUitls.IsConnected():"+ HidUitls.IsConnected());
        if(HidConsts.HidDevice != null) {

            HidUitls.ReConnect(this);
            HidUitls.HidDevice = HidConsts.HidDevice;
            HidUitls.BtDevice = HidConsts.BtDevice;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE: //When the ActivityDeviceList return, with the selected device address
            case REQUEST_SELECT_PAIREDDEVICE:

                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.e("franc","resoult from ActivityDeviceList: "+deviceAddress);
                    HidUitls.SelectedDeviceMac = deviceAddress;
                    if(HidUitls.Pair(deviceAddress)){
                        HidUitls.Connect(deviceAddress);
                    }
                }
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            Log.e(TAG,"action"+action);
            try {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                } else if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                    if(HidUitls.BtDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                        HidUitls.Connect(HidUitls.BtDevice);
                    }else if(HidUitls.BtDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    }else if(HidUitls.BtDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    }
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };
    private static IntentFilter makePairIntentFilter() {
        final IntentFilter filter=new IntentFilter();
        filter.addAction("android.bluetooth.device.action.FOUND");
        filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        return filter;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = false;
        switch (v.getId()){
            case R.id.mouse_move_pad:
                ret = mMouseUtils.mouseMove(event);
                break;
            case R.id.mouse_left_button:
                ret =  mMouseUtils.mouseLeft(event);
                break;
            case R.id.mouse_right_button:
                ret =  mMouseUtils.mouseRight(event);
                break;
            case R.id.mouse_middle:
                ret =  mMouseUtils.mouseMiddle(event);
                break;
        }
        return ret;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_keyboard:
                Intent keyboardIntent = new Intent(getBaseContext(), activity_keyboard_full.class);
                startActivity(keyboardIntent);
                break;
            case R.id.bluetooth_status:
                Intent BTControllerIntent = new Intent(getBaseContext(), ActivityDeviceList.class);
                startActivityForResult(BTControllerIntent, REQUEST_SELECT_DEVICE);
                break;
        }

    }
}
