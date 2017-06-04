package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Administrator on 2017/6/4/004.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: receiver result:" + getResultCode());

        if (getResultCode() == Activity.RESULT_CANCELED) {
            return;
        }

        int requestCode = getResultCode();
        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat notificationManage = NotificationManagerCompat.from(context);
        notificationManage.notify(requestCode, notification);
    }
}
