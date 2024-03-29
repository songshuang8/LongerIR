package clurc.net.longerir.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import clurc.net.longerir.Utils.BluebleUtils;
import clurc.net.longerir.uicomm.BleThread;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;
import static clurc.net.longerir.uicomm.SsSerivce.TAG_SS;
import static clurc.net.longerir.Utils.BluebleUtils.ByteArrToStr;

public class BlueDeiceCommu {
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeScanner mBluetoothLeScanne;
    private SampleScanCallback mscanback;
//    private List<ScanFilter> scanfilters;

    private BluetoothGattCharacteristic char_modestr;  //00002a24-0000-1000-8000-00805f9b34fb  型号
    private BluetoothGattCharacteristic char_byte8;

    private int mConnectionState = BluetoothProfile.STATE_DISCONNECTED;

    private boolean canconnect;

    private BluetoothDevice mdevice;

    private static BlueDeiceCommu instance=null;
    public static BlueDeiceCommu getInstance(){
        if(instance==null) {
            instance = new BlueDeiceCommu();
        }
        return instance;
    }

    private OnMyBlueEvents myBlueEvents;
    public interface OnMyBlueEvents{
        public abstract void OnStateChanged(boolean state);
        public abstract void OnModeRecved(String modelstr);
        public abstract void OnWriteed(boolean bsuc,byte[] buf);
        public abstract void OnLeared(byte[] buf);
        public abstract void OnReadBack(byte[] buf);
    }
    public void setOnMyBlueEvents(OnMyBlueEvents aev){
        this.myBlueEvents = aev;
    }

    public void readMyByte8(){
        if (bluetoothAdapter == null || mBluetoothGatt == null)return;
        if(char_byte8!=null)
            mBluetoothGatt.readCharacteristic(char_byte8);
    }

    public boolean readModelStr(){
        if (bluetoothAdapter == null || mBluetoothGatt == null)return false;
        if(mConnectionState != BluetoothProfile.STATE_CONNECTED)return false;
        if(char_modestr!=null)
            return mBluetoothGatt.readCharacteristic(char_modestr);
        return false;
    }

    public void writeMyByte8(byte[] data){
        if (bluetoothAdapter == null || mBluetoothGatt == null)return;
        if(char_byte8!=null) {
            char_byte8.setValue(data);
            mBluetoothGatt.writeCharacteristic(char_byte8);
        }
    }

    public  BlueDeiceCommu() {
        Log.i(TAG_SS, "创建Ble主类");
        canconnect = true;
        mdevice = null;
        mBluetoothGatt = null;
    }

    public  void setInit(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        IntentFilter blueactived = new IntentFilter();
        blueactived.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluestates,blueactived);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(bluePareiEvent,intentFilter);

        //
        mscanback = new SampleScanCallback();
        isscaning = false;
        isbangding = false;
//        scanfilters = new ArrayList<ScanFilter>();
//        ScanFilter afilters = new ScanFilter("Longer",null,null);
//        scanfilters.add(afilters);
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override //当连接上设备或者失去连接时会回调该函数
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if(mBluetoothGatt==null){
                    Log.i(TAG_SS, "连接成功但为空");
                   return;
                }
                canconnect = false;
                // Attempts to discover services after successful connection.
                Log.i(TAG_SS, "开始发现服务");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                Log.i(TAG_SS, "断开");
                if(mBluetoothGatt!=null){
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    scancnt = 0;
                }
                mdevice = null;
                canconnect = true;
                myBlueEvents.OnStateChanged(false);
            }
        }
        //当设备是否找到服务时，会回调该函数
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> servs = gatt.getServices(); //此处返回获取到的服务列表
                for (BluetoothGattService gattService : servs) { // 遍历出servs里面的所有服务
                    Log.i(TAG_SS, "服务:"+gattService.getUuid());
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) { // 遍历每条服务里的所有Characteristic
                        String sstr ="条目"+gattCharacteristic.getUuid()+ "属性"+gattCharacteristic.getProperties();
                        //gattCharacteristic.get
                        if((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ)== BluetoothGattCharacteristic.PROPERTY_READ){
                            sstr +=",可以读";
                        }
                        if( (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE)== BluetoothGattCharacteristic.PROPERTY_WRITE){
                            sstr +=",可以写";
                        }
                        if( (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY)== BluetoothGattCharacteristic.PROPERTY_NOTIFY){
                            sstr +=",可以Notify";
                        }
                        Log.i(TAG_SS, sstr);

                        List<BluetoothGattDescriptor> mDescriptors = gattCharacteristic.getDescriptors();
                        for (BluetoothGattDescriptor mDescriptor : mDescriptors) {
                            Log.i(TAG_SS, "描述"+mDescriptor.getUuid().toString());
                        }
                    }
                }
                if(BluebleUtils.enableNotification(gatt, UUID.fromString(BluebleUtils.SSBLE_SERVER),UUID.fromString(BluebleUtils.SSBLE_CHARI_PRESSURE))){
                    Log.i(TAG_SS, "事件成功");
                }else{
                    Log.i(TAG_SS, "事件失败");
                }
                char_byte8 = BluebleUtils.getCharacterics(gatt,UUID.fromString(BluebleUtils.SSBLE_SERVER),UUID.fromString(BluebleUtils.SSBLE_CHARI_BYTE8));
                char_modestr = BluebleUtils.getCharacterics(gatt,UUID.fromString(BluebleUtils.SSBLE_MODESERVER),UUID.fromString(BluebleUtils.SSBLEUUID_ModelStr));
