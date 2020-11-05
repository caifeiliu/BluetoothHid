package com.dten.hidroid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HidConsts {

    public static final String TAG = "u-HidConsts";

    public final static String NAME = "Hiddroid";
    public final static String DESCRIPTION = "fac";
    public final static String PROVIDER = "funny";
    public static  BluetoothHidDevice HidDevice;
    public static  BluetoothDevice BtDevice;
    private static Handler handler;
    private static final Queue<HidReport> inputReportQueue = new ConcurrentLinkedQueue<>();
    public static byte ModifierByte = 0x00;
    public static byte KeyByte = 0x00;
    public static void CleanKbd(){ SendKeyReport(new byte[]{0,0});Alted = false; }
    public static boolean Alted = false;

    protected static void addInputReport(final HidReport inputReport) {
        if (inputReport != null) {
            inputReportQueue.offer(inputReport);
        }
    }

    public final static byte[] Descriptor = {
            //Mouse
            (byte) 0x05, (byte) 0x01,  //USAGE page(Generic Desktop)
            (byte) 0x09, (byte) 0x02,  //usage (mouse)
            (byte) 0xa1, (byte) 0x01,  //collection(Application)
            (byte) 0x09, (byte) 0x01,  //usage (pointer)
            (byte) 0xa1, (byte) 0x00,  //collection(physical)
            (byte) 0x85, (byte) 0x01,  //report id(mouse)
            (byte) 0x05, (byte) 0x09,  //usage page(button)
            (byte) 0x19, (byte) 0x01,  //usage minimum(button1)
            (byte) 0x29, (byte) 0x03,  //usage maximum(button3)
            (byte) 0x15, (byte) 0x00,  //logical minimum(0)
            (byte) 0x25, (byte) 0x01,  //logical maximum(1)
            (byte) 0x95, (byte) 0x03,  //report count(3)
            (byte) 0x75, (byte) 0x01,  //report size(1bit)
            (byte) 0x81, (byte) 0x02,  //input(data,var,abs)
            (byte) 0x95, (byte) 0x01,  //report count(1)
            (byte) 0x75, (byte) 0x05,  //report size(5)
            (byte) 0x81, (byte) 0x03,  //input(data,var,abs)
            (byte) 0x05, (byte) 0x01,  //USAGE page(Generic Desktop)
            (byte) 0x09, (byte) 0x30,  //usage(X)
            (byte) 0x09, (byte) 0x31,  //usage(Y)
            (byte) 0x09, (byte) 0x38,  //usage(wheel)
            (byte) 0x15, (byte) 0x81,  //logical minimum(-127)
            (byte) 0x25, (byte) 0x7f,  //logical maximum(127)
            (byte) 0x75, (byte) 0x08,  //report size(8bit)
            (byte) 0x95, (byte) 0x03,  //report count(3)
            (byte) 0x81, (byte) 0x06,  //input(data,var,abs)
            (byte) 0xc0, (byte) 0xc0,  //End collection
            //Keyboard
            (byte) 0x05, (byte) 0x01,  //USAGE page(Generic Desktop)
            (byte) 0x09, (byte) 0x06,  //usage (keyboard)
            (byte) 0xa1, (byte) 0x01,  //collection(Application)
            (byte) 0x85, (byte) 0x02,  //report id(keyboard)
            (byte) 0x05, (byte) 0x07,  //usage page(keyboard)
            (byte) 0x19, (byte) 0xE0,  //usage minimum(keyboard leftControl)
            (byte) 0x29, (byte) 0xE7,  //usage maximum(keyboard right GUI)
            (byte) 0x15, (byte) 0x00,  //logical minimum(0)
            (byte) 0x25, (byte) 0x01,  //logical maximum(1)
            (byte) 0x75, (byte) 0x01,  //report size(1)
            (byte) 0x95, (byte) 0x08,  //report count(8)
            (byte) 0x81, (byte) 0x02,  //input(data,var,abs)
            (byte) 0x95, (byte) 0x01,  //report count(1)
            (byte) 0x75, (byte) 0x08,  //report size(8)
            (byte) 0x15, (byte) 0x00,  //logical minimum(0)
            (byte) 0x25, (byte) 0x65,  //logical maximum(101)
            (byte) 0x19, (byte) 0x00,  //usage minimum(0)
            (byte) 0x29, (byte) 0x65,  //usage maximum(keyboard application)
            (byte) 0x81, (byte) 0x00,  //input(data,var,abs)
            //leds
            (byte) 0x05, (byte) 0x08,  //usage page(leds)
            (byte) 0x95, (byte) 0x05,  //report count(6)
            (byte) 0x75, (byte) 0x01,  //report size(1)
            (byte) 0x19, (byte) 0x01,  //usage minimum(1)
            (byte) 0x29, (byte) 0x05,  //usage maximum(5)
            (byte) 0x91, (byte) 0x02,  //output(2)
            (byte) 0x95, (byte) 0x01,  //report count(01)
            (byte) 0x75, (byte) 0x03,  //report size(3)
            (byte) 0x91, (byte) 0x03,  //output(cost,var,abs)

        (byte)0xc0  //end collection
    };

    static long scheperoid = 5;
    public static void reporttrans(Context context){
        handler = new Handler(context.getMainLooper());
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                HidReport report = inputReportQueue.poll();
                if(report != null){
                    if (HidUitls.IsConnected()) {
                        postReport(report);
                    }
                }
            }
        }, 0, scheperoid);
    }

    private static void postReport(HidReport report){
        report.SendState = HidReport.State.Sending;
        boolean ret = HidDevice.sendReport(BtDevice,report.ReportId,report.ReportData);
        if(!ret){
            report.SendState = HidReport.State.Failded;
        }else{
            report.SendState = HidReport.State.Sended;
        }
    }

    public static void SendMouseReport(byte[] reportData) {
        HidReport report = new HidReport(HidReport.DeviceType.Mouse,(byte)0x01,reportData);
        addInputReport(report);
    }

    private static HidReport MouseReport = new HidReport(HidReport.DeviceType.Mouse,(byte)0x01,new byte[]{0,0,0,0});
    public static void MouseMove(int dx, int dy, int wheel, final boolean leftButton, final boolean rightButton, final boolean middleButton) {

        if(MouseReport.SendState.equals(HidReport.State.Sending)){
            return;
        }
        if (dx > 127) dx = 127;
        if (dx < -127) dx = -127;
        if (dy > 127) dy = 127;
        if (dy < -127) dy = -127;
        if (wheel > 127) wheel = 127;
        if (wheel < -127) wheel = -127;
        if (leftButton) {
            MouseReport.ReportData[0] |= 1;
        }else{
            MouseReport.ReportData[0] = (byte)(MouseReport.ReportData[0] & (~1));
        }
        if (rightButton) {
            MouseReport.ReportData[0] |= 2;
        }else{
            MouseReport.ReportData[0] = (byte)(MouseReport.ReportData[0] & (~2));
        }
        if (middleButton) {
            MouseReport.ReportData[0] |= 4;
        }else{
            MouseReport.ReportData[0] = (byte)(MouseReport.ReportData[0] & (~4));
        }
        MouseReport.ReportData[1] = (byte) dx;
        MouseReport.ReportData[2] = (byte) dy;
        MouseReport.ReportData[3] = (byte) wheel;

        addInputReport(MouseReport);
    }
    public static void LeftBtnDown(){
        HidConsts.MouseReport.ReportData[0] |= 1;
        SendMouseReport(HidConsts.MouseReport.ReportData);
    }
    public static void LeftBtnUp(){
        HidConsts.MouseReport.ReportData[0] &= (~1);
        SendMouseReport(HidConsts.MouseReport.ReportData);
    }
    public static void LeftBtnClick(){
        LeftBtnDown();
        UtilCls.DelayTask(new Runnable() {
            @Override
            public void run() {
                LeftBtnUp();
            }
        },20,true);
    }
    public static TimerTask LeftBtnClickAsync(int delay){
        return UtilCls.DelayTask(new Runnable() {
            @Override
            public void run() {
                LeftBtnClick();
            }
        },delay,true);
    }
    public static void RightBtnDown(){
        HidConsts.MouseReport.ReportData[0] |= 2;
        SendMouseReport(HidConsts.MouseReport.ReportData);
    }
    public static void RightBtnUp(){
        HidConsts.MouseReport.ReportData[0] &= (~2);
        SendMouseReport(HidConsts.MouseReport.ReportData);
    }
    public static void MidBtnDown(){
        HidConsts.MouseReport.ReportData[0] |= 4;
        SendMouseReport(HidConsts.MouseReport.ReportData);
    }
    public static void MidBtnUp(){
        HidConsts.MouseReport.ReportData[0] &= (~4);
        SendMouseReport(HidConsts.MouseReport.ReportData);
    }

    public static byte ModifierDown(byte UsageId){
        synchronized (HidConsts.class){
            ModifierByte |= UsageId;
        }
        return ModifierByte;
    }
    public static byte ModifierUp(byte UsageId){
        UsageId = (byte)(~((byte)(UsageId)));
        synchronized (HidConsts.class){
            ModifierByte = (byte)(ModifierByte & UsageId);
        }
        return ModifierByte;
    }
    public static void KbdKeyDown(String usageStr){
        if(!TextUtils.isEmpty(usageStr)){
            if(usageStr.startsWith("M")){
                usageStr = usageStr.replace("M","");
                synchronized (HidConsts.class){
                    byte mod = ModifierDown((byte)Integer.parseInt(usageStr));
                    SendKeyReport(new byte[]{mod,KeyByte});
                }
            }else{
                byte key = (byte)Integer.parseInt(usageStr);
                synchronized (HidConsts.class){
                    KeyByte = key;
                    SendKeyReport(new byte[]{ModifierByte,KeyByte});
                }
            }
        }
    }
    public static void KbdKeyUp(String usageStr){
        if(!TextUtils.isEmpty(usageStr)){
            if(usageStr.startsWith("M")){
                usageStr = usageStr.replace("M","");
                synchronized (HidConsts.class) {
                    byte mod = ModifierUp((byte)Integer.parseInt(usageStr));
                    SendKeyReport(new byte[]{mod,KeyByte});
                }
            }else{
                synchronized (HidConsts.class) {
                    KeyByte = 0;
                    SendKeyReport(new byte[]{ModifierByte,KeyByte});
                }
            }
        }
    }
    private static void SendKeyReport(byte[] reportData){
        HidReport report = new HidReport(HidReport.DeviceType.Keyboard,(byte)0x02,reportData);
        addInputReport(report);
    }
}
