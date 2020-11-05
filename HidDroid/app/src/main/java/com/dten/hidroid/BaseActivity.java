package com.dten.hidroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.fragment.app.FragmentActivity;

import com.dten.hidroid.mouse.MouseUtils;

public class BaseActivity extends FragmentActivity {
    private static final String TAG = "u-BaseActivity";
    private PowerManager.WakeLock wakeLock;//唤醒锁
    protected int LevelAndFlags;//唤醒锁级别

    @SuppressLint("InvalidWakeLockTag")
    public void AquireWakeLock(){
        ReleaseWakeLock();
        AquireWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        APP.GetInstance().addActivity(BaseActivity.this);
    }
    @SuppressLint("InvalidWakeLockTag")
    protected void AquireWakeLock(int levelAndFlags){
        try {
            ReleaseWakeLock();
            //PowerManager.PARTIAL_WAKE_LOCK,PowerManager.SCREEN_DIM_WAKE_LOCK
            wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(levelAndFlags, TAG);
            if (wakeLock != null) {
                wakeLock.acquire();//这句执行后，手机将不会休眠，直到执行wakeLock.release();方法
                //Toast.makeText(this, lock, Toast.LENGTH_SHORT).show();
            }else{
            }
            LevelAndFlags = levelAndFlags;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected void ReleaseWakeLock(){
        try {
            if(wakeLock!=null) {
                wakeLock.release();
                wakeLock = null;
            }
        }catch (Exception ex){ex.printStackTrace();}
    }
}
