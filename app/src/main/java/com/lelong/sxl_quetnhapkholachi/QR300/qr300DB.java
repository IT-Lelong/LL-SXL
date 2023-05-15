 package com.lelong.sxl_quetnhapkholachi.QR300;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;



public class qr300DB {
    public SQLiteDatabase db=null;
    private final static String DATABASE_NAME="qr300DB.db";
    private final static String TABLE_NAME="qra_table";
    private final static String TABLE_NAME2="qrb_table";
    private final static String TABLE_NAME3="json_table";
    private final static String qra01="qra01"; //工單號碼
    private final static String qra02="qra02"; //已掃數量
    private final static String qra03="qra03"; //未掃數量
    private final static String qra04="qra04"; //標籤總數
    private final static String qrb01="qrb01"; //工單號碼
    private final static String qrb02="qrb02"; //序號
    private final static String qrb03="qrb03"; //料號
    private final static String qrb04="qrb04"; //規格
    private final static String qrb05="qrb05"; //數量
    private final static String qrb06="qrb06"; //批號
    private final static String j01="j01"; //工單單號
    private final static String j02="j02"; //料號
    private final static String j03="j03"; //數量
    private final static String j04="j04"; //上傳人員
    private final static String j05="j05"; //發料單號
    private final static String j06="j06"; //移轉單號
    private final static String j07="j07"; //入庫單號


    private final static String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+" ("+qra01+" TEXT PRIMARY KEY,"+qra02+" INTEGER,"+qra03+" INTEGER,"+qra04+" INTEGER)";
    private final static String CREATE_TABLE2="CREATE TABLE "+TABLE_NAME2+" ("+qrb01+" TEXT,"+qrb02+" INTEGER,"+qrb03+" TEXT,"+qrb04+" TEXT,"+qrb05+" INTEGER,"+qrb06+" TEXT," +" PRIMARY KEY(qrb01,qrb02)"+")";
    private final static String CREATE_TABLE3="CREATE TABLE "+TABLE_NAME3+" ("+j01+" TEXT PRIMARY KEY,"+j02+" TEXT,"+j03+" INTEGER,"+j04+" TEXT,"+j05+" TEXT,"+j06+" TEXT,"+j07+" TEXT)";
    private Context mCtx=null;
    public qr300DB(Context ctx){
        this.mCtx=ctx;
    }
    public void open() throws SQLException{
        db=mCtx.openOrCreateDatabase(DATABASE_NAME,0,null);
        try {
            db.execSQL(CREATE_TABLE);
        }catch (Exception e){
        }
        try {
            db.execSQL(CREATE_TABLE2);
        }catch (Exception e){

        }
    }
    public void open_b() throws SQLException{
        db=mCtx.openOrCreateDatabase(DATABASE_NAME,0,null);
        try {
            db.execSQL(CREATE_TABLE3);
        }catch (Exception e){

        }
    }
    public void close(){
        try {
            final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(DROP_TABLE);
            final String DROP_TABLE2 = "DROP TABLE IF EXISTS " + TABLE_NAME2;
            db.execSQL(DROP_TABLE2);
            db.close();
        }catch (Exception e){

        }
    }
    public Cursor getAll_a(){
        return db.query(TABLE_NAME,new String[]{"rowid _id",qra01,qra02,qra03,qra04}
                ,null,null,null,null,qra01,null);
    }
    public Cursor getAll_b(){
        return db.query(TABLE_NAME2,new String[]{"rowid _id",qrb01,qrb02,qrb03,qrb04,qrb05}
                ,null,null,null,null,"_id DESC",null);
    }
    //刪除資料準備
    public Cursor getAll_d(){
        try {
            return db.rawQuery("SELECT qrb01,qrb02 FROM "+TABLE_NAME2,null);
        }catch(Exception e){
            return null;
        }
    }
    //上傳資料準備
    public Cursor getAll_b1(){
        try {
            return db.rawQuery("SELECT qrb01,qrb02,j04 FROM "+TABLE_NAME2+","+TABLE_NAME3+" WHERE qrb01=j01 ORDER BY qrb01,qrb02",null);
        }catch(Exception e){
            return null;
        }
    }

