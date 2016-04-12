package com.example.android.equ.app.equ.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by i on 2016-03-16.
 */
public class EquContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.equ.app.equ";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOV = "mov";
    public static final String PATH_ORDER = "order";
    public static final String PATH_FAVORITE = "favorite";

    public static final class SortOrderEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ORDER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ORDER;

        public static final String TABLE_ORDER = "sortorder";

        public static final String COLUMN_ORDER_SETTING = "order_setting";
        public static final String COLUMN_ORDER_NAME = "order_name";

        public static Uri buildOrderUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class MovEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOV).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOV;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOV;

        public static final String TABLE_MOV = "movie";

        public static final String COLUMN_ORDER_KEY = "order_id";
        public static final String COLUMN_RANKING = "ranking";
        public static final String COLUMN_MOV_ID = "mov_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_CREATE_TIME = "creation_time";

        public static Uri buildMovUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovOrder(String orderSetting) {
            return CONTENT_URI.buildUpon().appendPath(orderSetting).build();
        }

        public static Uri buildMovOrderWithRanking(String orderSetting, int id) {
            return CONTENT_URI.buildUpon().appendPath(orderSetting)
                    .appendPath(String.valueOf(id)).build();
        }

        public static String getOrderSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static int getRankingFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(2));
        }
    }

    public static final class FavoriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static final String TABLE_FAVORITE= "favorite";

        public static final String COLUMN_ORDER_KEY = "order_id";
        public static final String COLUMN_RANKING = "ranking";
        public static final String COLUMN_MOV_ID = "mov_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_CREATE_TIME = "creation_time";

        public static Uri buildFavoriteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFavoriteUri() {
            return CONTENT_URI;
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }
}
