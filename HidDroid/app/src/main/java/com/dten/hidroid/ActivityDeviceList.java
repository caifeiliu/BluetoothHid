/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dten.hidroid;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dten.hidroid.HidUitls.Device;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ActivityDeviceList extends Activity implements DeviceAdapter.ItemClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning = false;
    private TextView mEmptyList;
    public static final String TAG = "Hid-ActivityDeviceList";
    public static String SelectedDeviceMac = "";
    private DeviceAdapter mNewdeviceAdapter, mConnectAdapter;
    private static final long SCAN_PERIOD = 13000; //10 seconds
    LinkedList<BluetoothDevice> deviceList;
    Map<String, Integer> devRssiValues;
    private static final int MAX_LISTVIEW = 6;
    private final static int MSG_DISCOVERY = 0;
    private ListView mListNewDevices;
    private ListView mListConnectDevices;
    private CustomProgressDialog progressDialog;
    ImageView imageView;
    TextView mStatus;
    public final static String BOND_REASON = "android.bluetooth.device.extra.REASON";

    private ArrayList<HidUitls.Device> mPresentDevices = new ArrayList<>();

    CommonInterface.ConnectionStateChangeListener connectionStateChangeListener = new CommonInterface.ConnectionStateChangeListener() {
        @Override
        public void onConnecting() {
            mStatus.setText("Status:Connecting...");
        }

        @Override
        public void onConnected() {
            mStatus.setText("Status:Connected");
        }

        @Override
        public void onDisConnected() {
            mStatus.setText("Status:Disconnected");
        }
    };

    private Handler mH = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DISCOVERY:
                    setDiscoverableTimeout(30000);
                    scanDevice(true);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.activity_titlebar_scan);
        setContentView(R.layout.activity_device_list);
        imageView = findViewById(R.id.new_about);
        mStatus = findViewById(R.id.bt_status);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP;
        float scaledDensity = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        float xdpi = getApplicationContext().getResources().getDisplayMetrics().xdpi;
        float ydpi = getApplicationContext().getResources().getDisplayMetrics().ydpi;
        int width = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        int height = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        Log.e("franc", "ActivityDeviceList: scaledDensity: " + scaledDensity + " density: " + density + "  xdpi:" + xdpi + " ydpi: " + ydpi + " width: " + width + " height:" + height);
        layoutParams.y = 100;
        layoutParams.width = width / 4;
        //layoutParams.height = height/2;

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Log.e(TAG, "do not support the Ble feature");
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        //final BluetoothManager bluetoothManager =
        //(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        //mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "do not support bluetooth this device");
            finish();
            return;
        }
        mBluetoothAdapter.setName("DTEN_Mate");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothHidDevice.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(mReceiver, filter);

        populateList();

        mEmptyList = (TextView) findViewById(R.id.new_scanStateBar);
        HidUitls.connectionStateChangeListener = connectionStateChangeListener;
