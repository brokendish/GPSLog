package org.brokendish.timelog;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends ActionBarActivity implements View.OnClickListener,LocationListener {

    private LocationManager locationManager;
    private String rrmsg;
    private double lat;
    private double lng;
    ListView listView = null;
    EditText editText = null;
    ArrayAdapter<String> adapter = null;
    boolean fl=false;
    private static final String BR = System.getProperty("line.separator");
    private static final String OUT_FILE = "listView.txt";
    boolean fg=false;
    boolean tgbFlg=false;

    Button button;
    Button button2;
    Button button3;
    Button button4;
    ToggleButton tgb;

    String Altitude;    //高度
    double Time;        //時間
    String Latitude;    //緯度
    String Longitude;   //経度
    String Accuracy;    //正確さ
    String Speed;       //速度
    String Bearing;     //方角
    String Provider;    //GPSプロバイダ


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ボタン押下イベントリスナーの登録
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(this);
        // ボタン押下イベントリスナーの登録
        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(this);
        // ボタン押下イベントリスナーの登録
        button3 = (Button)findViewById(R.id.button3);
        button3.setOnClickListener(this);
        // ボタン押下イベントリスナーの登録
        button4 = (Button)findViewById(R.id.button4);
        button4.setOnClickListener(this);

        // トグル押下イベントリスナーの登録
        tgb = (ToggleButton)findViewById(R.id.toggleButton);
        tgb.setTextOff("GPS");
        tgb.setTextOn("Network");  //トグルボタンのメッセージ
        tgb.setChecked(false);       //OFFへ変更


        //-------------------------------
        // listViewを作成
        //-------------------------------
        listView = (ListView) findViewById(R.id.listView);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        editText = (EditText)findViewById(R.id.editText);

//        adapter.add("現在地");
//        adapter.add("現在地");
//        adapter.add("現在地");
//        adapter.add("現在地");
        //------------------------------
        // データファイルから読み込んでリストに設定
        //------------------------------
        fileRead();

        listView.setAdapter(adapter);

        //-------------------------------
        //Toggleのクリックイベント
        //-------------------------------
        //ToggleのCheckが変更したタイミングで呼び出されるリスナー
        tgb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //トグルキーが変更された際に呼び出される
                Log.d("ToggleButton", "call OnCheckdChangeListener");
                // ToggleButton が On かどうかを取得
                boolean checked = tgb.isChecked();
                if(checked){
                    button.setText("Network位置情報取得");
                }
                else{
                    button.setText("GPS位置情報取得");
                }
            }
        });

        //-------------------------------
        //listViewのクリックイベント
        //-------------------------------
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = (String)parent.getItemAtPosition(position) + "\nが選択されました。";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();

                //メッセージから緯度経度を取り出す
                ClickEvents ce = new ClickEvents();
                String latlng="";
                latlng = ce.splitLatLng(message);
                Log.d("緯度経度=======", latlng);

                // アクティビティの起動(GoogleMap)
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                Uri uri = Uri.parse("geo:0,0?q=" + latlng + "?z=14");
                intent.setData(uri);
                startActivity(intent);
            }
        });
        //-------------------------------
        //listViewの長押しイベント
        //-------------------------------
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("削除")
                        .setMessage("指定行を削除しますか")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                //--------------------------
                                //ListViewの長押し箇所を削除する
                                //--------------------------
                                deldellist(pos);
                                //--------------------------
                                //リスト内容を全てファイルに書出してListViewを再作成
                                //--------------------------
                                listViewReMake();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            }
            private void deldellist(int pos){
                // 選択項目を削除
                String item = (String) listView.getItemAtPosition(pos);
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
                adapter.remove(item);
                Toast.makeText(MainActivity.this, "選択行を削除しました", Toast.LENGTH_SHORT).show();
            }
        });

        //------------------------------
        // GPSロケーションマネージャの設定
        //------------------------------
        boolean gpsFlg;
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gpsFlg = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        gpsFlg = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Log.d("GPS Enabled", gpsFlg?"OK":"NG");

        chkGpsService();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    // GPS位置情報取得ボタン
    // 位置情報取得を開始する
    private void onClickButton1() {
//        Toast.makeText(MainActivity.this, "GPS位置情報取得を開始します", Toast.LENGTH_SHORT).show();
        //GPSロケーション取得------------------
        String mm = "";
        mm = GpsLocationGet();
        //-----------------------------------
    }
    // エクスポート
    // intentを利用してTEXTデータを関連するアプリに連携
    private void onClickButton2() {
        // インテントの生成
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_TEXT, fileReadText().toString());
//        Toast.makeText(MainActivity.this, "リストの情報をエクスポートします。\nエクスポート先を選択して下さい。", Toast.LENGTH_SHORT).show();


        //現在日時を取得する
        Calendar c = Calendar.getInstance();
        //フォーマットパターンを指定して表示する
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日　HH:mm:ss");
        String readString ="【GPSLog　" + sdf.format(c.getTime()) + "】\n";

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, readString);
        intent.putExtra(Intent.EXTRA_TEXT, fileReadText().toString() +" \n \n \n ");

        // アクティビティの起動(GoogleMap)
        startActivity(intent);
    }
    // Evernote開くボタン
    // Evernoteを開く
    private void onClickButton3() {
        Intent intent = new Intent();
        intent.setAction("com.evernote.action.SEARCH_NOTES");
//        intent.putExtra(SearchManager.QUERY, "tag:note");
        try {
            startActivity(intent);
        }catch(ActivityNotFoundException e){
            Toast.makeText(MainActivity.this, "Evernoteがインストールされていません。\nPlayストアからインストールして下さい。", Toast.LENGTH_SHORT).show();
            // 明示的に指定したアプリが見つからない場合、Playストアへ直行する
            Uri uri = Uri.parse("market://search?q=Evernote");
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(it);
        }
    }

    // DELETEボタン
    // リストの内容を全て削除する
    private void onClickButton4() {
        String mm = "";
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("データファイルの削除")
                .setMessage("データファイルを削除しますか？\nこれまでの履歴は全て削除されます")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        //ファイル削除
                        fileDelete();
                        //リストを削除
                        adapter.clear();
                        Toast.makeText(MainActivity.this, "データファイルを削除しました", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    //--------------------------------------------------
    //インターフェイスを実装 implements OnClickListener
    //--------------------------------------------------
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                onClickButton1();
                break;
            case R.id.button2:
                onClickButton2();
                break;
            case R.id.button3:
                onClickButton3();
                break;
            case R.id.button4:
                onClickButton4();
                break;
        }
    }

    //--------------------------------------
    // GPS
    //--------------------------------------
    private String GpsLocationGet() {

        Altitude = "";
        Time =0;
        Latitude ="";
        Longitude ="";

        //GPSプロバイダ格納
        List<String> providers;
        //利用可能な位置情報プロバイダを取得
        providers = locationManager.getProviders(true);
        String providerG;

        // ToggleButton が On かどうかを取得
       if(tgb.isChecked()){
           //トグルがNetworkであればNetwork
//           Toast.makeText(MainActivity.this, "Networkから取得します", Toast.LENGTH_SHORT).show();
           //NetworkであればNetworkプロバイダのみ使用
           for(int i=0;providers.size()>i;i++){
               providerG = providers.get(i);
               Log.d("-------@@",providerG);
               if(providerG.compareTo(LocationManager.NETWORK_PROVIDER)!=0){
                   providers.remove(i);
                   Log.d("----remove---@@", providers.remove(i));
               }
               if(providers.isEmpty()){
                   Toast.makeText(MainActivity.this, "Networkプロバイダを取得できません。Passive又はGPSから取得します", Toast.LENGTH_SHORT).show();
                   providers = locationManager.getProviders(true);
               }
               else{
                   Toast.makeText(MainActivity.this, "Networkプロバイダから取得します", Toast.LENGTH_SHORT).show();
               }
           }
        }
        else{
           //トグルがGPSであればGPSプロバイダのみ使用
           for(int i=0;providers.size()>i;i++){
               providerG = providers.get(i);
               if(providerG.compareTo(LocationManager.GPS_PROVIDER)!=0){
                   providers.remove(i);
               }
               if(providers.isEmpty()){
                   Toast.makeText(MainActivity.this, "GPSプロバイダを取得できません。Passive又はNetworkから取得します", Toast.LENGTH_SHORT).show();
                   providers = locationManager.getProviders(true);
               }
               else{
                   Toast.makeText(MainActivity.this, "GPSプロバイダから取得します", Toast.LENGTH_SHORT).show();
               }
           }
       }

        //List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            Provider=provider;
            Log.d("GPSプロバイダ：",provider);
            locationManager.requestLocationUpdates(
                    provider,
                    //LocationManager.GPS_PROVIDER,
                    //LocationManager.NETWORK_PROVIDER,
                    10, // 通知のための最小時間間隔（ミリ秒）
                    0, // 通知のための最小距離間隔（メートル）
                    new LocationListener() {

                        @Override
                        public void onLocationChanged(Location location) {
                            String msg = "Lat=" + location.getLatitude()
                                    + "\nLng=" + location.getLongitude();
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            Log.d("Get", msg);

                            rrmsg = msg;
//                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                            //GPSからの情報を取得
                            Altitude = String.valueOf(location.getAltitude());
                            Time =location.getTime();
                            Latitude =String.valueOf(location.getLatitude());
                            Longitude =String.valueOf(location.getLongitude());

                            //ロケーションマネージャを停止
                            locationManager.removeUpdates(this);

                            Provider=location.getProvider();
                            //------------------------------------
                            //緯度経度(latlng)
                            //------------------------------------
                            ClickEvents ag = new ClickEvents();
                            String latlng;
                            latlng = Latitude + "," + Longitude;
                            //------------------------------------
                            //latlngから住所を逆引き
                            //------------------------------------
                            String addre;
                            ag.GetAddress(lat, lng, MainActivity.this);
                            addre = ag.getAddress;
                            //------------------------------------
                            //時刻を取得
                            //------------------------------------
                            String tim;
                            ag.GetTime(Time);
                            tim = ag.getTime;

                            //------------------------------------
                            //文字結合
                            //------------------------------------
                            String ms;
                            String memo = "";
                            //メモの改行を””に変換
                            memo = editText.getText().toString();
                            memo = memo.replaceAll("\n", " ");
                            ms = "■" + tim + " 高度:" + Altitude + " [" + Provider +"]" + "\nメモ:" + memo + "\n" + addre + "\n@" + latlng;
                            Log.d("GPS取得情報：\n", ms);
                            //------------------------------------
                            //Listに値を追加
                            //------------------------------------
                            if (ms != "") {
                                adapter.add(ms);
                                listView.setAdapter(adapter);
                                //最終行を選択
                                listView.setSelection(listView.getCount());
                            }
                            //------------------------------------
                            //ファイルに保存
                            //------------------------------------
                            String mss;
                            mss = "■" + tim + "| 高度:" + Altitude + " [" + Provider +"]" + "|メモ:" + editText.getText() + "|" + addre + "|@" + latlng;
                            fileOutput(mss);

                            Altitude = "";
                            Time =0;
                            Latitude ="";
                            Longitude ="";
                            Provider="";
                            editText.setText(" ");

                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }
                    }
            );

        }

        return rrmsg;
    }



    //---------------------------------------------
    //implements ,LocationListener と木の下は後で消す
    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        locationManager.removeUpdates(this);

    }


    //--------------------------
    //リスト内容を全てファイルに書出してListViewを再作成
    //--------------------------
    private void listViewReMake(){
        //一度ファイルを削除
        fileDelete();

        //listViewの内容をファイルに書き出す
        listViewFileSave();

//        Toast.makeText(MainActivity.this, "リストを保存しました", Toast.LENGTH_SHORT).show();

        //リストを削除
        adapter.clear();
        //ファイルからリストを作成
        fileRead();
    }

    //---------------------------------------------
    //listViewの内容をファイルに書き出す
    //---------------------------------------------
    private void listViewFileSave(){
        String rep="";
        //listViewの内容をファイルに書き出す
        for(int i=0;listView.getCount()>i;i++) {
            //listViewのAdpterを取得
            Adapter aa = listView.getAdapter();
            //AdpterのgetItemで1行づつデータを取得
            String bb = (String)aa.getItem(i);

            rep = bb.replaceAll("\n","");
            rep = rep.replaceAll(" 高度", "| 高度");
            rep = rep.replaceAll("メモ","|メモ");
            rep = rep.replaceAll("〒","|〒");
            rep = rep.replaceAll("@","|@");
            Log.d("##replaceAll[改行]###", rep);
            //ファイル出力
            fileOutput(rep);
        }
    }

    //---------------------------------------------
    //データファイル書込
    //---------------------------------------------
    private void fileOutput(String str){

        String fln = OUT_FILE;
        BufferedWriter bufw = null;
        StringBuffer stb = new StringBuffer();

        try {
            FileOutputStream fileOutputStream = openFileOutput(fln, Context.MODE_APPEND);
            bufw = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

            stb.append(str);
            stb.append(BR);
            bufw.write(stb.toString());

            bufw.close();

            //ファイル出力場所
            File aa = getFilesDir();
            Log.d("@@ファイル出力場所@@@@", aa.toString());

            //ファイル一覧
            String bb[]  = fileList();
            Log.d("@@ファイル一覧@@@@", bb[0]);

            fileOutputStream.close();
//            Toast.makeText(MainActivity.this, "データファイルに保存しました", Toast.LENGTH_SHORT).show();

        }
        catch (IOException e) {
        }
    }

    //---------------------------------------------
    //データファイル読込
    //---------------------------------------------
    private void fileRead(){

        String fln = OUT_FILE;

        try {
            FileInputStream fileInputStream;
            fileInputStream = openFileInput(fln);
            BufferedReader reader = new BufferedReader( new InputStreamReader( fileInputStream));

            String tmp;
            String[] dat;
            String readString="";

            while( (tmp = reader.readLine()) != null ){
                Log.v("@@READ@@@@@@@@@@@@@@@@=", tmp);
                dat = tmp.split("\\|");
                readString=( dat[0] +  dat[1] + "\n" +dat[2] +  "\n" + dat[3]+ "\n" + dat[4]);
                //リストにデータを追加
                adapter.add(readString);
            }
            reader.close();

            Log.v("readString", readString);

        }
        catch (IOException e) {
        }
    }

    //---------------------------------------------
    //データファイル->Text
    //---------------------------------------------
    private String fileReadText(){

        String fln = OUT_FILE;
        String readString="";

//        //現在日時を取得する
//        Calendar c = Calendar.getInstance();

//        //フォーマットパターンを指定して表示する
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 E曜日");

//        readString ="【GPSLog　" + sdf.format(c.getTime()) + "】\n";

        try {
            FileInputStream fileInputStream;
            fileInputStream = openFileInput(fln);
            BufferedReader reader = new BufferedReader( new InputStreamReader( fileInputStream));

            String tmp;
            String[] dat;

            while( (tmp = reader.readLine()) != null ){
                dat = tmp.split("\\|");
                dat[4]=dat[4].replace("@","");
                readString= readString + ( dat[0] +  dat[1] + "\n" +dat[2] +  "\n" + dat[3]+ "\n" + "https://www.google.co.jp/maps/place/"+dat[4]+ "\r\n\n");
            }
            reader.close();

            Log.v("readStringText", readString);

        }
        catch (IOException e) {
        }
        return readString;
    }
    //---------------------------------------------
    //データファイル削除
    //---------------------------------------------
    private void fileDelete(){

        String fln = OUT_FILE;
        //ファイルを削除
        deleteFile(fln);
    }

    // GPSが有効かCheck
    // 有効になっていなければ、設定画面の表示確認ダイアログ
    private void chkGpsService() {

        //GPSセンサーが利用可能か？
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPSが有効になっていません。\n有効化しますか？")
                    .setCancelable(false)
                     //GPS設定画面起動用ボタンとイベントの定義
                    .setPositiveButton("GPS設定起動",
                            new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id){
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                }
                            });
            //キャンセルボタン処理
            alertDialogBuilder.setNegativeButton("キャンセル",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id){
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            // 設定画面へ移動するかの問い合わせダイアログを表示
            alert.show();
        }
    }

    // OnResumeでの呼び出し
    @Override
    protected void onResume() {

        chkGpsService();

        super.onResume();
    }
}
