package com.dten.hidroid;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Create by caifei.liu on 2020/8/22.
 */
public class DeviceAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    List<BtItemBean> datas;

    static class ViewHolder{
        private ImageView img_signal;
        private TextView tvadd;
        private TextView tvname;
        private TextView tvpaired;
        private View layout;
    }
    private View layout;

    private ItemClickListener mItemClickListener;

    public DeviceAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        datas = new ArrayList<>();
    }
    public void addData(BtItemBean btItemBean){
        datas.add(btItemBean);
        notifyDataSetChanged();
    }

    public void add(int index,BtItemBean btItemBean){
        datas.add(index,btItemBean);
        notifyDataSetChanged();
    }

    public void addDataALL(List<BtItemBean> btItemBeans){
        datas.addAll(btItemBeans);
        notifyDataSetChanged();
    }

    public void clear(){
        datas.clear();
    }

    public List<BtItemBean> getData(){
        return datas;
    }

    public void remove(BtItemBean btItemBean){
        datas.remove(btItemBean);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItemClickListener(ItemClickListener listener){
        mItemClickListener=listener;
    }

    public interface ItemClickListener{
        void onItemClickListener(BtItemBean btItemBean);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup vg;
        ViewHolder vh = null;
        if(convertView == null){
             vh = new ViewHolder();
             convertView = (FrameLayout)inflater.inflate(R.layout.device_element, null);
             vh.tvadd = ((TextView) convertView.findViewById(R.id.address));
             vh.tvname = ((TextView) convertView.findViewById(R.id.name));
             vh.tvpaired = (TextView) convertView.findViewById(R.id.paired);
             vh.img_signal = (ImageView)convertView.findViewById(R.id.img_signal);
             vh.layout = convertView.findViewById(R.id.layout);
             convertView.setTag(vh);

        }else {
            vh = (ViewHolder) convertView.getTag();
        }

//        if (convertView != null) { vg = (FrameLayout) convertView; }
//          else {
//        vg = (FrameLayout) inflater.inflate(R.layout.device_element, null);// }
        final BtItemBean btItemBean = datas.get(position);
        BluetoothDevice bluetoothDevice=btItemBean.getBluetoothDevice();
//        final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
//        final TextView tvname = ((TextView) vg.findViewById(R.id.name));
//        final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
//        vh.layout = vg.findViewById(R.id.layout);
        vh.layout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mItemClickListener!=null){
                    mItemClickListener.onItemClickListener(btItemBean);
                }
            }
        });
//            tvrssi.setVisibility(View.VISIBLE);
////            byte rssival = (byte) devRssiValues.get(btItemBean.getBluetoothDevice().getAddress()).intValue();
////            if (rssival != 0) {
////                tvrssi.setText("Rssi = " + String.valueOf(rssival));
////            }

        vh.tvname.setText(bluetoothDevice.getName());
        vh.tvadd.setText(bluetoothDevice.getAddress());
        vh.tvname.setTextColor(Color.BLACK);
        vh.tvadd.setTextColor(Color.BLACK);
        vh.tvpaired.setTextColor(Color.BLACK);
//        if (btItemBean.getBluetoothDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
//            tvname.setTextColor(Color.BLACK);
//            tvadd.setTextColor(Color.BLACK);
//            tvpaired.setTextColor(Color.GRAY);
//            tvpaired.setVisibility(View.VISIBLE);
//            tvpaired.setText("已配对");
//
//        } else {
//            tvname.setTextColor(Color.BLACK);
//            tvadd.setTextColor(Color.BLACK);
//            tvpaired.setVisibility(View.GONE);
//        }

        //连接状态
        switch (btItemBean.getState()){
            case BtItemBean.STATE_UNCONNECT://未连接
            case BtItemBean.STATE_BOND_NONE://未配对
                vh.tvpaired.setText("");
                break;
            case BtItemBean.STATE_BONDING://配对中
                Log.e("franc", "STATE_BONDING: ");
                vh.tvpaired.setText("pairing");
                break;
            case BtItemBean.STATE_BONDED://已配对

                vh.tvpaired.setText("paired");
                break;
            case BtItemBean.STATE_CONNECTING://连接中
                vh.tvpaired.setText("connecting");
                break;
            case BtItemBean.STATE_CONNECTED://已连接
                vh.tvpaired.setText("connected");
                break;
            case BtItemBean.STATE_DISCONNECTING://断开中
                vh.tvpaired.setText("disconnecting");
                break;
            case BtItemBean.STATE_DISCONNECTED://已断开
                vh.tvpaired.setText("disconnected");
                break;
        }

            int styleMajor = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();//获取蓝牙主要分类
            switch (styleMajor) {
                case BluetoothClass.Device.Major.AUDIO_VIDEO://音频设备
                    if( vh.img_signal !=null){
                        vh.img_signal.setImageResource(R.drawable.icon_headset);
                    }
                    break;
                case BluetoothClass.Device.Major.COMPUTER://电脑
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_computer);
                    }
                    break;
                case BluetoothClass.Device.Major.HEALTH://健康状况
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
                case BluetoothClass.Device.Major.IMAGING://镜像，映像
                   if( vh.img_signal !=null) {
                       vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                   }
                    break;
                case BluetoothClass.Device.Major.MISC://麦克风
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
                case BluetoothClass.Device.Major.NETWORKING://网络
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
                case BluetoothClass.Device.Major.PERIPHERAL://外部设备
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
                case BluetoothClass.Device.Major.PHONE://电话
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_phone);
                    }
                    break;
                case BluetoothClass.Device.Major.TOY://玩具
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
                case BluetoothClass.Device.Major.UNCATEGORIZED://未知的
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
                case BluetoothClass.Device.Major.WEARABLE://穿戴设备
                    if( vh.img_signal !=null) {
                        vh.img_signal.setImageResource(R.drawable.icon_bluetooth);
                    }
                    break;
            }

        return convertView;
    }
}