//        Button cancelButton = (Button) findViewById(R.id.btn_cancel);
//        cancelButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mScanning==false) scanDevice(true);
//                else finish();
//            }
//        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        getDefaultDevicesStatus();

    }
    private void addDevPaire(int state,BluetoothDevice dev){
        BtItemBean btUseItem=findItemByList(mConnectAdapter.getData(),dev);
        if(btUseItem!=null){
            btUseItem.setBluetoothDevice(dev);
        }else{
            BtItemBean bluetoothItem=createBluetoothItem(dev);
            bluetoothItem.setState(state);
            mConnectAdapter.add(0,bluetoothItem);
        }
        mConnectAdapter.notifyDataSetChanged();
    }

    private void getDefaultDevicesStatus(){//以配对设备
        List<BluetoothDevice> bluetoothDeviceSet= HidUitls.getConnectedDevices();
        if(bluetoothDeviceSet!=null&&bluetoothDeviceSet.size()>0){
            for(BluetoothDevice device:bluetoothDeviceSet){
                addDevPaire(BtItemBean.STATE_CONNECTED,device);
            }
        }
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        View itemView = listAdapter.getView(0, null, listView);
        itemView.measure(0, 0);
        int itemHeight = itemView.getMeasuredHeight();
        int itemCount = listAdapter.getCount();
        LinearLayout.LayoutParams layoutParams = null;
        if (itemCount <= MAX_LISTVIEW) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * itemCount);
        } else if (itemCount > MAX_LISTVIEW) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * MAX_LISTVIEW);
        }
        listView.setLayoutParams(layoutParams);
    }

    private void populateList() {
        /* Initialize device list container */
        deviceList = new LinkedList<>();
        mNewdeviceAdapter = new DeviceAdapter(this);
        mConnectAdapter = new DeviceAdapter(this);
        devRssiValues = new HashMap<>();
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_upload_progress);
        mListNewDevices = findViewById(R.id.new_devices);
        mListNewDevices.setAdapter(mNewdeviceAdapter);
        mListConnectDevices = findViewById(R.id.connect_devices);
        mListConnectDevices.setAdapter(mConnectAdapter);
        // mListNewDevices.setOnItemClickListener(mDeviceClickListener);
        imageView.startAnimation(animation);
        mNewdeviceAdapter.setItemClickListener(this);
        mConnectAdapter.setItemClickListener(this);
        scanDevice(true);

    }

    private BtItemBean findItemByList(List<BtItemBean> datas, BluetoothDevice dev) {
        if (datas == null || datas.size() < 1) {
            return null;
        }
        for (BtItemBean btItemBean : datas) {
            if (!TextUtils.isEmpty(dev.getAddress()) && dev.getAddress().equals(btItemBean.getBluetoothDevice().getAddress())) {
                return btItemBean;
            }
        }
        return null;
    }

    private BtItemBean createBluetoothItem(BluetoothDevice device) {
        BtItemBean btItemBean = new BtItemBean();
        btItemBean.setBluetoothDevice(device);
        return btItemBean;
    }

    //定义广播接收
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, action);
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (dev == null) return;
                String address = dev.getAddress();
                String name = dev.getName();
                Log.e(TAG,"device found: address"+address+ " name: " +name);

                if (address == null || name == null) {
                    return;
                }
                if(HidUitls.getAllConnectedDevices().contains(dev)){
                    Log.e(TAG,"device is connect or bond ");
                    return;
                }
                Device device = null;
                final int N = mPresentDevices.size();
                for (int i = 0; i < N; i++) {
                    final Device d = mPresentDevices.get(i);
                    if (address.equals(d.address)) {
                        device = d;
                    }
                }

                if (device == null) {
                    Log.e(TAG, "Device is new device");
                    device = new Device();
                    device.btDevice = dev;
                    device.btname = name;
                    device.address = address;
                    mPresentDevices.add(device);
                }
//                if(mBTScanListener!=null){
//                    mBTScanListener.onFindDevice(device);
//                }
                addDevUse(device.btDevice);

