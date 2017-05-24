package com.bignerdranch.android.photogallery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by Administrator on 2017/5/24/024.
 */

public class PollService extends IntentService {
    private static final String TAG = "PollService";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }
    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetWorkAvailableAndConnection()) {
            return;
        }
        Log.i(TAG, "onHandleIntent: receive a intent" + intent);
    }

    private boolean isNetWorkAvailableAndConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetWorkAvailable = cm.getActiveNetworkInfo() != null; // != null have
        boolean isNetWorkConnected = cm.getActiveNetworkInfo().isConnected() && isNetWorkAvailable;
        return isNetWorkConnected;
    }
}
