package com.dten.hidroid.mouse;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.dten.hidroid.HidConsts;
import com.dten.hidroid.HidUitls;
import com.dten.hidroid.R;

import java.util.Date;
import java.util.TimerTask;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_POINTER_DOWN;
import static android.view.MotionEvent.ACTION_POINTER_UP;
import static android.view.MotionEvent.ACTION_UP;


public class MouseUtils {

    public static final String TAG = "u-FragmentMousePad";
    private float Xpad, Ypad, Xmus, Ymus;
    private int maxPointerCount;
    private long actionDownTime_Pad = 0;
    float rate = 1f;

    boolean leftbtnUped = true;//左键是否抬起
    boolean leftUped = true;//pad双击模拟左键是否抬起
    boolean rightbtnUped = true;//右键是否抬起
    boolean midbtnUped = true;//中键是否抬起

    TimerTask virtureClickTask;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public  MouseUtils(){

    }

    public boolean mouseLeft(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_UP){
            HidConsts.LeftBtnUp();
            leftbtnUped = true;
        }
        else if(event.getAction() == MotionEvent.ACTION_DOWN){
            HidConsts.LeftBtnDown();
            leftbtnUped = false;
        }
        return true;

    }


    public boolean mouseMove(MotionEvent event){
        switch (event.getAction()) {
            case ACTION_DOWN:
            case ACTION_POINTER_DOWN:
                long now = new Date().getTime();
                long dis = now - actionDownTime_Pad;
                if(dis >=50 && dis <= 150 && leftbtnUped) {
                    if(virtureClickTask != null){
                        virtureClickTask.cancel();
                    }
                    HidConsts.LeftBtnDown();
                    leftUped = false;
                }
                actionDownTime_Pad = now;
                maxPointerCount = event.getPointerCount();
                Xpad = event.getX();
                Ypad = event.getY();
                return true;
            case ACTION_MOVE:
                maxPointerCount = Math.max(maxPointerCount, event.getPointerCount());
                if (HidUitls.IsConnected()) {
                    int deltaX = (int) ((event.getX() - Xpad)*rate);
                    int deltay = (int) ((event.getY() - Ypad)*rate);
                    HidConsts.MouseMove(deltaX, deltay, 0, !leftbtnUped||!leftUped, !rightbtnUped, !midbtnUped);
                }
                Xpad = event.getX();
                Ypad = event.getY();
                return true;
            case ACTION_UP:
            case ACTION_POINTER_UP:
                Xpad = event.getX();
                Ypad = event.getY();
                now = new Date().getTime();
                dis = now - actionDownTime_Pad;
                actionDownTime_Pad = now;
                if (HidUitls.IsConnected()) {
                    if (maxPointerCount == 1) {
                        if(dis >=50 && dis <= 150 && leftUped){
                            virtureClickTask = HidConsts.LeftBtnClickAsync(150);
                        }else if(dis >=50 && dis <= 150 && !leftUped){
                            HidConsts.LeftBtnUp();
                            leftUped = true;//模拟左键抬起
                            HidConsts.LeftBtnClickAsync(20);
                        }
                        else{
                            HidConsts.LeftBtnUp();
                            leftUped = true;//模拟左键抬起
                            virtureClickTask = null;
                        }
                    }
                }
                //UtilCls.SLog(TAG,"ACTION_UP:"+dis);
                return true;
        }
        return false;
    }

    public boolean mouseRight(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_UP){
            HidConsts.RightBtnUp();
            rightbtnUped = true;
        }
        else if(event.getAction() == MotionEvent.ACTION_DOWN){
            HidConsts.RightBtnDown();
            rightbtnUped = false;
        }
        return true;

    }
    public boolean mouseMiddle(MotionEvent event){
        switch (event.getAction()) {
            case ACTION_DOWN:
            case ACTION_POINTER_DOWN:
                HidConsts.MidBtnDown();
                midbtnUped = false;
                maxPointerCount = event.getPointerCount();
                Ymus = event.getY();
                midbtnUped = false;
                return true;
            case ACTION_MOVE:
                maxPointerCount = Math.max(maxPointerCount, event.getPointerCount());
                if (HidUitls.IsConnected()) {
                    if(!midbtnUped){
                        HidConsts.MidBtnUp();
                        midbtnUped = true;
                    }
                    int deltay = -(int)((event.getY() - Ymus));
                    HidConsts.MouseMove(0, 0, deltay, !leftbtnUped, !rightbtnUped, !midbtnUped);
                }
                Ymus = event.getY();
                return true;
            case ACTION_UP:
            case ACTION_POINTER_UP:
                if(!midbtnUped){
                    HidConsts.MidBtnUp();
                    midbtnUped = true;
                }
                return true;
        }

        return false;

    }

}
