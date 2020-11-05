package com.dten.hidroid;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class APP extends Application {

    private final static String TAG = "u-APP";
    private static APP application;
    public static APP GetInstance(){
        return application;
    }
    private ArrayList<Activity> lists = new ArrayList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        //RegistTimeTick();
    }

    public void addActivity(Activity activity){
        if(!lists.contains(activity)){
            lists.add(activity);
        }
    }

    public void exit() {
        try {
            for (Activity activity : lists) {
                activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }



    public String ReadConfig(String key){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("hiddroid",0);
        String val = sharedPreferences.getString(key,"");
        return val;
    }
    public void WriteConfig(String key,String val){
        try {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("hiddroid",0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, val);
            editor.commit();
        }catch (Exception ex)
        {ex.printStackTrace();}
    }

    public void RegistTimeTick(){

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(new AppBroadcastReceiver(), filter);
    }

    class AppBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                //UtilCls.SLog(TAG,"AppBroadcastReceiver Intent.ACTION_TIME_TICK" + Configers.ymdhmsDateFormat.format(new Date()));
                //检查Service状态
//                boolean isServiceRunning = false;
//                ActivityManager manager = (ActivityManager)application.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//                for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {
//                    //if("com.aegerita.Services.UartService".equals(service.service.getClassName()))
//                    if(UartService.class.getName().equals(service.service.getClassName())) {
//                        isServiceRunning = true;
//                    }
//                }
//                if (!isServiceRunning) {
//                    //Intent i = new Intent(context, UartService.class);
//                    //context.startService(i);
//                }
                //UtilCls.Alarm(getApplicationContext(),false);
                //UtilCls.SLog(TAG,"com.aegerita.Services.UartService running:"+isServiceRunning);
            }
            //if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){}
        }
    }

}
