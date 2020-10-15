package clurc.net.longerir.Utils;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.List;
import java.util.UUID;

public class BluebleUtils {
    public static String CON_DEV_MAC = "CB:A9:87:65:43:21";
    public static String SSBLE_SERVER = "00001810-0000-1000-8000-00805f9b34fb";
    public static String SSBLE_CHARI_PRESSURE =    "00002a35-0000-1000-8000-00805f9b34fb"; /// rx
    public static String SSBLE_CHARI_BYTE8 =  "00002a49-0000-1000-8000-00805f9b34fb";
    //public static String SSBLE_CHARIDESC =  "00002a35-0000-1000-8000-00805f9b34fb";

    public static String SSBLE_MODESERVER = "0000180a-0000-1000-8000-00805f9b34fb";
    public static String SSBLEUUID_ModelStr = "00002a24-0000-1000-8000-00805f9b34fb";  //型号

    public static BluetoothGattCharacteristic getCharacterics(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattCharacteristic success = null;
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic c : characteristics) {
                if (characteristicUUID.equals(c.getUuid())) {
                    success = c;
                    break;
                }
            }
        }
        return success;
    }

    public static boolean enableNotification(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        boolean success = false;
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
            if (characteristic != null) {
                success = gatt.setCharacteristicNotification(characteristic, true); //事件
                if (success) {
                    // 来源：http://stackoverflow.com/questions/38045294/oncharacteristicchanged-not-called-with-ble
                    for(BluetoothGattDescriptor dp: characteristic.getDescriptors()){
                        if (dp != null) {
                            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            gatt.writeDescriptor(dp);
                        }
                    }
                }
            }
        }
        return success;
    }
    public static void disenableNotification(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
            if (characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, false); //事件
                for(BluetoothGattDescriptor dp: characteristic.getDescriptors()){
                    if (dp != null) {
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            dp.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                            dp.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        }
                        gatt.writeDescriptor(dp);
                    }
                }
            }
        }
    }
    //寻找指定的Characteristic
    private static BluetoothGattCharacteristic findNotifyCharacteristic(BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = null;
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                    && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        if (characteristic != null)
            return characteristic;
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                    && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        return characteristic;
    }

    public static String ByteArrToStr(byte[] data){
        String ret = " ";
        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            ret = stringBuilder.toString();
        }
        return ret;
    }
}
