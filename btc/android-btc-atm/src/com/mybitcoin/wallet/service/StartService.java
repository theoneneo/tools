package com.mybitcoin.wallet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mybitcoin.wallet.ui.WelcomePageActivity;
import com.mybitcoin.wallet.ui.first.MainActivity;

/**
 * Created by zhuyun on 14-4-13.
 */
public class StartService extends  Service {


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("----", "service on create");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.i("test", "service start");
        //new ServiceTask(this).execute();
        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        this.startActivity(startIntent);
        return super.onStartCommand(intent, flags, startId);
    }
}
