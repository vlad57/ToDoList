package com.example.vladup.appdit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;

    private static String DB_NAME = "Notification.db";

    private static String DB_TABLE = "notificationTable";

    private static String COL_ID = "id";
    private static String COL_DATE = "date";
    private static String COL_TIME = "time";
    private static String COL_NOTIFID = "notifid";
    private static String COL_STATENOTIF = "statenotif";
    private static String COL_IDTASK = "idtask";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + DB_TABLE + "(" +
                COL_ID + " INTEGER PRIMARY KEY," +
                COL_DATE + " TEXT," +
                COL_TIME + " TEXT," +
                COL_NOTIFID + " TEXT," +
                COL_STATENOTIF + " TEXT," +
                COL_IDTASK + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXIST " + DB_TABLE);
        onCreate(db);
    }

    public void addNotif(String date, String time, String notifid, String statenotif,  String idtask) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_TIME, time);
        contentValues.put(COL_NOTIFID, notifid);
        contentValues.put(COL_STATENOTIF, statenotif);
        contentValues.put(COL_IDTASK, idtask);
        db.insert(DB_TABLE, null, contentValues);
        db.close();
    }

    public int getMaxIdNotif(){
        List<Integer> ListIdNotif = new ArrayList<>();
        int maxId;
        String myQuery = "SELECT * FROM " + DB_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(myQuery, null);

        if (cursor.moveToFirst()){
            do {
                ListIdNotif.add(Integer.valueOf(cursor.getString(3)));
            }while (cursor.moveToNext());
        }
        cursor.close();
        if (!ListIdNotif.isEmpty()) {
            maxId = Collections.max(ListIdNotif);
            return maxId;
        }
        return 0;
    }

    public List<ModelNotification> getSpecificNotif(String IdTask){
        List<ModelNotification> ListModel = new ArrayList<>();
        //int newID = IdTask;

        String myQuery = "SELECT * FROM " + DB_TABLE + " WHERE " + COL_IDTASK + " = '" + IdTask + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(myQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ModelNotification model = new ModelNotification();
                model.setDate(String.valueOf(cursor.getString(1)));
                model.setTime(String.valueOf(cursor.getString(2)));
                model.setNotifid(String.valueOf(cursor.getString(3)));
                model.setStatenotif(String.valueOf(cursor.getString(4)));
                model.setIdtask(String.valueOf(cursor.getString(5)));
                ListModel.add(model);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ListModel;
    }

    public void updateNotif(String date, String time, String notifid, String statenotif, String idtask) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_TIME, time);
        contentValues.put(COL_NOTIFID, notifid);
        contentValues.put(COL_STATENOTIF, statenotif);
        contentValues.put(COL_IDTASK, idtask);
        //updating rows
        db.update(DB_TABLE, contentValues, COL_IDTASK + " = ?",
                new String[]{
                        idtask
                }
        );
        db.close();
    }

    public void deleteContact(String Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int newID = Integer.parseInt(Id);

        db.delete(DB_TABLE, COL_ID + " = ?",
                new String[]{
                        Id
                });
        db.close();
    }
}
