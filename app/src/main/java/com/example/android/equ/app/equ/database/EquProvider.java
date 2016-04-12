package com.example.android.equ.app.equ.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.example.android.equ.app.equ.Utility;
import com.example.android.equ.app.equ.database.EquContract.MovEntry;
import com.example.android.equ.app.equ.database.EquContract.SortOrderEntry;
import com.example.android.equ.app.equ.database.EquContract.FavoriteEntry;

/**
 * Created by i on 2016-03-20.
 */
public class EquProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private EquHelper mOpenHelper;

    static final int MOV = 1000;
    static final int MOV_WITH_ORDER = 2000;
    static final int MOV_WITH_ORDER_AND_RANKING = 3000;
    static final int ORDER = 4000;
    static final int FAVORITE = 5000;
    static final int FAVORITE_WITH_ID = 6000;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EquContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, EquContract.PATH_MOV, MOV);
        matcher.addURI(authority, EquContract.PATH_MOV + "/*", MOV_WITH_ORDER);
        matcher.addURI(authority, EquContract.PATH_MOV + "/*/#", MOV_WITH_ORDER_AND_RANKING);
        matcher.addURI(authority, EquContract.PATH_ORDER, ORDER);
        matcher.addURI(authority, EquContract.PATH_FAVORITE, FAVORITE);
        matcher.addURI(authority, EquContract.PATH_FAVORITE + "/*", FAVORITE_WITH_ID);

        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOV:
                return MovEntry.CONTENT_TYPE;
            case MOV_WITH_ORDER:
                return MovEntry.CONTENT_TYPE;
            case MOV_WITH_ORDER_AND_RANKING:
                return MovEntry.CONTENT_ITEM_TYPE;
            case ORDER:
                return SortOrderEntry.CONTENT_TYPE;
            case FAVORITE:
                return FavoriteEntry.CONTENT_TYPE;
            case FAVORITE_WITH_ID:
                return FavoriteEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new EquHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case MOV:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EquContract.MovEntry.TABLE_MOV,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            }
            case MOV_WITH_ORDER:
            {
                String orderSetting = EquContract.MovEntry.getOrderSettingFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EquContract.MovEntry.TABLE_MOV +
                                " INNER JOIN " + SortOrderEntry.TABLE_ORDER +
                                " ON " + MovEntry.TABLE_MOV + "." + MovEntry.COLUMN_ORDER_KEY +
                                " = " + SortOrderEntry.TABLE_ORDER + "." + SortOrderEntry._ID,
                        projection,
                        SortOrderEntry.TABLE_ORDER+
                                "." + SortOrderEntry.COLUMN_ORDER_SETTING + " = ? ",
                        new String[]{orderSetting},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOV_WITH_ORDER_AND_RANKING:
            {
                String orderSetting = EquContract.MovEntry.getOrderSettingFromUri(uri);
                int ranking = EquContract.MovEntry.getRankingFromUri(uri);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        EquContract.MovEntry.TABLE_MOV +
                                " INNER JOIN " + SortOrderEntry.TABLE_ORDER +
                                " ON " + MovEntry.TABLE_MOV + "." + MovEntry.COLUMN_ORDER_KEY +
                                " = " + SortOrderEntry.TABLE_ORDER + "." + SortOrderEntry._ID,
                        projection,
                        SortOrderEntry.TABLE_ORDER+
                                "." + SortOrderEntry.COLUMN_ORDER_SETTING + " = ? AND " +
                                MovEntry.COLUMN_RANKING + " = ? ",
                        new String[]{orderSetting, String.valueOf(ranking)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case ORDER:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SortOrderEntry.TABLE_ORDER,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FAVORITE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EquContract.FavoriteEntry.TABLE_FAVORITE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
                break;
            }
            case FAVORITE_WITH_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        EquContract.FavoriteEntry.TABLE_FAVORITE,
                        projection,
                        FavoriteEntry.TABLE_FAVORITE+
                                "." + FavoriteEntry.COLUMN_MOV_ID + " = ?",
                        new String[]{String.valueOf(EquContract.FavoriteEntry.getIdFromUri(uri))},
                        null,
                        null,
                        null
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOV: {
                long _id = db.insert(MovEntry.TABLE_MOV, null, values);
                if ( _id > 0 )
                    returnUri = EquContract.MovEntry.buildMovUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ORDER: {
                long _id = db.insert(SortOrderEntry.TABLE_ORDER, null, values);
                if ( _id > 0 )
                    returnUri = SortOrderEntry.buildOrderUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case FAVORITE: {
                long _id = db.insert(FavoriteEntry.TABLE_FAVORITE, null, values);
                if ( _id > 0 )
                    returnUri = FavoriteEntry.buildFavoriteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOV:
                rowsUpdated = db.update(MovEntry.TABLE_MOV, values, selection,
                        selectionArgs);
                break;
            case ORDER:
                rowsUpdated = db.update(SortOrderEntry.TABLE_ORDER, values, selection,
                        selectionArgs);
                break;
            case FAVORITE:
                rowsUpdated = db.update(FavoriteEntry.TABLE_FAVORITE, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOV:
                rowsDeleted = db.delete(
                        MovEntry.TABLE_MOV, selection, selectionArgs);
                break;
            case ORDER:
                rowsDeleted = db.delete(
                        SortOrderEntry.TABLE_ORDER, selection, selectionArgs);
                break;
            case FAVORITE:
                rowsDeleted = db.delete(
                        FavoriteEntry.TABLE_FAVORITE, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            //case MOV_WITH_ORDER:
            case MOV:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    //String orderKey = EquContract.MovEntry.getOrderSettingFromUri(uri);
                    //db.delete(MovEntry.TABLE_MOV, MovEntry.COLUMN_ORDER_KEY + " = ? ", new String[]{orderKey});
                    String orderKey = Utility.getPreferredOrder(getContext());
                    db.delete(MovEntry.TABLE_MOV, MovEntry.COLUMN_ORDER_KEY + " = ? ", new String[]{orderKey});

                    for (ContentValues value : values) {
                        //normalizeDate(value);
                        long _id = db.insert(MovEntry.TABLE_MOV, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

}
