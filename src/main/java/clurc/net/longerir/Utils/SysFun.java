package clurc.net.longerir.Utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.hardware.ConsumerIrManager;
import android.provider.Settings;
import android.util.Log;

import com.google.zxing.client.android.MNScanManager;
import com.google.zxing.client.android.model.MNScanConfig;
import com.google.zxing.client.android.other.MNScanCallback;

import static android.content.Context.CONSUMER_IR_SERVICE;
import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;

public class SysFun {
    public static boolean IfHasIrDaPort(Context context){
        boolean hasir = false;
        ConsumerIrManager ir=(ConsumerIrManager)context.getSystemService(CONSUMER_IR_SERVICE);
        if(ir!=null)if(ir.hasIrEmitter())hasir=true;
        return hasir;
    }

    public static boolean IfhasBle(Context context){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return (bluetoothAdapter != null )?true:false;
    }

    public static void BleCheckOpend(Activity context){
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if(bluetoothAdapter==null)return;
        if (!bluetoothAdapter.isEnabled()) {// 如果蓝牙还没有打开
            //请求打开 Bluetooth
            Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //设置 Bluetooth 设备可以被其它 Bluetooth 设备扫描到
            requestBluetoothOn.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置 Bluetooth 设备可见时间
            //requestBluetoothOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_DURATION);
            //请求开启 Bluetooth
            context.startActivityForResult(requestBluetoothOn, 0);
            return;
        }
    }
        //获取是否旋转
    public static int getSensorState(Context context){
        int sensorState = 0;
        try {
            sensorState = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            return sensorState;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return sensorState;
    }

    public static MNScanConfig getQrCodeOption(){
        MNScanConfig scanConfig = new MNScanConfig.Builder()
                //设置完成震动
                .isShowVibrate(false)
                //扫描完成声音
                .isShowBeep(true)
                //显示相册功能
                .isShowPhotoAlbum(true)
                //显示闪光灯
                .isShowLightController(true)
                //自定义文案
                .setScanHintText("  ")
                //自定义文案颜色
                .setScanHintTextColor("#FFFF00")
                //自定义文案大小（单位sp）
                .setScanHintTextSize(16)
                //扫描线的颜色
                .setScanColor("#FFFF00")
                //是否显示缩放控制器
                .isShowZoomController(true)
                //显示缩放控制器位置
                .setZoomControllerLocation(MNScanConfig.ZoomControllerLocation.Bottom)
                //扫描线样式
                .setLaserStyle(MNScanConfig.LaserStyle.Grid)
                //背景颜色
                .setBgColor("#33FF0000")
                //网格扫描线的列数
                .setGridScanLineColumn(30)
                //网格高度
                .setGridScanLineHeight(150)
                //高度偏移值（单位px）+向上偏移，-向下偏移
                .setScanFrameHeightOffsets(150)
                //是否全屏范围扫描
                .setFullScreenScan(false)
                //二维码标记点
                .isShowResultPoint(false)
                .setResultPointConfigs(60, 30, 10, "#FFFFFFFF", "#7000A81F")
                //状态栏设置：颜色，是否黑色字体
                .setStatusBarConfigs("#00000000", true)
                .builder();
       return scanConfig;
    }
}
