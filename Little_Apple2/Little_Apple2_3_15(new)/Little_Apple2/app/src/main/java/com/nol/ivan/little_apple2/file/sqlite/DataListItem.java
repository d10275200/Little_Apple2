package com.nol.ivan.little_apple2.file.sqlite;

/**
 * Created by Ivan on 2017/1/3.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2016/7/19.
 */
// 資料功能類別
public class DataListItem {
    //存日期的資料ID
    public static ArrayList<String> Date_ID_List = new ArrayList<String>();

    // 表格名稱
    public static final String TABLE_NAME = "datalist_item";

    // 編號表格欄位名稱，固定不變
    public static final String KEY_ID = "_id";

    // 其它表格欄位名稱
    public static final String DATE_COLUMN = "date";
    public static final String SUBJECT_COLUMN = "subject";
    public static final String STROKENUM_COLUMN = "stroke_num";
    public static final String DATAPATH_COLUMN = "data_path";
    public static final String ISTESTING_COLUMN = "is_testing";
    public static final String ISUPDATED_COLUMN = "is_updated";
    public static final String OFFSET_COLUMN = "offset";
    public static final String MATCH_TRAINING_ID = "match_training_id";
    
    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATE_COLUMN + " TEXT UNIQUE NOT NULL, " +
                    SUBJECT_COLUMN + " TEXT NOT NULL, " +
                    STROKENUM_COLUMN + " INTEGER NOT NULL, " +
                    DATAPATH_COLUMN + " TEXT NOT NULL, " +
                    ISTESTING_COLUMN + " INTEGER NOT NULL, " +
                    ISUPDATED_COLUMN + " INTEGER NOT NULL, " +
                    OFFSET_COLUMN + " INTEGER NOT NULL, " +
                    MATCH_TRAINING_ID + " INTEGER NOT NULL ) ";

    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public DataListItem(Context context) {
        db = SQLiteHandler.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public long insert(String date, String subject, int stroke_num, String dataPath, boolean is_testing, long offset, long match_id) {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(DATE_COLUMN, date);
        cv.put(SUBJECT_COLUMN, subject);
        cv.put(STROKENUM_COLUMN, stroke_num);
        cv.put(DATAPATH_COLUMN, dataPath);
        if(is_testing)
            cv.put(ISTESTING_COLUMN, 1);
        else
            cv.put(ISTESTING_COLUMN, 0);
        cv.put(ISUPDATED_COLUMN, 0);
        cv.put(OFFSET_COLUMN, offset);
        cv.put(MATCH_TRAINING_ID, match_id);

        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);

        return id;
    }

    // 修改參數指定的物件
    public boolean update(long id,boolean isupdated) {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();

        // 加入ContentValues物件包裝的修改資料
        if( isupdated )
            cv.put(ISUPDATED_COLUMN, 1);
        else
            cv.put(ISUPDATED_COLUMN, 0);


        // 設定修改資料的條件為編號
        // 格式為「欄位名稱＝資料」
        String where = KEY_ID + "=" + id;

        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(long id){
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = KEY_ID + "=" + id;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public List<DataItem> getALLTestingData(){
        List<DataItem> result = new ArrayList<>();
        String where = ISTESTING_COLUMN + "=" + 1;
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        while (cursor.moveToNext())
            result.add(getRecord(cursor));

        cursor.close();
        return result;
    }

    public List<DataItem> getDateData(String date){
        List<DataItem> result = new ArrayList<>();
        Cursor cursor=db.rawQuery("SELECT " + KEY_ID + "," + ISTESTING_COLUMN + "," + DATE_COLUMN + " FROM " + TABLE_NAME + " WHERE (" + DATE_COLUMN + " BETWEEN '" + date + "' AND '" + date +" 23:59:59') AND " + ISTESTING_COLUMN + "= 1" , null);
        long getID=0;
        while (cursor.moveToNext()) {
//            result.add(getRecord(cursor));
            getID = cursor.getLong(0);
//            DataItem result2 = new DataItem();
//            result2 = get(getID);
            result.add(get(getID));
        }

        cursor.close();
        return result;
    }


    // 讀取所有記事資料
    public List<DataItem> getAll() {
        List<DataItem> result = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext())
            result.add(getRecord(cursor));

        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public DataItem get(long id) {
        // 準備回傳結果用的物件
        DataItem item = null;
        // 使用編號為查詢條件
        String where = KEY_ID + "=" + id;
        // 執行查詢
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // 如果有查詢結果
        if (cursor.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            Log.e("TAG", String.valueOf(cursor.getLong(0)) + " " + cursor.getString(1)+"-------111");
            Date_ID_List.add(String.valueOf(cursor.getLong(0)));
            item = getRecord(cursor);
        }

        // 關閉Cursor物件
        cursor.close();
        // 回傳結果
        return item;
    }

    // 把Cursor目前的資料包裝為物件
    public DataItem getRecord(Cursor cursor) {
        // 準備回傳結果用的物件
        DataItem result = new DataItem();

        result.id = cursor.getLong(0);
        result.date = cursor.getString(1);
        result.subject = cursor.getString(2);
        result.stroke_num = cursor.getInt(3);
        result.path = cursor.getString(4);
        result.is_testing = cursor.getInt(5);
        result.isupdated = cursor.getInt(6);
        result.offset = cursor.getLong(7);
        result.match_id = cursor.getLong(8);

        // 回傳結果
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext())
            result = cursor.getInt(0);

        return result;
    }


    public class DataItem{
        public long id;
        public String date;
        public String subject;
        public String path;
        public int stroke_num;
        public int is_testing; // 0 for false, 1 for true
        public int isupdated; // 0 for false, 1 for true
        public long offset;
        public long match_id;
    }
}

