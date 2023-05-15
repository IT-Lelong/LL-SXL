package com.lelong.sxl_quetnhapkholachi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class Create_Table {

    private Context mCtx = null;
    String DATABASE_NAME = "KyThuatDB.db";
    public SQLiteDatabase db = null;

    String TABLE_NAME_TC_FAB = "tc_fab_file";
    String tc_fab001 = "tc_fab001"; //Mã hạng mục
    String tc_fab002 = "tc_fab002"; //Mã báo biểu
    String tc_fab003 = "tc_fab003"; //Mã hạng mục chi tiết
    String tc_fab004 = "tc_fab004"; //Mã tổng

    String TABLE_NAME_TC_FAC = "tc_fac_file";
    String tc_fac001 = "tc_fac001"; //Mã hạng mục
    String tc_fac002 = "tc_fac002"; //Mã báo biểu
    String tc_fac003 = "tc_fac003"; //Mã hạng mục chi tiết
    String tc_fac004 = "tc_fac004"; //Mã tổng
    String tc_fac005 = "tc_fac005"; //tên tiếng hoa
    String tc_fac006 = "tc_fac006"; //tên tiếng việt
    String tc_fac007 = "tc_fac007"; //Điểm số
    String tc_fac008 = "tc_fac008"; //Hãng sản xuất
    String tc_fac011 = "tc_fac011"; //Dãy đo thiết bị


    String CREATE_TABLE_FAB = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TC_FAB + " ("
            + tc_fab001 + " TEXT," + tc_fab002 + " TEXT,"
            + tc_fab003 + " TEXT," + tc_fab004 + " TEXT)";

    String CREATE_TABLE_FAC = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TC_FAC + " ("
            + tc_fac001 + " TEXT," + tc_fac002 + " TEXT," + tc_fac003 + " TEXT,"
            + tc_fac004 + " TEXT," + tc_fac005 + " TEXT," + tc_fac006 + " TEXT,"
            + tc_fac007 + " TEXT," + tc_fac008 + " TEXT," + tc_fac011 + " TEXT )";


    public Create_Table(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        try {
            db.execSQL(CREATE_TABLE_FAB);
            db.execSQL(CREATE_TABLE_FAC);
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            String DROP_TABLE_TC_FAB = "DROP TABLE IF EXISTS " + TABLE_NAME_TC_FAB;
            String DROP_TABLE_TC_FAC = "DROP TABLE IF EXISTS " + TABLE_NAME_TC_FAC;
            db.execSQL(DROP_TABLE_TC_FAB);
            db.execSQL(DROP_TABLE_TC_FAC);
            db.close();
        } catch (Exception e) {

        }
    }

    public String append(String g_tc_fab001, String g_tc_fab002, String g_tc_fab003, String g_tc_fab004) {
        try {
            ContentValues args = new ContentValues();
            args.put(tc_fab001, g_tc_fab001);
            args.put(tc_fab002, g_tc_fab002);
            args.put(tc_fab003, g_tc_fab003);
            args.put(tc_fab004, g_tc_fab004);
            db.insert(TABLE_NAME_TC_FAB, null, args);
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String append(String g_tc_fac001, String g_tc_fac002, String g_tc_fac003,
                       String g_tc_fac004, String g_tc_fac005, String g_tc_fac006,
                       String g_tc_fac007, String g_tc_fac008, String g_tc_fac011) {
        try {
            ContentValues args = new ContentValues();
            args.put(tc_fac001, g_tc_fac001);
            args.put(tc_fac002, g_tc_fac002);
            args.put(tc_fac003, g_tc_fac003);
            args.put(tc_fac004, g_tc_fac004);
            args.put(tc_fac005, g_tc_fac005);
            args.put(tc_fac006, g_tc_fac006);
            args.put(tc_fac007, g_tc_fac007);
            args.put(tc_fac008, g_tc_fac008);
            args.put(tc_fac011, g_tc_fac011);
            db.insert(TABLE_NAME_TC_FAC, null, args);
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }

    }

    public void delete_table() {
        db.delete(TABLE_NAME_TC_FAB, null  , null);
        db.delete(TABLE_NAME_TC_FAC, null  , null);
    }

    /*KT01*/
    public Cursor getAll_tc_fab() {
        Cursor a;
        try {
         /*  a  = db.query(TABLE_NAME_TC_FAB, new String[]{"rowid _id", tc_fab001,tc_fab002,tc_fab003,tc_fab004}
                    , null, null, null, null, tc_fab002 + " DESC", null) ;*/
            // a = db.rawQuery("SELECT * FROM " + TABLE_NAME_TC_FAB + " WHERE tc_fab001='KT01'", null);

            //SQLiteDatabase db = this.getWritableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_NAME_TC_FAB + " WHERE tc_fab001='KT01'";
            return db.rawQuery(selectQuery, null);

        } catch (Exception e) {
            return null;
        }
    }
}
