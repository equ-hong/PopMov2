package com.example.android.equ.app.equ.sync;

/**
 * Created by i on 2016-04-11.
 */
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;

import com.example.android.equ.app.equ.BuildConfig;
import com.example.android.equ.app.equ.MovListActivity;
import com.example.android.equ.app.equ.R;
import com.example.android.equ.app.equ.Utility;
import com.example.android.equ.app.equ.database.EquContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

public class PopMovSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = PopMovSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOV_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_MOV_PROJECTION = new String[] {
            EquContract.MovEntry.COLUMN_TITLE,
            EquContract.MovEntry.COLUMN_OVERVIEW,
            EquContract.MovEntry.COLUMN_RELEASE_DATE,
    };

    private static final int COL_TITLE = 0;
    private static final int COL_OVERVIEW = 1;
    private static final int COL_RELEASE_DATE = 2;

    public PopMovSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        String sortOrderQuery = Utility.getPreferredOrder(getContext());

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movJsonStr = null;

        try {
            final String MOV_BASE_URL =
                    "http://api.themoviedb.org/3/movie";

            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOV_BASE_URL).buildUpon()
                    .appendPath(sortOrderQuery)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THEMOVIEDB_API_KEY)
                    .build();
            Log.d("net path", String.valueOf(builtUri));

            URL url = new URL(builtUri.toString());
            Log.d("url", String.valueOf(url));

            // Create the request to OpenMovMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            movJsonStr = buffer.toString();

            final String MOV_RESULTS = "results";

            final String MOV_POSTER_PATH = "poster_path";
            final String MOV_TITLE = "title";
            final String MOV_OVERVIEW = "overview";
            final String MOV_RELEASE_DATE = "release_date";
            final String MOV_VOTE_AVERAGE = "vote_average";
            final String MOV_ID = "id";

            final long orderId = addOrder(sortOrderQuery, sortOrderQuery);

            JSONObject movJson = new JSONObject(movJsonStr);
            JSONArray movArray = movJson.getJSONArray(MOV_RESULTS);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(movArray.length());

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();
            long dateTime = dayTime.setJulianDay(julianStartDay);

            for(int i = 0; i < movArray.length(); i++) {

                String title;
                String overview;
                String releaseDate;
                double voteAverage;
                String posterPath;
                int id;

                JSONObject movObject = movArray.getJSONObject(i);

                posterPath = movObject.getString(MOV_POSTER_PATH);
                title = movObject.getString(MOV_TITLE);
                overview =  movObject.getString(MOV_OVERVIEW);
                releaseDate = movObject.getString(MOV_RELEASE_DATE);
                voteAverage = movObject.getDouble(MOV_VOTE_AVERAGE);
                id = movObject.getInt(MOV_ID);

                if(posterPath != "null") {

                    ContentValues movValues = new ContentValues();

                    movValues.put(EquContract.MovEntry.COLUMN_POSTER_PATH, posterPath);
                    movValues.put(EquContract.MovEntry.COLUMN_TITLE, title);
                    movValues.put(EquContract.MovEntry.COLUMN_RELEASE_DATE, releaseDate);
                    movValues.put(EquContract.MovEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                    movValues.put(EquContract.MovEntry.COLUMN_OVERVIEW, overview);
                    movValues.put(EquContract.MovEntry.COLUMN_ORDER_KEY, orderId);
                    movValues.put(EquContract.MovEntry.COLUMN_CREATE_TIME, dateTime);
                    movValues.put(EquContract.MovEntry.COLUMN_MOV_ID, id);
                    movValues.put(EquContract.MovEntry.COLUMN_RANKING, i + 1);

                    cVVector.add(movValues);

                    final String MOV_BASE_POSTER_PATH = "http://image.tmdb.org/t/p/w185";

                    URL imageUrl = new URL(MOV_BASE_POSTER_PATH + posterPath);
                    URLConnection conn = imageUrl.openConnection();
                    Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());

                    File internalStorage = getContext().getDir("movDir", Context.MODE_PRIVATE);
                    File movPath = new File(internalStorage, posterPath);
                    Log.d("ALARM", String.valueOf(movPath));

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(movPath);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fos);
                        fos.close();
                    } catch (Exception ex) {
                        Log.i("DATABASE", "Problem updating picture", ex);
                    } finally {
                        fos.close();
                    }
                }
            }

            int inserted;

            if ( cVVector.size() > 0 ) {

                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getContext().getContentResolver().bulkInsert(EquContract.MovEntry.CONTENT_URI, cvArray);

                notifyWeather();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    private void notifyWeather() {
        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications ) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                String orderQuery = Utility.getPreferredOrder(context);

                Uri movUri = EquContract.MovEntry.buildMovOrderWithRanking(orderQuery, 1);

                Cursor cursor = context.getContentResolver().query(movUri, NOTIFY_MOV_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    String title = cursor.getString(COL_TITLE);
                    String overview = cursor.getString(COL_OVERVIEW);
                    String releaseDate = cursor.getString(COL_RELEASE_DATE);

                    String appName = context.getString(R.string.app_name);
                    String contentText = overview.substring(0, 20) + "... " + releaseDate;

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getContext())
                                    .setSmallIcon(R.mipmap.ic_popcorn)
                                    .setContentTitle(appName + " : " + title)
                                    .setContentText(contentText);

                    Intent resultIntent = new Intent(context, MovListActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager =
                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(MOV_NOTIFICATION_ID, mBuilder.build());

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
                    editor.commit();
                }
                cursor.close();
            }
        }
    }

    long addOrder(String orderSetting, String orderName) {
        long orderId;

        Cursor orderCursor = getContext().getContentResolver().query(
                EquContract.SortOrderEntry.CONTENT_URI,
                new String[]{EquContract.MovEntry._ID},
                EquContract.SortOrderEntry.COLUMN_ORDER_SETTING + " = ?",
                new String[]{orderSetting},
                null);

        if (orderCursor.moveToFirst()) {
            int orderIdIndex = orderCursor.getColumnIndex(EquContract.SortOrderEntry._ID);
            orderId = orderCursor.getLong(orderIdIndex);
        } else {
            ContentValues orderValues = new ContentValues();

            orderValues.put(EquContract.SortOrderEntry.COLUMN_ORDER_SETTING, orderSetting);
            orderValues.put(EquContract.SortOrderEntry.COLUMN_ORDER_NAME, orderName);

            Uri insertedUri = getContext().getContentResolver().insert(
                    EquContract.SortOrderEntry.CONTENT_URI,
                    orderValues
            );
            orderId = ContentUris.parseId(insertedUri);
        }
        orderCursor.close();

        return orderId;
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( null == accountManager.getPassword(newAccount) ) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        PopMovSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
