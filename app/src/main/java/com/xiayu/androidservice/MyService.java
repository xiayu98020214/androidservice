package com.xiayu.androidservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by 七喜 on 2017/5/25.
 */

public class MyService extends Service {

    private static final String TAG = "MyService";

    private DownloadBinder mBinder = new DownloadBinder();


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);

    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return mBinder;
    }

    class DownloadBinder extends Binder {

        public void startDownload(){
            Log.d(TAG, "startDownload: ");
        }

        public int getProgress(){
            Log.d(TAG, "getProgress: ");
            return 0;
        }
    }
}