    //上傳結果listview
    public Cursor getAll_c(){
        try {
            return db.query(TABLE_NAME3, new String[]{"rowid _id", j01, j02, j03, j04, j05, j06, j07}
                    , null, null, null, null, j01, null);
        }catch (Exception e) {
            return null;
        }
    }
    //取出上傳結果
    public Cursor getAll_j(){
        try {
            return db.rawQuery("SELECT * FROM "+TABLE_NAME3,null);
        }catch(Exception e){
            return null;
        }

    }
    //顯示同一工單資料
    public Cursor get(String xqra01){
        try {
            if(xqra01.equals("offline")){
                String xqrb04="";
                return db.query(TABLE_NAME2, new String[]{"rowid _id", qrb01, qrb02, qrb03, qrb04, qrb05},
                        qrb04 + "=?", new String[]{xqrb04}, null, null, null, null);
            }else{
                return db.query(TABLE_NAME2, new String[]{"rowid _id", qrb01, qrb02, qrb03, qrb04, qrb05},
                        qrb01 + "=?", new String[]{xqra01}, null, null, null, null);
            }

        }catch (Exception e){
            return null;
        }
    }
    //回傳一筆單身資料
    public int getb(String xqrb01,String xqrb02)throws SQLException{
        Cursor mCursor=db.query(TABLE_NAME2,new String[]{qrb01,qrb02,qrb03,qrb04,qrb05},
                qrb01+"=? AND "+ qrb02+"=?",new String[]{xqrb01,xqrb02},null,null,null,null);
        if (mCursor.getCount()>0 ){
            mCursor.moveToFirst();
            return 1;
        }else {
            return 0;
        }

    }
    //取出離線資料
    public Cursor getb_offline(){
        try {
            return db.rawQuery("SELECT qrb01,qrb02 FROM "+TABLE_NAME2+" WHERE qrb04='' ORDER BY qrb01,qrb02",null);
        }catch(Exception e){
            return null;
        }
    }
    //確認是否有offline資料
    public int geta()throws SQLException{
        Cursor mCursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE qra01='offline'",null);
        if (mCursor.getCount()>0 ){
            mCursor.moveToFirst();
            return 1;
        }else {
            return 0;
        }

    }
    //掃描資料插入
    public long append(String xqra01,double xqra02,double xqra03,double xqra04,String xqrb01,double xqrb02,
                       String xqrb03,String xqrb04,double xqrb05,String xqrb06){
        try {
            ContentValues argsA = new ContentValues();
            ContentValues argsB = new ContentValues();
            argsB.put(qrb01, xqrb01);
            argsB.put(qrb02, xqrb02);
            argsB.put(qrb03, xqrb03);
            argsB.put(qrb04, xqrb04);
            argsB.put(qrb05, xqrb05);
            argsB.put(qrb06, xqrb06);
            Cursor mCursor = db.query(TABLE_NAME, new String[]{qra01, qra02, qra03, qra04},
                    qra01 + "=?", new String[]{xqra01}, null, null, null, null);
            if (mCursor.getCount() > 0) {
                xqra03=xqra03-1;
                db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02+1,qra03="+xqra03+",qra04="+xqra04+" WHERE qra01='" + xqra01 +"'");
                db.insert(TABLE_NAME2, null, argsB);
                return 1;
            } else {
                argsA.put(qra01, xqra01);
                argsA.put(qra02, xqra02 + 1);
                argsA.put(qra03, xqra03 - 1);
                argsA.put(qra04, xqra04);
                db.insert(TABLE_NAME, null, argsA);
                db.insert(TABLE_NAME2, null, argsB);
                return 1;
            }
        }catch (Exception e){
            return 0;
        }
    }
    //離線資料插入
    public long append2(String xqra01,double xqrb02) {
        try {
            ContentValues argsB = new ContentValues();
            argsB.put(qrb01, xqra01);
            argsB.put(qrb02, xqrb02);
            argsB.put(qrb03, "");
            argsB.put(qrb04, "");
            argsB.put(qrb05, "");
            argsB.put(qrb06, "");
            Cursor mCursor = db.query(TABLE_NAME, new String[]{qra01, qra02, qra03, qra04},
                    qra01 + "=?", new String[]{xqra01}, null, null, null, null);
            Cursor offCursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE qra01='offline'",null);
            if (mCursor.getCount() > 0) {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02+1 WHERE qra01='" + xqra01 +"'");
                db.insert(TABLE_NAME2, null, argsB);
                //判斷是否已有離線資料
                if(offCursor.getCount()>0){
                    db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02+1 WHERE qra01='offline'");
                }else{
                    db.execSQL("INSERT INTO "+ TABLE_NAME + "(qra01,qra02) VALUES('offline',1)");
                }
                return 1;
            } else {
                db.execSQL("INSERT INTO "+ TABLE_NAME + "(qra01,qra02) VALUES('"+xqra01+"',1)");
                db.insert(TABLE_NAME2, null, argsB);
                //判斷是否已有離線資料
                if(offCursor.getCount()>0){
                    db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02+1 WHERE qra01='offline'");
                }else{
                    db.execSQL("INSERT INTO "+ TABLE_NAME + "(qra01,qra02) VALUES('offline',1)");
                }
                return 1;
            }

        }catch (Exception e){
            return 0;
        }
    }
    //離線資料更新
    public long offlineupdate(String xqra01,double xqra02,double xqra03,double xqra04,String xqrb01,double xqrb02,
                       String xqrb03,String xqrb04,double xqrb05,String xqrb06){
        try {
            xqra03=xqra03-1;
            db.execSQL("UPDATE " + TABLE_NAME + " SET qra03="+xqra03+",qra04="+xqra04+" WHERE qra01='" + xqra01 +"'");
            db.execSQL("UPDATE " + TABLE_NAME2 + " SET qrb03='"+xqrb03+"',qrb04='"+xqrb04+"',qrb05="+xqrb05+",qrb06='"+xqrb06+
                    "' WHERE qrb01='" + xqrb01 +"' AND qrb02='"+xqrb02+"'");
            return 1;

        }catch (Exception e){
            return 0;
        }

    }
    //更新離線已掃比數
    public long delofflinedata(){
        try {
            int count=0;
            Cursor mCount= db.rawQuery("SELECT count(*) FROM "+TABLE_NAME2+" WHERE qrb04=''",null);
            mCount.moveToFirst();
            count=mCount.getInt(0);
            if(count>0){
                db.execSQL("UPDATE " + TABLE_NAME + " SET qra02="+count+" WHERE qra01='offline'");
            }else{
                db.execSQL("DELETE FROM "+ TABLE_NAME + " WHERE qra01='offline'");
            }
            return 1;
        }catch (Exception e){
            return 0;
        }
    }
    //刪除單筆資料
    public boolean delete(String xqrb01,String xqrb02){
        try {

            db.delete(TABLE_NAME2, qrb01 + "=? AND " + qrb02 + "=?", new String[]{xqrb01, xqrb02});
            Cursor mCursor=db.query(TABLE_NAME2,new String[]{qrb01,qrb02,qrb03,qrb04,qrb05},
                    qrb01+"=?",new String[]{xqrb01},null,null,null,null);
            if (mCursor.getCount()>0 ){
                db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02-1,qra03=qra03+1 WHERE qra01='" + xqrb01 + "'");
            }else {
                db.delete(TABLE_NAME, qra01 + "=?", new String[]{xqrb01});
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
    //刪除單筆離線資料
    public boolean delete2(String xqrb01,String xqrb02){
        try {
            db.delete(TABLE_NAME2, qrb01 + "=? AND " + qrb02 + "=?", new String[]{xqrb01, xqrb02});
            Cursor mCursor=db.query(TABLE_NAME2,new String[]{qrb01,qrb02,qrb03,qrb04,qrb05},
                    qrb01+"=?",new String[]{xqrb01},null,null,null,null);
            if (mCursor.getCount()>0 ){
                db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02-1 WHERE qra01='" + xqrb01 + "'");
            }else {
                db.delete(TABLE_NAME, qra01 + "=?", new String[]{xqrb01});
            }
            //刪除單頭離線資料
            db.execSQL("UPDATE " + TABLE_NAME + " SET qra02=qra02-1 WHERE qra01='offline'");
            Cursor c=db.rawQuery("SELECT qra02 FROM "+TABLE_NAME+" WHERE qra01='offline'",null);
            c.moveToFirst();
            int tqra02=c.getInt(0);
            //若無離線資料則刪除單頭
            if(tqra02==0){
                db.execSQL("DELETE FROM "+ TABLE_NAME + " WHERE qra01='offline'");
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
    //上傳資料準備
    public boolean upload(String cpf01){
        db=mCtx.openOrCreateDatabase(DATABASE_NAME,0,null);
        try {
            //重建上傳json_table
            final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME3;
            db.execSQL(DROP_TABLE);
            db.execSQL(CREATE_TABLE3);
        }catch (Exception e){
        }
        //將上傳資料彙整至json_table
        ContentValues argsC = new ContentValues();
        Cursor c=db.rawQuery("SELECT qrb01,qrb03,SUM(qrb05),'"+cpf01+"' FROM "+TABLE_NAME2+" group by qrb01,qrb03",null);
        if(c.getCount()>0){
            c.moveToFirst();
            do{
                argsC.put("j01",c.getString(0));
                argsC.put("j02",c.getString(1));
                argsC.put("j03",c.getInt(2));
                argsC.put("j04",c.getString(3));
                db.insert(TABLE_NAME3, null, argsC);
            }while (c.moveToNext());
        }else {
            return false;
        }
        return true;
    }
    //上傳結果更新
    public long update_jtable(String r01,String r02,String r03){
        try {
            Cursor c=db.rawQuery("SELECT j01 FROM "+TABLE_NAME3,null);
            c.moveToFirst();
            do{
                int c_index=c.getColumnIndex(j01);
                String l_j01=c.getString(c_index);
                int r02_indexA = r02.indexOf(l_j01)+17;
                int r02_indexA1 = r02_indexA+16;
                int r02_indexB = r02.indexOf(l_j01,r02_indexA+1)+17;
                int r02_indexB1 = r02_indexB+16;
                String l_r02=r02.substring(r02_indexA,r02_indexA1)+' '+r02.substring(r02_indexB,r02_indexB1);
                db.execSQL("UPDATE " + TABLE_NAME3 + " SET j06='"+l_r02+"',j05='"+r01+"',j07='"+r03+"' WHERE j01='"+l_j01+"'");
            }while (c.moveToNext());
            return 1;
        }
        catch (Exception e){
            return 0;
        }
    }
    //計算單身總數
    public int getCount_b(){
        try {
            int count=0;
            Cursor mCount= db.rawQuery("SELECT count(*) FROM "+TABLE_NAME2,null);
            mCount.moveToFirst();
            count=mCount.getInt(0);
            return count;
        }catch(Exception e){
            return 0;
        }
    }

    //取得上傳量
    public Cursor chkqty(){
        Cursor cursor=db.rawQuery("SELECT qrb01,SUM(qrb05) qrb05 FROM "+TABLE_NAME2+" group by qrb01 ",null);
        return cursor;
    }




}
