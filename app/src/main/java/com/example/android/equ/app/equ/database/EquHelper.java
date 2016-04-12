package com.example.android.equ.app.equ.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.equ.app.equ.database.EquContract.MovEntry;
import com.example.android.equ.app.equ.database.EquContract.SortOrderEntry;
import com.example.android.equ.app.equ.database.EquContract.FavoriteEntry;

/**
 * Created by i on 2016-03-16.
 */
public class EquHelper extends SQLiteOpenHelper {
    private static final String TAG = "EquHelper";
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "equ.db";

    public EquHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + EquContract.MovEntry.TABLE_MOV + "(" +
                        MovEntry._ID + " integer primary key autoincrement, " +
                        MovEntry.COLUMN_ORDER_KEY + " INTEGER NOT NULL, " +
                        MovEntry.COLUMN_RANKING + " INTEGER NOT NULL, " +
                        MovEntry.COLUMN_MOV_ID + " INTEGER NOT NULL, " +
                        MovEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MovEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MovEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        MovEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        MovEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        MovEntry.COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +

                        " FOREIGN KEY (" + MovEntry.COLUMN_ORDER_KEY + ") REFERENCES " +
                        SortOrderEntry.TABLE_ORDER + " (" + MovEntry._ID + ")" +

                        " UNIQUE (" + MovEntry.COLUMN_ORDER_KEY + ", " +
                        MovEntry.COLUMN_RANKING + ") ON CONFLICT REPLACE" +
                        ");"
        );

        db.execSQL("create table " + SortOrderEntry.TABLE_ORDER + "(" +
                        SortOrderEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SortOrderEntry.COLUMN_ORDER_SETTING + " TEXT UNIQUE NOT NULL, " +
                        SortOrderEntry.COLUMN_ORDER_NAME + " TEXT NOT NULL " +
                        ");"
        );

        db.execSQL("create table " + EquContract.FavoriteEntry.TABLE_FAVORITE + "(" +
                        FavoriteEntry._ID + " integer primary key autoincrement, " +
                        FavoriteEntry.COLUMN_ORDER_KEY + " INTEGER, " +
                        FavoriteEntry.COLUMN_RANKING + " INTEGER, " +
                        FavoriteEntry.COLUMN_MOV_ID + " INTEGER NOT NULL, " +
                        FavoriteEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        FavoriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        FavoriteEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        FavoriteEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        FavoriteEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        FavoriteEntry.COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +

                        " UNIQUE (" + FavoriteEntry.COLUMN_MOV_ID  +
                        ") ON CONFLICT REPLACE" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
