package com.bignerdranch.android.photogallery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.List;

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
        String query = QueryPreference.getPreference(this);
        String lastResultId = QueryPreference.getLastResultID(this);
        List<PhotoItem> item;

        if (query == null) {
            item = new PhotoFetcher().top205Photos();
        } else {
            item = new PhotoFetcher().searchPhotos(query);
        }

        String lastId = item.get(0).getId();
        if (lastId.equals(lastResultId)) {
            Log.i(TAG, "onHandleIntent: old result");
        } else {
            Log.i(TAG, "onHandleIntent: new result");
        }
    }

    private boolean isNetWorkAvailableAndConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetWorkAvailable = cm.getActiveNetworkInfo() != null; // != null have
        boolean isNetWorkConnected = cm.getActiveNetworkInfo().isConnected() && isNetWorkAvailable;
        return isNetWorkConnected;
    }
}
