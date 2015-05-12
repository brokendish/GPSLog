package org.brokendish.timelog;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


/**
 * Created by hidekin on 15/05/02.
 */
public class ClickEvents {

    public String getAddress;
    public String getTime;
    public String getLatlng;

    //-------------------------------
    //latlngから住所を逆引き
    //-------------------------------
    public void GetAddress(double latitude,double longitude,Context thi) {

        // 住所の取得
        StringBuffer sbuf = new StringBuffer();
        Geocoder geoc = new Geocoder(thi, Locale.getDefault());
        try {
            List<Address> lstAaa = geoc.getFromLocation(latitude, longitude,1);
            for (Address addr : lstAaa) {
                int idx = addr.getMaxAddressLineIndex();
                for (int i = 1; i <= idx; i++) {
                    sbuf.append(addr.getAddressLine(i));
                    Log.v("addr", addr.getAddressLine(i));
                }
            }
            Toast.makeText(thi, sbuf.toString(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        getAddress = sbuf.toString();
    }

    public void GetTime(double tim){

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        getTime = df.format(tim);


    }

    //-------------------------------
    //Listに表示去れている文字列から緯度経度を抜き出す
    //-------------------------------
    public String splitLatLng(String msg){

        String ret="";
        String[] latlng = msg.split("@", 0);
        ret = latlng[1].trim();
        return ret;
    }

}
