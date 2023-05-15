package com.lelong.sxl_quetnhapkholachi.QR300;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.lelong.sxl_quetnhapkholachi.Constant_Class;
import com.lelong.sxl_quetnhapkholachi.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class qr300 extends AppCompatActivity {

    private qr300DB db=null;
    ListView qralist,qrblist;
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    Cursor cursor_a;
    Cursor cursor_b;
    Button delete,upload,updata;
    private boolean firstDetected=true;
    String nqra01,nqrb01,nqrb02,nqrb04;
    String ID,tempcode;
    int uploadchk;
    JSONArray jsonupload;
    Locale locale;
    SoundPool OKPool,ERRORPool;
    int oksound,errorsound;
    int countb; //單身總數
    TextView vcount;
    CheckBox onlinecheck; //離線作業
    Button btnonlineupdate;
    JSONArray jsononlinedata; //更新資料
    int mYear,mMonth,mDay; //系統日期
    String uDate; //設定日期
    JSONObject ujobject; //上傳資料
    View layout; //自訂dialog
    String chkstring;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr300_main);
        tempcode="";
        ID = Constant_Class.UserID;

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        addControls();
        addEvents();

        cursor_a=db.getAll_a();
        cursor_b=db.getAll_b();
        UpdateAdapter(cursor_a,cursor_b);

    }

    private void addEvents() {
        delete.setOnClickListener(btndelListener);
        upload.setOnClickListener(btnuploadListener);
        updata.setOnClickListener(btnupdatalListener);
        qralist.setOnItemClickListener(listviewaListener);
        qrblist.setOnItemClickListener(listviewbListener);
        //btnonlineupdate.setOnClickListener(btnonlineupdateListener);
        barcodeDetector=new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource=new CameraSource.Builder(this,barcodeDetector).setRequestedPreviewSize(300,300).build();
        cameraSource=new CameraSource.Builder(this,barcodeDetector).setAutoFocusEnabled(true).build();

        //設定dialog畫面
        //LayoutInflater inflater=(LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        //=inflater.inflate(R.layout.qr300_dialog01,(ViewGroup) findViewById(R.id.layout_dialog01));
        surfaceView.setOnClickListener(surclick);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)
                    return;
                try{
                    cameraSource.start(surfaceHolder);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }
            @Override
            //收到掃描資料

            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if(qrCodes.size() !=0 && firstDetected){
                    firstDetected = false;
                    //QRCODE取出
                    final String qr300_code = qrCodes.valueAt(0).displayValue;

                    //若連續讀取同一條碼則不做動作
                    if(tempcode.equals(qr300_code)){
                        firstDetected = true;
                    }else {
                        tempcode = qr300_code;
                        getcode(qr300_code);
                    }
                    //拆解QRCODE
                }

            }

        });
    }

    private void addControls() {
        surfaceView = (SurfaceView) findViewById(R.id.suvqr300);
        qralist=(ListView)findViewById(R.id.qralist);
        qrblist=(ListView)findViewById(R.id.qrblist);
        delete=(Button)findViewById(R.id.delete);
        upload=(Button)findViewById(R.id.upload);
        updata=(Button)findViewById(R.id.updata);

        db=new qr300DB(this);
        db.open();
        OKPool=new SoundPool.Builder().build();
        ERRORPool= new SoundPool.Builder().build();
        oksound=OKPool.load(qr300.this,R.raw.ok,1);
        errorsound=ERRORPool.load(qr300.this,R.raw.error,1);
    }

    //點擊刪除按鈕
    private Button.OnClickListener btndelListener=new Button.OnClickListener(){
        public void onClick(View view){
            if(onlinecheck.isChecked()==true) {
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(qr300.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.M11));
                builder.setMessage(getString(R.string.M12));
                builder.setNegativeButton((getString(R.string.M03)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }else if(db.geta()>0){
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(qr300.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.M11));
                builder.setMessage(getString(R.string.M13));
                builder.setNegativeButton((getString(R.string.M03)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }else {
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(qr300.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.M05));
                builder.setMessage(getString(R.string.M10));
                builder.setNegativeButton(getString(R.string.M04), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton(getString(R.string.M03), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Cursor j = db.getAll_d();
                                    jsonupload = cur2Json(j);
                                    String del = deleteqrcodeAll("http://172.16.40.20/"+ Constant_Class.server +"/QR300/deleteqrcodeAll.php");
                                    if (del.equals("true")) {


                                    } else {
                                        Toast alert = Toast.makeText(getApplicationContext(), getString(R.string.E09), Toast.LENGTH_LONG);
                                        alert.show();
                                    }
                                } catch (Exception e) {

                                } finally {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            db.close();
                                            db.open();
                                            cursor_a = db.getAll_a();
                                            cursor_b = db.getAll_b();
                                            UpdateAdapter(cursor_a, cursor_b);
                                        }
                                    });
                                }

                            }
                        }).start();
                    }
                });
                builder.show();
            }
        }
    };
    //點擊上傳按鈕
    private Button.OnClickListener btnuploadListener=new Button.OnClickListener(){
        public void onClick(View view){
            chkstring="";
            if(db.geta()>0){
                AlertDialog.Builder builder = new
                        AlertDialog.Builder(qr300.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.M11));
                builder.setMessage(getString(R.string.M13));
                builder.setNegativeButton((getString(R.string.M03)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }else {
                //檢查入庫量是否超過
                final Thread chkqty= new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Cursor chkqty=db.chkqty();
                            jsonupload=cur2Json(chkqty);
                            String rchkqty=APIchkqty("http://172.16.40.20/PHP/QR300/qr300_chkqty.php");
                            if(rchkqty.equals("TRUE")){
                                uploadchk=1;
                            }else if(rchkqty.equals("FALSE")){
                                uploadchk=-2; //檢查失敗
                            }else{
                                chkstring=rchkqty;
                                uploadchk=-3; //上傳量超過工單量
                            }
                        }catch (Exception e){

                        }
                    }
                });
                //資料上傳至asfi511 asft700 asft620
                final Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (db.upload(ID) == true && uploadchk>0) {
                            Cursor j = db.getAll_b1();
                            jsonupload = cur2Json(j);

                            try {
                                ujobject=new JSONObject();
                                ujobject.put("udate",uDate);
                                ujobject.put("ujson",jsonupload);
                            } catch (JSONException e) {
                                uploadchk=0;
                            }
                            String jArry = upload_all("http://172.16.40.20/PHP/QR300/qr300_upload.php");
                            if (jArry.equals("false")) {
                                uploadchk = 0;
                            } else if (jArry.equals("nodata")) {
                                uploadchk = -1;
                            } else {
                                try {
                                    JSONObject Json = new JSONObject(jArry);
                                    String j01 = Json.getString("r01");
                                    String j02 = Json.getString("r02");
                                    String j03 = Json.getString("r03");
                                    if (db.update_jtable(j01, j02, j03) == 0) {
                                        uploadchk = 0;
                                    } else {
                                        uploadchk = 1;
                                    }
                                } catch (Exception e) {
                                    uploadchk = 0;
                                }
                            }
                        }
                    }
                });
                //寄發MAIL
                final Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (uploadchk > 0) {
                            Cursor j = db.getAll_j();
                            jsonupload = cur2Json(j);
                            String mail = qr300_mail("http://172.16.40.20/PHP/QR300/qr300mail.php");
                        }
                    }
                });
                //傳送LINE訊息
                final Thread thread3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (uploadchk > 0) {
                            Cursor j = db.getAll_j();
                            jsonupload = cur2Json(j);
                            String line = line_notify("http://172.16.40.20/PHP/QR300/line_notify.php");
                        }
                    }
                });
                //將畫面清空
                final Thread thread4 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (uploadchk > 0) {
                                db.close();
                                db.open();
                                cursor_a = db.getAll_a();
                                cursor_b = db.getAll_b();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        UpdateAdapter(cursor_a, cursor_b);
                                    }
                                });

                            }
                        } catch (Exception e) {

                        }
                    }
                });
                new Thread() {
                    public void run() {
                        //選擇日期
                        final HandlerThread mHandlerThread = new HandlerThread("handlerThread");
                        mHandlerThread.start();
                        new Handler(mHandlerThread.getLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    final Calendar c = Calendar.getInstance();
                                    mYear = c.get(Calendar.YEAR);
                                    mMonth=c.get(Calendar.MONTH);
                                    mDay=c.get(Calendar.DAY_OF_MONTH);

                                    final DatePickerDialog dlgDatePicker = new DatePickerDialog(qr300.this, new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePicker view, int year, int month, int day) {
                                            c.set(year,month,day);
                                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                                            uDate=format.format(c.getTime());

                                        }

                                    },mYear,mMonth,mDay);
                                    dlgDatePicker.setCancelable(false);
                                    dlgDatePicker.show();

                                }catch (Exception e){

                                }finally {

                                }

                            }
                        });
                        qr300.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new
                                        AlertDialog.Builder(qr300.this);
                                builder.setTitle(getString(R.string.M01));
                                builder.setMessage(getString(R.string.M02));
                                builder.setNegativeButton(getString(R.string.M04), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        firstDetected = true;
                                    }
                                });
                                builder.setPositiveButton(getString(R.string.M03), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        chkqty.start();
                                        try {
                                            chkqty.join();
                                        } catch (InterruptedException e) {
                                        }
                                        thread1.start();
                                        try {
                                            thread1.join();
                                        } catch (InterruptedException e) {
                                        }
                                        thread2.start();
                                        try {
                                            thread2.join();
                                        } catch (InterruptedException e) {
                                        }
                                        thread3.start();
                                        try {
                                            thread3.join();
                                        } catch (InterruptedException e) {
                                        }
                                        thread4.start();
                                        try {
                                            thread4.join();
                                        } catch (InterruptedException e) {
                                        }
                                        AlertDialog.Builder builder = new
                                                AlertDialog.Builder(qr300.this);
                                        builder.setTitle(getString(R.string.M01));
                                        if (uploadchk == -1) {
                                            builder.setMessage(getString(R.string.M07));
                                        } else if(uploadchk==-2){
                                            builder.setMessage(getString(R.string.M15));
                                        }else if(uploadchk==-3){
                                            builder.setMessage(getString(R.string.M16)+chkstring);
                                        } else if (uploadchk == 1) {
                                            builder.setMessage(getString(R.string.M08));
                                        } else {
                                            builder.setMessage(getString(R.string.M09));
                                        }
                                        builder.setPositiveButton(getString(R.string.M03), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        });
                                        builder.show();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                }.start();


            }
        }
    };
    //點擊上傳結果按鈕
    private Button.OnClickListener btnupdatalListener=new Button.OnClickListener(){
        public void onClick(View view){

            Intent qrupdate = new Intent();
            qrupdate.setClass(qr300.this, qr300updata.class);
            Bundle bundle=new Bundle();
            qrupdate.putExtras(bundle);
            startActivity(qrupdate);

        }
    };
    //點擊更新按鈕
    /*private Button.OnClickListener btnonlineupdateListener=new Button.OnClickListener(){
        public void onClick(View view){
            Thread thread=new Thread() {
                @Override
                public void run() {
                    try{
                        Cursor offlinedata=db.getb_offline();
                        jsononlinedata= cur2Json(offlinedata);
                        String check = onlineupdate("http://172.16.40.20/PHP/QR300/qr300_onlineupdate.php");
                        if(check.equals("TRUE")){
                            for(int i=0;i<jsononlinedata.length();i++){
                                String qra01="";
                                double qra02=0;
                                double qra03=0;
                                double qra04=0;
                                String qrb01="";
                                double qrb02=0;
                                String qrb03="";
                                String qrb04="";
                                double qrb05=0;
                                String qrb06="";
                                String tc_plb013="";
                                JSONObject jso=jsononlinedata.getJSONObject(i);
                                qra01=jso.getString("TC_PLB001");
                                qra03=Integer.parseInt(jso.getString("TC_COUNT"));
                                qra04=Integer.parseInt(jso.getString("TC_PLB010"));
                                qrb01=jso.getString("TC_PLB001");
                                qrb02=Integer.parseInt(jso.getString("TC_PLB002"));
                                qrb03 = jso.getString("TC_PLB003");
                                qrb04 = jso.getString("IMA021");
                                qrb05 = jso.getDouble("TC_PLB006");
                                qrb06 = jso.getString("TC_PLB016");
                                tc_plb013=jso.getString("TC_PLB013");
                                if(tc_plb013.equals("1")){
                                    db.offlineupdate(qra01,qra02,qra03,qra04,qrb01,qrb02,qrb03,qrb04,qrb05,qrb06);
                                }

                            }
                            onlinecheck.setChecked(false);

                        }else{
                        }

                    }catch (Exception e){

                    }finally {




                    }
                }
            };
            Thread thread1= new Thread(){
                @Override
                public void run() {
                    db.delofflinedata();
                    cursor_a=db.getAll_a();
                    cursor_b=db.getAll_b();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateAdapter(cursor_a, cursor_b);
                        }
                    });
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            thread1.start();
            try {
                thread1.join();
            } catch (InterruptedException e) {
            }

        }
    };*/
    //顯示單頭工單資料
    private ListView.OnItemClickListener listviewaListener=
            new ListView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                    TextView qra01=(TextView)v.findViewById(R.id.qra01);

                    nqra01=qra01.getText().toString();
                    cursor_a=db.getAll_a();
                    cursor_b=db.get(nqra01);
                    UpdateAdapter(cursor_a,cursor_b);

                }
            };
    //點擊單身事件
    private ListView.OnItemClickListener listviewbListener=
            new ListView.OnItemClickListener(){
                public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                    TextView qrb01=(TextView)v.findViewById(R.id.qrb01);
                    TextView qrb02=(TextView)v.findViewById(R.id.qrb02);
                    TextView qrb04=(TextView)v.findViewById(R.id.qrb04);
                    nqrb01=qrb01.getText().toString();
                    nqrb02=qrb02.getText().toString();
                    nqrb04=qrb04.getText().toString();
                    //判斷是否為離線資料
                    if(nqrb04.length()>0){
                        //判斷是否離線作業
                        if(onlinecheck.isChecked()==true){
                            AlertDialog.Builder builder = new
                                    AlertDialog.Builder(qr300.this);
                            builder.setCancelable(false);
                            builder.setTitle(getString(R.string.M11));
                            builder.setMessage(getString(R.string.M12));
                            builder.setNegativeButton((getString(R.string.M03)), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            builder.show();
                        }else{
                            AlertDialog.Builder builder = new
                                    AlertDialog.Builder(qr300.this);
                            builder.setTitle(getString(R.string.M05));
                            builder.setMessage(getString(R.string.tqrb01) + nqrb01 +" "+getString(R.string.tqrb02) + nqrb02);
                            builder.setNegativeButton(getString(R.string.M04), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            builder.setPositiveButton(getString(R.string.M03), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String del = deleteqrcode("http://172.16.40.20/PHP/QR300/deleteqrcode.php?qr01=" + nqrb01 + "&qr02=" + nqrb02);
                                                if(del.equals("true")){
                                                    db.delete(nqrb01,nqrb02);
                                                    cursor_a=db.getAll_a();
                                                    cursor_b=db.get(nqrb01);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            UpdateAdapter(cursor_a, cursor_b);
                                                        }
                                                    });
                                                }else{
                                                    Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E09),Toast.LENGTH_LONG);
                                                    alert.show();
                                                }
                                            }catch (Exception e){

                                            }

                                        }
                                    }).start();
                                }
                            });
                            builder.show();
                        }

                    }else {
                        AlertDialog.Builder builder = new
                                AlertDialog.Builder(qr300.this);
                        builder.setTitle(getString(R.string.M05));
                        builder.setMessage(getString(R.string.tqrb01) + nqrb01 +" "+getString(R.string.tqrb02) + nqrb02);
                        builder.setNegativeButton(getString(R.string.M04), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.setPositiveButton(getString(R.string.M03), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            db.delete2(nqrb01,nqrb02);
                                            cursor_a=db.getAll_a();
                                            cursor_b=db.get(nqrb01);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                        UpdateAdapter(cursor_a, cursor_b);
                                                }
                                            });
                                        }catch (Exception e){

                                        }

                                    }
                                }).start();
                            }
                        });
                        builder.show();

                    }

                }
            };
    //更新listview
    public void UpdateAdapter(Cursor cursor_a,Cursor cursor_b){
        try {
            if (cursor_a != null && cursor_a.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.qr300_view_a, cursor_a,
                        new String[]{"qra01", "qra02", "qra03","qra04"}, new int[]{R.id.qra01, R.id.qra02, R.id.qra03,R.id.qra04}, 0);
                qralist.setAdapter(adapter);
                SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this, R.layout.qr300_view_b, cursor_b,
                        new String[]{"qrb01", "qrb02", "qrb03", "qrb04","qrb05"}, new int[]{R.id.qrb01, R.id.qrb02, R.id.qrb03, R.id.qrb04,R.id.qrb05}, 0);
                qrblist.setAdapter(adapter2);
                //countb=db.getCount_b();
                //vcount.setText(Integer.toString(countb));

            }
            else {
                Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E03),Toast.LENGTH_LONG);
                alert.show();
            }
        }catch (Exception e){
            Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E04)+e,Toast.LENGTH_LONG);
            alert.show();
        }finally {
            firstDetected=true;
        }
    }
    protected void onDestroy(){
        super.onDestroy();
    }
    //解析掃描資料
    private void getcode(final String qr300_code){

        try {
            int qr01_index = qr300_code.indexOf('_');
            int qr02_index = qr300_code.indexOf('_', qr01_index + 1);
            final String qr01 = qr300_code.substring(0, qr01_index);
            final String qr02 = qr300_code.substring(qr01_index + 1, qr02_index);
            //判斷是否為離線作業
            if(onlinecheck.isChecked()==true) {
                if (db.getb(qr01,qr02)>0){
                    ERRORPool.play(errorsound,1,1,0,0,1);
                    Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E05),Toast.LENGTH_LONG);
                    alert.show();

                }else{
                    String qra01=qr01;
                    double qrb02=Integer.parseInt(qr02);
                    if (db.append2(qra01,qrb02)>0){
                        cursor_a=db.getAll_a();
                        cursor_b=db.getAll_b();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UpdateAdapter(cursor_a, cursor_b);
                            }
                        });
                        OKPool.play(oksound,1,1,0,0,1);

                    }else{
                        ERRORPool.play(errorsound,1,1,0,0,1);
                        Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E06),Toast.LENGTH_LONG);
                        alert.show();
                    }
                }
            }else {

                //QRCODE資料確認
                new qr300_b().execute("http://172.16.40.20/PHP/QR300/getqrcode.php?qr01=" + qr01 + "&qr02=" + qr02);
            }
        }catch (Exception e){
            Toast alert = Toast.makeText(getApplicationContext(),"ERROR"+e,Toast.LENGTH_LONG);
            alert.show();

        }finally {
            firstDetected=true;
        }

    }

    //刪除單筆資料
    public String deleteqrcode(String apiUrl) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            String jsonstring1 = reader.readLine();
            reader.close();
            String jsonString = jsonstring1;
            if(jsonString.equals("true")){
                return "true";
            }else{
                return "false";
            }
        }catch (Exception ex) {
            return "false";
        }finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }

    //刪除全部資料
    public String deleteqrcodeAll(String apiUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(jsonupload.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return result;
        }catch (Exception ex) {
            return "false";
        }finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }

    //Cursor 轉 Json
    public JSONArray cur2Json(Cursor cursor){
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        rowObject.put(cursor.getColumnName(i),
                                cursor.getString(i));
                    } catch (Exception e) {
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;

    }

    //將工單資料從資料庫撈出帶入
    private class qr300_b extends AsyncTask<String,Integer,String> {
        String qra01="";
        double qra02=0;
        double qra03=0;
        double qra04=0;
        String qrb01="";
        double qrb02=0;
        String qrb03="";
        String qrb04="";
        double qrb05=0;
        String qrb06="";
        String tc_plb013="";
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection =(HttpURLConnection) url.openConnection();
                connection.setReadTimeout(999999);
                connection.setConnectTimeout(999999);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
                String jsonstring1 = reader.readLine();
                reader.close();
                String jsonString = jsonstring1;
                if(jsonString.equals("false")){
                    return "false";
                }else if(jsonString.equals("false2")){
                    return "false2";
                }else{
                JSONObject jsonObject = new JSONObject(jsonString);
                qra01=jsonObject.getString("TC_PLB001");
                qra03=Integer.parseInt(jsonObject.getString("TC_COUNT"));
                qra04=Integer.parseInt(jsonObject.getString("TC_PLB010"));
                qrb01=jsonObject.getString("TC_PLB001");
                qrb02=Integer.parseInt(jsonObject.getString("TC_PLB002"));
                qrb03 = jsonObject.getString("TC_PLB003");
                qrb04 = jsonObject.getString("IMA021");
                qrb05 = jsonObject.getDouble("TC_PLB006");
                qrb06 = jsonObject.getString("TC_PLB016");
                tc_plb013=jsonObject.getString("TC_PLB013");
                return  "ok";
                 }
            }catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }

        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            //執行中 可以在這邊告知使用者進度
            // super.onProgressUpdate(values);

        }

        protected void onPostExecute(String result) {
            try {
                if (result.equals("ok")){
                    if (db.getb(qrb01,Integer.toString((int) qrb02))>0 || tc_plb013.equals("2")){
                        ERRORPool.play(errorsound,1,1,0,0,1);
                        Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E05),Toast.LENGTH_LONG);
                        alert.show();

                    }else{
                        if (db.append(qra01,qra02,qra03,qra04,qrb01,qrb02,qrb03,qrb04,qrb05,qrb06)>0){
                            cursor_a=db.getAll_a();
                            cursor_b=db.getAll_b();
                            UpdateAdapter(cursor_a,cursor_b);
                            OKPool.play(oksound,1,1,0,0,1);

                        }else{
                            ERRORPool.play(errorsound,1,1,0,0,1);
                            Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E06)+result,Toast.LENGTH_LONG);
                            alert.show();
                        }
                    }
                }else if(result.equals("false")){
                    ERRORPool.play(errorsound,1,1,0,0,1);
                    Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E07),Toast.LENGTH_LONG);
                    alert.show();
                }else if(result.equals("false2")) {
                    ERRORPool.play(errorsound,1,1,0,0,1);
                    Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.M07),Toast.LENGTH_LONG);
                    alert.show();
                }else{
                    ERRORPool.play(errorsound,1,1,0,0,1);
                    Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E08)+result,Toast.LENGTH_LONG);
                    alert.show();
                }
            }catch (Exception e){

            }finally {
                firstDetected=true;
            }
        }
    }
    //上傳資料
    public String upload_all(String apiUrl){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(ujobject.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return result;
        }catch (Exception ex) {
            return "false";
        }finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }
    //寄發MAIL
    public String qr300_mail(String apiUrl) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(jsonupload.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return "ok";
        }catch (Exception ex) {
            return "false";
        }finally {
            if(conn!=null) {
                conn.disconnect();

            }
        }

    }
    //傳送LINE
    public String line_notify(String apiUrl) {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(jsonupload.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return "ok";
        }catch (Exception ex) {
            return "false";
        }finally {
            if(conn!=null) {
                conn.disconnect();

            }
        }

    }
    //離線資料更新
    public String onlineupdate(String apiUrl){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(jsononlinedata.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            if(result.equals("FALSE")){
                return "FALSE";
            }else{
                jsononlinedata = new JSONArray(result);
                return "TRUE";
            }
        }catch (Exception ex) {
            return "FALSE";
        }finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }
    //設定語言
    private void setLanguage() {
        SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
        int language = preferences.getInt("Language", 0);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        switch (language){
            case 0:
                configuration.setLocale(Locale.TRADITIONAL_CHINESE);
                break;
            case 1:
                locale=new Locale("vi");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;
            case 2:
                locale=new Locale("en");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;
        }
        resources.updateConfiguration(configuration,displayMetrics);
    }

    //手動輸入工單
    private SurfaceView.OnClickListener surclick=new SurfaceView.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(qr300.this);
            builder.setTitle(getString(R.string.M14));
            builder.setCancelable(false);
            builder.setView(layout);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String code;
                    String code1 = null;
                    String code2 = null;
                    EditText edtext1,edtext2;
                    edtext1=(EditText)(layout).findViewById(R.id.eqra01);
                    edtext2=(EditText)(layout).findViewById(R.id.eqra02);
                    code1=edtext1.getText().toString();
                    code2=edtext2.getText().toString();
                    code=code1+"_"+code2+"_";

                    getcode(code);
                    edtext1.setText("");
                    edtext2.setText("");
                }
            });
            builder.show();

        }
    };
    //檢查上傳量
    public String APIchkqty(String apiUrl){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            writer.write(jsonupload.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            if(result.equals("TRUE")) {
                return "TRUE";
            }else if(result.equals("FALSE")){
                return "FALSE";
            }else{
                return result;
            }
        }catch (Exception ex) {
            return "FALSE";
        }finally {
            if(conn!=null) {
                conn.disconnect();
            }
        }
    }

}