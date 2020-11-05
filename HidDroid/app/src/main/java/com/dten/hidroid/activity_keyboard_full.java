package com.dten.hidroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.dten.hidroid.R;

public class activity_keyboard_full extends Activity {
    Button caps;
    Drawable capsBack;
    ViewGroup vg;
    public static final String TAG = "hid_activity_keyboard_full";

    private WindowManager windowManager;// 用于可拖动的浮动窗口
    private WindowManager.LayoutParams windowParams;// 浮动窗口的参数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_full);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.y = 100;
        double width = (double) getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        double height = (double) getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        layoutParams.width= (int)Math.floor(width/1.28);
        layoutParams.height= (int)Math.floor(height/1.35);
        getWindow().setAttributes(layoutParams);
        caps = findViewById(R.id.caps);
        capsBack = caps.getBackground();
        registKeyButton();
        HidConsts.CleanKbd();
    }

    private void setWindowParams(int x, int y) {
        // 建立item的缩略图
        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP;
        // 得到preview左上角相对于屏幕的坐标
        windowParams.x = x;
        windowParams.y = y;
        // 设置宽和高
        double width = (double) getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        double height = (double) getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        windowParams.width= (int)Math.floor(width/1.28);
        windowParams.height= (int)Math.floor(height/1.35);
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        HidUitls.ReConnect(this);
        HidConsts.HidDevice = HidUitls.HidDevice;
        HidConsts.BtDevice =  HidUitls.BtDevice;
    }
    private void registKeyButton(){
        vg = findViewById(R.id.keysButtons);
        for(int i =0;i<vg.getChildCount();i++){
            View view = vg.getChildAt(i);
            if(view.getClass().equals(Button.class)){
                view.setOnTouchListener(onTouchListener);
            }
        }
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                Log.d(TAG,"onkeydown "+v.getTag().toString());
                HidConsts.KbdKeyDown(v.getTag().toString());
            }else if(event.getAction() == MotionEvent.ACTION_UP){
                HidConsts.KbdKeyUp(v.getTag().toString());
            }
            return false;
        }
    };
}