//                if(device != null && device.getName() != null ){
//                    addDevice(device,0);
//                }
//                if(device.getBondState()==BluetoothDevice.BOND_BONDED)
//                {    //显示已配对设备
//                    //text.append("\n"+device.getName()+"==>"+device.getAddress()+"\n");
//                }else if(device.getBondState()!=BluetoothDevice.BOND_BONDED)
//                {
//                    //text3.append("\n"+device.getName()+"==>"+device.getAddress()+"\n");
//                }

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mH.sendEmptyMessageDelayed(MSG_DISCOVERY,10000);
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int newState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
                int oldState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, 0);
                int reason = 0;
                if (newState == BluetoothDevice.BOND_NONE) {
                    reason = intent.getIntExtra(BOND_REASON, 0);
                }
                Log.i(TAG, "address: " + dev.getAddress() + "设备配对状态改变：" + dev.getBondState() + " newState:" + newState + "  oldstate: " + oldState + " reason: " + reason);

                onBondStateChange(dev);

            } else if (action.equals(BluetoothHidDevice.ACTION_CONNECTION_STATE_CHANGED)) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (intent.getIntExtra(BluetoothHidDevice.EXTRA_STATE, -1)) {
                    case BluetoothHidDevice.STATE_CONNECTING:
                        Log.i(TAG,"hid device: " + dev.getName() + " connecting");
                        paireDevStateChange(BtItemBean.STATE_CONNECTING,dev);
                        break;
                    case BluetoothHidDevice.STATE_CONNECTED:
                        Log.i(TAG,"hid device: " + dev.getName() + " connected");
                        paireDevStateChange(BtItemBean.STATE_CONNECTED,dev);

                        break;
                    case BluetoothHidDevice.STATE_DISCONNECTING:
                        Log.i(TAG,"hid device: " + dev.getName() + " disconnecting");
                        paireDevStateChange(BtItemBean.STATE_DISCONNECTING,dev);
                        break;
                    case BluetoothHidDevice.STATE_DISCONNECTED:
                        Log.i(TAG,"hid device " + dev.getName() + " disconnected");
                        useDevStateChange(BtItemBean.STATE_DISCONNECTED,dev);
                        removeBond(dev);


                        break;
                    default:
                        break;
                }
            }else if(action.equals(Intent.ACTION_SHUTDOWN)){
                Log.i(TAG, "receive the shutdown broadcast");
                List<BluetoothDevice> connectDevices = HidUitls.getConnectedDevices();
                for(BluetoothDevice device: connectDevices) {
                    HidUitls.disConnect(device);
                }
            }

        }
    };

    public boolean removeBond(BluetoothDevice device) {//取消配对
        Class btDeviceCls = BluetoothDevice.class;
        Method removeBond = null;

        try {
            removeBond = btDeviceCls.getMethod("removeBond");
            removeBond.setAccessible(true);
            Log.e(TAG,"into removeBond");
            return (boolean) removeBond.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void paireDevStateChange(int state, BluetoothDevice dev) {
        BtItemBean btUseItem = findItemByList(mNewdeviceAdapter.getData(), dev);
        BtItemBean btPaireItem = findItemByList(mConnectAdapter.getData(), dev);
        if (btUseItem != null) {
            btUseItem.setState(state);
            btUseItem.setBluetoothDevice(dev);
            mNewdeviceAdapter.remove(btUseItem);
            mNewdeviceAdapter.notifyDataSetChanged();
            if (btPaireItem != null) {
                mConnectAdapter.remove(btPaireItem);
            }
            mConnectAdapter.add(0, btUseItem);
        } else if (btPaireItem != null) {
            btPaireItem.setState(state);
            btPaireItem.setBluetoothDevice(dev);
        } else {
            BtItemBean bluetoothItem = createBluetoothItem(dev);
            bluetoothItem.setState(state);
            mConnectAdapter.add(0, bluetoothItem);
        }
        mConnectAdapter.notifyDataSetChanged();
    }

    private void useDevStateChange(int state, BluetoothDevice dev) {
        BtItemBean btUseItem = findItemByList(mNewdeviceAdapter.getData(), dev);
        BtItemBean btPaireItem = findItemByList(mConnectAdapter.getData(), dev);
        if (btPaireItem != null) {
            btPaireItem.setState(state);
            btPaireItem.setBluetoothDevice(dev);
            mConnectAdapter.remove(btPaireItem);
            mConnectAdapter.notifyDataSetChanged();
            if (btUseItem != null) {
                mNewdeviceAdapter.remove(btUseItem);
            }
            mNewdeviceAdapter.add(0, btPaireItem);
        } else if (btUseItem != null) {
            btUseItem.setState(state);
            btUseItem.setBluetoothDevice(dev);
        } else {
            BtItemBean bluetoothItem = createBluetoothItem(dev);
            bluetoothItem.setState(state);
            mNewdeviceAdapter.add(0, bluetoothItem);
        }
        mNewdeviceAdapter.notifyDataSetChanged();
    }

    public void onBondStateChange(BluetoothDevice dev) {
        if (dev.getBondState() == BluetoothDevice.BOND_BONDED) {//已配对
            paireDevStateChange(BtItemBean.STATE_BONDED, dev);
            HidUitls.Connect(dev);
            // mBluetoothHelper.connect(dev);
        } else if (dev.getBondState() == BluetoothDevice.BOND_BONDING) {//配对中
            useDevStateChange(BtItemBean.STATE_BONDING, dev);
        } else {//未配对
                /*
                switch (reason){
                    case UNBOND_REASON_REMOTE_DEVICE_DOWN:
                    case UNBOND_REASON_REMOTE_AUTH_CANCELED:
                    case UNBOND_REASON_DISCOVERY_IN_PROGRESS:
                    case UNBOND_REASON_AUTH_REJECTED:
                    case UNBOND_REASON_AUTH_CANCELED:
                    case UNBOND_REASON_AUTH_TIMEOUT:
                        case UNBOND_REASON_PIN_REGECTED:
                    case UNBOND_REASON_REPEATED_ATTEMPTS:
                    case UNBOND_REASON_REMOVED:
                        mBluetoothHelper.createBond(dev);
                        break;

                }

                 */

            BtItemBean btUseItem = findItemByList(mNewdeviceAdapter.getData(), dev);
            if (btUseItem != null && btUseItem.getState() == BtItemBean.STATE_BONDING) {
                //Toast.makeText(getApplicationContext(),"请确认配对设备已打开且在通信范围内",Toast.LENGTH_SHORT).show();
            }
            useDevStateChange(BtItemBean.STATE_BOND_NONE, dev);
        }
    }

    public void setDiscoverableTimeout(int timeout) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanDevice(final boolean enable) {
        if (enable) {
            Log.d(TAG, "scanDevice");

            if (mBluetoothAdapter.isDiscovering()) {
                return;
            }

            mScanning = true;
            //mBluetoothAdapter.startLeScan(mLeScanCallback);
            mBluetoothAdapter.startDiscovery();
            // cancelButton.setText("Discovering");

        } else {
            if (!mBluetoothAdapter.isDiscovering()) {
                return;
            }
            mScanning = false;
            mBluetoothAdapter.cancelDiscovery();
            //cancelButton.setText("Discovery");
        }
    }

    private void stopScan() {
        scanDevice(false);
    }

    private void addDevUse(BluetoothDevice dev) {
        for (BtItemBean btItemBean : mNewdeviceAdapter.getData()) {
            //Log.e(TAG, "btItemBean.getBluetoothDevice(): " + btItemBean.getBluetoothDevice().getAddress());
        }
        BtItemBean btUseItem = findItemByList(mNewdeviceAdapter.getData(), dev);
        if (btUseItem != null) {
            btUseItem.setBluetoothDevice(dev);
        } else {
            BtItemBean bluetoothItem = createBluetoothItem(dev);

            if(dev.getBondState()==BluetoothDevice.BOND_BONDED){
                bluetoothItem.setState(BtItemBean.STATE_BONDED);
            }else
            if (dev.getBondState() == BluetoothDevice.BOND_BONDING) {
                bluetoothItem.setState(BtItemBean.STATE_BONDING);
            }
            mEmptyList.setVisibility(View.GONE);
            mNewdeviceAdapter.add(0, bluetoothItem);
        }
        try {
            setListViewHeight(mListNewDevices);
        } catch (Exception e) {
            Log.e(TAG, "setListViewHeight failed" + e);
        }
        mNewdeviceAdapter.notifyDataSetChanged();
    }

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }

        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
            mEmptyList.setVisibility(View.GONE);

            mNewdeviceAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        //mScanning = false;
        //mBluetoothAdapter.stopScan();
        // stopScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mScanning = false;
        //mBluetoothAdapter.stopScan();
        stopScan();
        //解除注册
        unregisterReceiver(mReceiver);
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            BluetoothDevice device = deviceList.get(position);
//            if(device.getBondState() == BluetoothDevice.BOND_NONE){
//                device.createBond();
//            }
//            //stopScan();
//            Bundle b = new Bundle();
//            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
//            ActivityDeviceList.SelectedDeviceMac = deviceList.get(position).getAddress();
//            //UtilCls.SLog(TAG,"bluetooth address:"+deviceList.get(position).getAddress());
//            Intent result = new Intent();
//            result.putExtras(b);
//            setResult(Activity.RESULT_OK, result);
//            finish();
            //UtilCls.SLog(TAG,device.getName() + "   " + device.getAddress());

        }


    };

    protected void onPause() {
        super.onPause();
        scanDevice(false);
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClickListener(BtItemBean btItemBean) {

        BluetoothDevice bluetoothDevice = btItemBean.getBluetoothDevice();
        String deviceAddress = bluetoothDevice.getAddress();
        Log.e(TAG, "into onIntemClickLister: " + btItemBean.getState() + " name :" + bluetoothDevice.getName()+ " address: "+bluetoothDevice.getAddress());
        switch (btItemBean.getState()) {
            case BtItemBean.STATE_UNCONNECT://未连接
            case BtItemBean.STATE_BOND_NONE://未配对
                Log.e(TAG, "into onIntemClickListe create "+bluetoothDevice.getBluetoothClass().getDeviceClass());
                List<BluetoothDevice> devices = HidUitls.getConnectedDevices();
                for(BluetoothDevice device :devices){
                    if(!bluetoothDevice.equals(device)){
                        Log.e(TAG, "into disconnect device: "+ device.getName());
                        HidUitls.disConnect(device);
                    }
                }
                bluetoothDevice.createBond();
                HidUitls.SelectedDeviceMac = deviceAddress;
                break;
            case BtItemBean.STATE_BONDING://配对中
                break;
            case BtItemBean.STATE_BONDED://已配对

                 HidUitls.Connect(bluetoothDevice);
                break;
            case BtItemBean.STATE_CONNECTING://连接中
                break;
            case BtItemBean.STATE_CONNECTED://已连接
                HidUitls.disConnect(bluetoothDevice);
                break;
            case BtItemBean.STATE_DISCONNECTING://断开中
                break;
            case BtItemBean.STATE_DISCONNECTED://已断开(但还保存)

                break;
        }

    }
}