//
                mConnectionState = BluetoothProfile.STATE_CONNECTED;
                myBlueEvents.OnStateChanged(true);
                Log.i(TAG_SS, "连接成功");
            } else {
                Log.w(TAG_SS, "DisCover 失败" + status);
            }
        }

        @Override//当读取设备时会回调该函数
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(characteristic.getUuid().toString().equals(BluebleUtils.SSBLEUUID_ModelStr)){
                    String modestr = characteristic.getStringValue(0);
                    myBlueEvents.OnModeRecved(modestr);
                    Log.w(TAG_SS, "蓝牙型号：" + modestr);
                }else if(characteristic.getUuid().toString().equals(BluebleUtils.SSBLE_CHARI_BYTE8)) {
                    Log.w(TAG_SS, "主动读取到：" + ByteArrToStr(characteristic.getValue()));
                    myBlueEvents.OnReadBack(characteristic.getValue());
                }else{
                        Log.w(TAG_SS, "Charc数据：" + ByteArrToStr(characteristic.getValue()));
                }
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.w(TAG_SS, "Charc写数据：sta="+status+"data:"+ ByteArrToStr(characteristic.getValue()));
            if(characteristic.getUuid().toString().equals(BluebleUtils.SSBLE_CHARI_BYTE8)) {
                if (status == BluetoothGatt.GATT_SUCCESS)
                    myBlueEvents.OnWriteed(true,characteristic.getValue());
                else
                    myBlueEvents.OnWriteed(false,null);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            Log.w(TAG_SS, "描述可读：" + descriptor.getUuid()+",sta="+status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG_SS, "描述数据：" + ByteArrToStr(descriptor.getValue()));
            }
        }

        @Override //当向设备Descriptor中写数据时，会回调该函数
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG_SS, "描述写入成功");
            }else{
                Log.w(TAG_SS, "描述写入失败");
            }
        }
        @Override//设备发出通知时会调用到该接口
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.w(TAG_SS, characteristic.getUuid().toString()+"被动接收到：" + ByteArrToStr(characteristic.getValue()));
            if(characteristic.getUuid().toString().equals(BluebleUtils.SSBLE_CHARI_PRESSURE)) {
                myBlueEvents.OnLeared(characteristic.getValue());
            }
        }
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.w(TAG_SS, "信号变化：rssi=" + rssi +  "sta="+status);
        }
    };

    private volatile boolean isbangding;
    private volatile boolean isscaning;
    private volatile int scancnt = 0;
    private int connectbuf = 0;
    public void connect() {
        if(canconnect == false){
            Log.w(TAG_SS,"不需要连接");
            return;
        }
        if (bluetoothAdapter == null  || bluetoothAdapter.isEnabled()==false) {
            //Log.w(TAG_SS,"蓝牙适配器为空");
            return;
        }
        if(mBluetoothLeScanne==null) {
            mBluetoothLeScanne = bluetoothAdapter.getBluetoothLeScanner();
            isscaning = false;
            scancnt = 0;
        }
        if(mdevice==null) {
            if(scancnt==0) {
                if(isscaning)
                    mBluetoothLeScanne.stopScan(mscanback);
                Log.w(TAG_SS, "开始扫描.");
                mBluetoothLeScanne.startScan(mscanback);
                isscaning = true;
            }
            scancnt++;
            if(scancnt>30){
                scancnt = 0;
            }
            connectbuf = 0;
            return;
        }else{
            if(isscaning)
                mBluetoothLeScanne.stopScan(mscanback);
            isscaning = false;
            scancnt = 0;
        }
        //mdevice = bluetoothAdapter.getRemoteDevice(BluebleUtils.CON_DEV_MAC);
        int bondstate = mdevice.getBondState();
        if (bondstate == BluetoothDevice.BOND_BONDED) {
            if(isbangding){
                Log.d(TAG_SS, "等待完成配对");
                return;
            }
            if(connectbuf==0) {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt.close();
                }
                mBluetoothGatt = mdevice.connectGatt(context, false, mGattCallback); //该函数才是真正的去进行连接
                if (mBluetoothGatt == null) {
                    return;
                }
                Log.d(TAG_SS, "连接。。。");
                mConnectionState = STATE_CONNECTING;
            };
            connectbuf++;
            if(connectbuf>30)connectbuf=0;
        }else{
            if(isbangding)return;
            try {
                //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                Log.d(TAG_SS, "开始配对");
                Boolean returnValue = (Boolean) createBondMethod.invoke(mdevice);
                isbangding = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        Log.d(TAG_SS, "blue disevent");
        mdevice = null;
        if(mBluetoothLeScanne!=null && isscaning)
            mBluetoothLeScanne.stopScan(mscanback);
        scancnt = 0;
        isbangding = false;

        context.unregisterReceiver(bluestates);
        context.unregisterReceiver(bluePareiEvent);

        if(mBluetoothGatt!=null) {
            BluebleUtils.disenableNotification(mBluetoothGatt, UUID.fromString(BluebleUtils.SSBLE_SERVER), UUID.fromString(BluebleUtils.SSBLE_CHARI_PRESSURE));
            if(mConnectionState != BluetoothProfile.STATE_DISCONNECTED){
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
            }
        }
        canconnect = true;
//        if(mConnectionState != BluetoothProfile.STATE_DISCONNECTED) {
//            Log.d(TAG_SS, "blue do dis");
//            mBluetoothGatt.disconnect();
//        }
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
    }

    private boolean CheckSacanDevice(ScanResult ret){
        if(mdevice!=null)return true;
        BluetoothDevice temp = ret.getDevice();
        if(temp==null)return false;
        String aname = temp.getName();
        if(aname==null)return false;
        Log.d(TAG_SS, "扫描到:"+aname);
        if(aname.equals("LongerIR")){
            mdevice = temp;
            return true;
        }
        //mdevice = bluetoothAdapter.getRemoteDevice(BluebleUtils.CON_DEV_MAC);
        return false;
    }
    private class SampleScanCallback extends ScanCallback {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                if(CheckSacanDevice(result))
                    return;
            }
        }
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            CheckSacanDevice(result);
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG_SS, "扫描错误......");
        }
    }


    private BroadcastReceiver bluePareiEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                final int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                boolean isSuccess = false;
                switch (type) {
                    case 0:
                    case 2:
                        isSuccess = device.setPin("012345".getBytes());
                        break;
                }
                if (isSuccess) {
                    Log.d(TAG_SS, "Sucess bond.");
                    abortBroadcast();//这个主要是在setpin成功后防止输入框的弹出
                } else {
                    device.createBond();
                }
                isbangding = true;
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG_SS, "正在配对......");
                        isbangding = true;
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG_SS,"完成配对");
                        isbangding = false;
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG_SS, "取消配对");
                        isbangding = false;
                    default:
                        break;
                }
            }
        }
    };

    private BroadcastReceiver bluestates = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            //Log.e("onReceive---------蓝牙正在打开中");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Log.d(TAG_SS,"蓝牙已经打开");
                            if(mBluetoothGatt!=null){
                                mBluetoothGatt.disconnect();
                                mBluetoothGatt.close();
                                mBluetoothGatt = null;
                            }
                            mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                            canconnect=true;
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Log.d(TAG_SS,"蓝牙正在关闭");
                            if(mBluetoothGatt!=null){
                                mBluetoothGatt.disconnect();
                                mBluetoothGatt.close();
                                mBluetoothGatt = null;
                            }
                            canconnect=false;
                            mConnectionState = BluetoothProfile.STATE_DISCONNECTED;
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            Log.d(TAG_SS,"蓝牙已经关闭");
                            break;
                    }
                    break;
            }
        }
    };

    public int getintConnectionState(){
        return mConnectionState;
    }
}
