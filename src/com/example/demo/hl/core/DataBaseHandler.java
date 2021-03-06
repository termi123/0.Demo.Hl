package com.example.demo.hl.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.demo.hl.bean.DoujinBean;
import com.example.demo.hl.bean.URLBean;
import com.example.demo.hl.bean.UserBean;
import com.example.demo.hl.util.Helper;

public class DataBaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "doujinsDB";

	// Settings table name
	private static final String TABLE_SETTINGS = "settings";
	private static final String TABLE_DOUJIN = "doujin";

	// Commun Table Columns names
	private static final String KEY_ID = "id";

	// Doujin Table Columns names
	private static final String KEY_URL = "url";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
//	private static final String KEY_ARTIST = "artist";
//	private static final String KEY_TAGS = "tags";
//	private static final String KEY_SERIE = "serie";
    private static final String KEY_QTY_PAGES = "qty_pages";

	// Settings Table Columns names
	private static final String KEY_USER = "user";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_CHECKED = "checked";

	public DataBaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + "("
				+ KEY_ID + " TEXT PRIMARY KEY," + KEY_USER + " TEXT,"
				+ KEY_PASSWORD + " TEXT" + "," + KEY_CHECKED
				+ " INTEGER" + ")";

		String CREATE_TABLE_DOUJIN = "CREATE TABLE " + TABLE_DOUJIN + "("
				+ KEY_ID + " TEXT PRIMARY KEY," + KEY_URL + " TEXT,"
				+ KEY_TITLE + " TEXT" + "," 
//				+ KEY_ARTIST + " TEXT" + ","
//				+ KEY_TAGS + " TEXT" + "," 
				+ KEY_DESCRIPTION + " TEXT" + ","
				+ KEY_QTY_PAGES + " INTEGER" 
//				+ "," + KEY_SERIE + " TEXT" 
				+ ")";

		db.execSQL(CREATE_TABLE_SETTINGS);
		db.execSQL(CREATE_TABLE_DOUJIN);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOUJIN);

		// Create tables again
		onCreate(db);
	}

	public UserBean addSetting() {
		UserBean bean = new UserBean();
		bean.setUser("");
		bean.setPassword("");
		bean.setChecked(false);

		Calendar c = Calendar.getInstance();

		int random = (int) (Math.random() * 7 + 3);
		c.add(Calendar.DATE, random);
		Date dateMessageHelp = c.getTime();

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_USER, bean.getUser());
		values.put(KEY_PASSWORD, bean.getPassword());
		values.put(KEY_CHECKED, bean.isChecked() ? 1 : 0);

		// Inserting Row
		db.insert(TABLE_SETTINGS, null, values);
		db.close(); // Closing database connection

		return bean;
	}

	public DoujinBean addDoujin(DoujinBean bean) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_ID, bean.getId());
		values.put(KEY_URL, bean.getUrl());
