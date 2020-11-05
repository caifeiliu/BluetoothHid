package com.dten.hidroid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class HidUitls {
    public static final String TAG = "Hid-HidUitls";
    public static String SelectedDeviceMac = "";
    public static boolean _connected = false;
    public static boolean IsRegisted = false;
    public static CommonInterface.ConnectionStateChangeListener connectionStateChangeListener;

    static BluetoothAdapter mBluetoothAdapter;
    static BluetoothProfile bluetoothProfile;
    static BluetoothDevice BtDevice;
    static BluetoothHidDevice HidDevice;
    public static class Device{
        public BluetoothDevice btDevice;
        public String address;
        public String btname;
    }
    public static void RegistApp(Context context){
        if(IsRegisted) { }
        else{ BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, mProfileServiceListener,BluetoothProfile.HID_DEVICE); }
    }

    public static boolean Pair(String deviceAddress){
        Log.e("franc","into Pair");
        if(BluetoothAdapter.checkBluetoothAddress(deviceAddress)){
            try {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(BtDevice == null){
                    BtDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                }
                if(BtDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.e("franc","resoult from ActivityDeviceList: Pair");
                    BtDevice.createBond();
                    return false;
                }else if(BtDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    return true;
                }else if(BtDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    return false;
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return false;
    }

    public static List<BluetoothDevice> getAllConnectedDevices(){
        List<BluetoothDevice> devices = new ArrayList<>();
        if (mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondDevices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> bondList = new ArrayList<BluetoothDevice>(bondDevices);
        for (BluetoothDevice dev : bondList) {
            Log.e(TAG, "bond all bond:" + dev.getName());
        }
        if (bondDevices != null && bondDevices.size() > 0) {
            devices.addAll(bondList);
        }


        if (HidDevice != null) { ;
            List<BluetoothDevice> deviceList = HidDevice.getConnectedDevices();
            for(BluetoothDevice dev:deviceList){
                Log.e(TAG,"name all connected:" + dev.getName());
            }
            if (deviceList != null && deviceList.size() > 0) {
                devices.addAll(deviceList);
            }

        }
        return devices;
    }
    public static List<BluetoothDevice> getBondDevices(){
        List<BluetoothDevice> devices = new ArrayList<>();
        if(mBluetoothAdapter != null){
            Set<BluetoothDevice>  bondDevices =  mBluetoothAdapter.getBondedDevices();
            List<BluetoothDevice> bondList = new ArrayList<BluetoothDevice>(bondDevices);
            for(BluetoothDevice dev:bondList){
                Log.e(TAG,"bond name:" + dev.getName());
            }
            if(bondDevices !=null && bondDevices.size() >0){
                devices.addAll(bondList);
            }
        }
        return devices;
    }
    public static List<BluetoothDevice> getConnectedDevices(){
        List<BluetoothDevice> devices = new ArrayList<>();
        if (HidDevice != null) { ;
            List<BluetoothDevice> deviceList = HidDevice.getConnectedDevices();
            for(BluetoothDevice dev:deviceList){
                Log.e(TAG,"name connected:" + dev.getName());
            }
            if (deviceList != null && deviceList.size() > 0) {
                devices.addAll(deviceList);
            }

        }
        return devices;
    }

    public static  boolean removeBond(BluetoothDevice device) {//取消配对
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

    public static boolean IsConnected() {
        return HidUitls._connected;
    }

    private static void IsConnected(boolean _connected) {
        HidUitls._connected = _connected;
    }

    public static boolean Connect(String deviceAddress){
        if(TextUtils.isEmpty(deviceAddress)){return false;}
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(BtDevice == null){
            BtDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        }
        boolean ret = HidDevice.connect(BtDevice);
        HidConsts.BtDevice = BtDevice;
        HidConsts.HidDevice = HidDevice;
        return ret;
    }
    public static boolean Connect(BluetoothDevice device){
        Log.e(TAG,"Connect: device: "+ device.getName());
        boolean ret = HidDevice.connect(device);
        HidConsts.BtDevice = device;
        HidConsts.HidDevice = HidDevice;
        return ret;
    }
    public static boolean disConnect(BluetoothDevice device){
        boolean ret = HidDevice.disconnect(device);
        Log.e(TAG,"disConnect: "+ ret );
//        HidConsts.BtDevice = device;
//        HidConsts.HidDevice = HidDevice;
        return ret;
    }

    public static void ReConnect(final Activity context){
        try {
            Log.e(TAG,"ReConnect: "+ HidUitls.HidDevice +"HidUitls.BtDevice : "+ HidUitls.BtDevice);
            if (HidUitls.HidDevice != null) {
                if (HidUitls.BtDevice == null) {
                    HidUitls.BtDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(HidUitls.SelectedDeviceMac);
                }
                int state = HidUitls.HidDevice.getConnectionState(HidUitls.BtDevice);
                Log.e(TAG,"state: "+state + "HidUitls.SelectedDeviceMac");
                if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    if (TextUtils.isEmpty(HidUitls.SelectedDeviceMac)) {
                    } else {
                        if (HidUitls.Pair(HidUitls.SelectedDeviceMac)) {
                            HidUitls.RegistApp(context.getApplicationContext());
                            UtilCls.DelayTask(new Runnable() {
                                @Override
                                public void run() {
                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            HidUitls.Connect(HidUitls.SelectedDeviceMac);
                                        }
                                    });
                                }
                            }, 500, true);
                        }
                    }
                }
            }
        }catch (Exception ex){ }
    }

    public static BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
            Log.e(TAG,"hid onServiceDisconnected");
            if (profile == BluetoothProfile.HID_DEVICE) {
                HidDevice.unregisterApp();
            }
        }
        @SuppressLint("NewApi") @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.e(TAG,"hid onServiceConnected");
            bluetoothProfile = proxy;
            if (profile == BluetoothProfile.HID_DEVICE) {
                HidDevice = (BluetoothHidDevice) proxy;
                HidConsts.HidDevice = HidDevice;
                BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(HidConsts.NAME, HidConsts.DESCRIPTION, HidConsts.PROVIDER,BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.Descriptor);
                HidDevice.registerApp(sdp, null, null, Executors.newCachedThreadPool(), mCallback);
            }
        }
    };
    public static final BluetoothHidDevice.Callback mCallback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            Log.e(TAG,"onAppStatusChanged: " + registered);
            IsRegisted = registered;
        }
        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            Log.e(TAG,"onConnectionStateChanged:"+ state);
            if(state == BluetoothProfile.STATE_DISCONNECTED){
                HidUitls.IsConnected(false);
                if(connectionStateChangeListener != null){
                    connectionStateChangeListener.onDisConnected();
                }
            }else if(state == BluetoothProfile.STATE_CONNECTED){
                HidUitls.IsConnected(true);
                if(connectionStateChangeListener != null){
                    connectionStateChangeListener.onConnected();
                }
            }else if(state == BluetoothProfile.STATE_CONNECTING){
                if(connectionStateChangeListener != null){
                    connectionStateChangeListener.onConnecting();
                }
            }
        }
    };
}
