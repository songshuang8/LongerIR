package clurc.net.longerir.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MobileUUID {
    public static String getUniquePsuedoID(Context ctx) {
        String  m_szAndroidID = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // wlan mac
        String m_szWLANMAC = "";
        WifiManager wm = (WifiManager) ctx
                .getSystemService(ctx.WIFI_SERVICE);
        if (wm != null) {
            WifiInfo ainfo = wm.getConnectionInfo();
            if (ainfo != null)
                m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
        }

        String m_szDevIDShort = "73" +
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 位

        String  serial = m_szAndroidID + m_szWLANMAC;
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    public static String getUniquePsuedoID2(Context ctx) {
        String auniID = "";
        String m_szAndroidID = "";
        String m_szWLANMAC = "";
        // The Android ID
        m_szAndroidID = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // wlan mac
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            if (ContextCompat.checkSelfPermission(ctx,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                WifiInfo ainfo = wm.getConnectionInfo();
                if (ainfo != null)
                    m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
            }
        }
        // //////
        int m_szDevIDShort = 35
                + // we make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10; // 13 digits
        String m_szLongID = m_szDevIDShort + m_szAndroidID
                + m_szWLANMAC;
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
            m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
            // get md5 bytes
            byte p_md5Data[] = m.digest();
            // create a hex string
            for (int i = 0; i < p_md5Data.length; i++) {
                int b = (0xFF & p_md5Data[i]);
                // if it is a single digit, make sure it have 0 in front (proper
                // padding)
                if (b <= 0xF)
                    auniID += "0";
                // add number to string
                auniID += Integer.toHexString(b);
            } // hex string to uppercase
            auniID = auniID.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return auniID.substring(1);
    }
}
