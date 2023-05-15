package com.lelong.sxl_quetnhapkholachi.QR300;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.lelong.sxl_quetnhapkholachi.R;
import java.util.Locale;

public class qr300updata extends AppCompatActivity {
    private qr300DB db=null;
    Cursor jcursor;
    ListView jlist;
    Locale locale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr300updata);

        jlist=(ListView)findViewById(R.id.jsonlist);

        db= new qr300DB(this);
        db.open_b();
        jcursor=db.getAll_c();
        UpdateAdapter(jcursor);
    }
    public void UpdateAdapter(Cursor cursor){
        try {
            if (cursor != null && jcursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.qr300_view_c, cursor,
                        new String[]{"j01", "j02", "j03","j05","j06","j07"}, new int[]{R.id.j01, R.id.j02, R.id.j03,R.id.j05,R.id.j06,R.id.j07}, 0);
                jlist.setAdapter(adapter);
            }
            else {
                Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E03),Toast.LENGTH_LONG);
                alert.show();
            }
        }catch (Exception e){
            Toast alert = Toast.makeText(getApplicationContext(),getString(R.string.E04)+e,Toast.LENGTH_LONG);
            alert.show();
        }
    }
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
}