package clurc.net.longerir.manager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Map;

public class UsbHidDevice {
    private static final int INTERFACE_CLASS_HID = 3;
    private static final String ACTION_USB_PERMISSION = "clurc.net.longerir.manager.USB_PERMISSION";
    private Context context;
    private int vid;
    private int pid;
    private boolean actived;
    private boolean opened;
    private boolean permissing;

    private UsbManager mUsbManager;
    private UsbDevice mUsbDevice;
    private UsbInterface mUsbInterface;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mInUsbEndpoint;
    private UsbEndpoint mOutUsbEndpoint;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                context.unregisterReceiver(mUsbReceiver);
                openDevice();
            }
            permissing = false;
        }
    };

    public void Init(){
        if (opened && mConnection!=null)
            mConnection.close();
        if (mUsbManager == null)return;
        mUsbDevice = null;
        mUsbInterface = null;
        mConnection = null;
        actived = false;
        opened = false;
        Map<String, UsbDevice> devices = mUsbManager.getDeviceList();
        for (UsbDevice device : devices.values()) {
            if ((vid == 0 || device.getVendorId() == vid) && (pid == 0 || device.getProductId() == pid)) {
                for (int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface usbInterface = device.getInterface(i);
                    if (usbInterface.getInterfaceClass() == INTERFACE_CLASS_HID) {
                        mUsbDevice = device;
                        mUsbInterface = usbInterface;
                        actived = true;
                        break;
                    }
                }
            }
        }
        if(mUsbDevice != null){
            for (int i = 0; i < mUsbInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = mUsbInterface.getEndpoint(i);
                int dir = endpoint.getDirection();
                int type = endpoint.getType();
                if (mInUsbEndpoint == null && dir == UsbConstants.USB_DIR_IN && type == UsbConstants.USB_ENDPOINT_XFER_INT) {
                    mInUsbEndpoint = endpoint;
                }
                if (mOutUsbEndpoint == null && dir == UsbConstants.USB_DIR_OUT && type == UsbConstants.USB_ENDPOINT_XFER_INT) {
                    mOutUsbEndpoint = endpoint;
                }
            }
        }
    }

    public UsbHidDevice(Context context, int vid, int pid/*,OnUsbHidDeviceListener listener*/) {
        this.context = context;
        this.vid = vid;
        this.pid = pid;
        //this.mListener = listener;
        actived = false;
        opened = false;
        permissing = false;
        mUsbManager = (UsbManager) context.getApplicationContext().getSystemService(Context.USB_SERVICE);
    }

    public boolean getActive(){
        return actived;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String getDeviceName() {
        if(mUsbDevice!=null)
            return mUsbDevice.getDeviceName();
        else
            return null;
    }

    public void tryOpenDevice(){
        if (!mUsbManager.hasPermission(mUsbDevice)) {
            if(permissing)return;
            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            permissing = true;
            context.registerReceiver(mUsbReceiver, filter);
            mUsbManager.requestPermission(mUsbDevice, permissionIntent);
        }else
        {
            openDevice();
        }
    }

    private void openDevice() {
        opened = false;
        if(mUsbDevice == null)return;
        mConnection = mUsbManager.openDevice(mUsbDevice);
        if (mConnection == null) {
            return;
        }
        if (!mConnection.claimInterface(mUsbInterface, true)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            mConnection.setInterface(mUsbInterface);
        }
        opened = true;
    }

    public boolean getOpened(){
        return opened;
    }

    public void close() {
        if (opened)
            mConnection.close();
    }

    public boolean write(byte[] data) {
        if (mConnection == null)return false;
        if (mOutUsbEndpoint == null)return false;
        mConnection.bulkTransfer(mOutUsbEndpoint, data, mOutUsbEndpoint.getMaxPacketSize(), 1000);
        return true;
    }

    public byte[] read(int timeout) {
        if (mConnection == null)return null;
        if(mUsbDevice==null)return null;
        if(!opened)return null;
        int size = mInUsbEndpoint.getMaxPacketSize();
        byte[] buffer = new byte[size];
        int bytesRead = mConnection.bulkTransfer(mInUsbEndpoint, buffer, size, timeout);
        if (bytesRead < size) {
            return null;
        }
        return buffer;
    }

    public void setNull(){
        if (opened && mConnection!=null)
            mConnection.close();
        if (mUsbManager == null)return;
        mUsbDevice = null;
        mUsbInterface = null;
        mConnection = null;
        actived = false;
        opened = false;
    }
}