//		values.put(KEY_ARTIST, bean.getArtist().getDescription() + "|"
//				+ bean.getArtist().getUrl());
//		values.put(KEY_SERIE, bean.getSerie().getDescription() + "|"
//				+ bean.getSerie().getUrl());
//		values.put(KEY_TAGS, bean.getTagsWithURL());
		values.put(KEY_DESCRIPTION, bean.getDescription());
		values.put(KEY_QTY_PAGES, bean.getQtyPages());
		values.put(KEY_TITLE, bean.getTitle());

		// Inserting Row
		db.insert(TABLE_DOUJIN, null, values);
		db.close(); // Closing database connection

		return bean;
	}

	public UserBean getSetting() {
		UserBean result = null;
		// Select All Query
		String selectQuery = "SELECT " + KEY_ID + "," + KEY_USER + ","
				+ KEY_PASSWORD + "," + KEY_CHECKED + " FROM " + TABLE_SETTINGS;

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			result = new UserBean();
			result.setUser(cursor.getString(1));
			result.setPassword(cursor.getString(2));
			result.setChecked(cursor.getInt(3) == 1);
		}

		db.close();
		// return contact list
		return result;
	}
	
	public DoujinBean getDoujinBean(String id) {
		DoujinBean result = null;
		// Select All Query
		String selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + ","
				+ KEY_DESCRIPTION + ","
//				+ KEY_ARTIST + "," 
//				+ KEY_TAGS + ","
//				+ KEY_SERIE + "," 
				+ KEY_QTY_PAGES + "," + KEY_URL + " FROM " + TABLE_DOUJIN
				+ " WHERE " + KEY_ID + "='" + id + "'";

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			result = new DoujinBean();
			result.setTitle(cursor.getString(1));
			result.setDescription(cursor.getString(2));
//			result.setArtist(Helper.castURLBean(cursor.getString(3)));
			List<URLBean> lstTags = new ArrayList<URLBean>();
			String tags = cursor.getString(4);
			String[] tags_list = tags.split(",");
			for (String str : tags_list) {
				lstTags.add(Helper.castURLBean(str));
			}
//			result.setLstTags(lstTags);
//			result.setSerie(Helper.castURLBean(cursor.getString(5)));
			result.setQtyPages(cursor.getInt(6));
			result.setUrl(cursor.getString(7));
		}

		db.close();
		// return contact list
		return result;
	}

	public LinkedList<DoujinBean> getDoujinList(int numPage, String query, boolean alphabeticalOrder) {
		LinkedList<DoujinBean> result = new LinkedList<DoujinBean>();
		int pageSize = 20;
		// Select All Query
        String selectQuery = "";
        if(alphabeticalOrder){
            selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + ","
                    + KEY_DESCRIPTION + "," 
//            		+ KEY_ARTIST + "," 
//                    + KEY_TAGS + ","
//                    + KEY_SERIE + "," 
                    + KEY_QTY_PAGES + "," + KEY_URL + " FROM " + TABLE_DOUJIN +
                    " WHERE " + KEY_TITLE + " LIKE '%" + query + "%'" +
//                    		" OR "
//                    + KEY_SERIE + " LIKE '%" + query + "%' OR "
//                    + KEY_ARTIST + " LIKE '%" + query + "%'" +
                    " ORDER BY " + KEY_TITLE + " LIMIT "+((numPage-1)*pageSize)+"," + pageSize;
        }else{
            selectQuery = "SELECT " + KEY_ID + "," + KEY_TITLE + ","
                    + KEY_DESCRIPTION + "," 
//            		+ KEY_ARTIST + "," + KEY_TAGS + ","
//                    + KEY_SERIE + ","
            		+ KEY_QTY_PAGES + "," + KEY_URL + " FROM " + TABLE_DOUJIN +
                    " WHERE " + KEY_TITLE + " LIKE '%" + query + "%'" +
//                    		" OR "
//                    + KEY_SERIE + " LIKE '%" + query + "%' OR "
//                    + KEY_ARTIST + " LIKE '%" + query + "%'" +
                    " ORDER BY rowid DESC LIMIT "+((numPage-1)*pageSize)+"," + pageSize;
        }

		Log.i(this.getClass().toString(), selectQuery);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst())
			do{
				DoujinBean bean = new DoujinBean();
				bean.setTitle(cursor.getString(1));
				bean.setDescription(cursor.getString(2));
//				bean.setArtist(Helper.castURLBean(cursor.getString(3)));
				List<URLBean> lstTags = new ArrayList<URLBean>();
				String tags = cursor.getString(4);
				String[] tags_list = tags.split(",");
				for (String str : tags_list) {
					lstTags.add(Helper.castURLBean(str));
				}
//				bean.setLstTags(lstTags);
//				bean.setSerie(Helper.castURLBean(cursor.getString(5)));
				bean.setQtyPages(cursor.getInt(6));
				bean.setUrl(cursor.getString(7));
				result.add(bean);
			}while (cursor.moveToNext());

		db.close();
		// return contact list
		return result;
	}

	public void updateSetting(UserBean bean) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(KEY_USER, bean.getUser());
		values.put(KEY_PASSWORD, bean.getPassword());
		values.put(KEY_CHECKED, bean.isChecked() ? 1 : 0);

		// Inserting Row
		db.update(TABLE_SETTINGS, values, "1=1", new String[] {});
		db.close(); // Closing database connection
	}

	public void deleteDoujin(String id) {
		SQLiteDatabase db = this.getWritableDatabase();

		// Inserting Row
		db.delete(TABLE_DOUJIN, KEY_ID + "=?", new String[] { id });
		db.close(); // Closing database connection
	}

    public static boolean verifyAlreadyDownloaded(DoujinBean bean, Activity activity) {
        try {
            DataBaseHandler db = new DataBaseHandler(activity);
            return db.getDoujinBean(bean.getId()) != null;
        } catch (Exception e) {
            Helper.logError(activity, "Error verifing if exists doujin in the db.", e);
        }

        return false;
    }
}

