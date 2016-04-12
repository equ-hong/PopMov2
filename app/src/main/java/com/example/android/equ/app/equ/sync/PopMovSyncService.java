package com.example.android.equ.app.equ.sync;

/**
 * Created by i on 2016-04-11.
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PopMovSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static PopMovSyncAdapter sPopMovSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("PopMovSyncService", "onCreate - PopMovSyncService");
        synchronized (sSyncAdapterLock) {
            if (sPopMovSyncAdapter == null) {
                sPopMovSyncAdapter = new PopMovSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sPopMovSyncAdapter.getSyncAdapterBinder();
    }
}
