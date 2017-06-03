package com.bignerdranch.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.app.PendingIntent.FLAG_NO_CREATE;

/**
 * Created by Administrator on 2017/5/24/024.
 */
//单例全用静态方法
public class PollService extends IntentService {
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMinutes(1);
    private static final String TAG = "PollService";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }

        QueryPreference.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context) {
        Intent i = newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, FLAG_NO_CREATE);

        return pi != null;
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
        Resources resources = getResources();
        String textNotification;
        if (lastId.equals(lastResultId)) {
            Log.i(TAG, "onHandleIntent: old result");
            textNotification = resources.getString(R.string.old_pictures_title);
        } else {
            Log.i(TAG, "onHandleIntent: new result");
            textNotification = resources.getString(R.string.new_pictures_title);
            QueryPreference.setLastResultID(this, lastId);   //this 代表自己这个类,在内部类里不行
        }

        notification(textNotification);
    }

    private void notification(String text) {
        Intent i = PhotoGalleryActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(text)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(text)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0, notification);
    }

    private boolean isNetWorkAvailableAndConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetWorkAvailable = cm.getActiveNetworkInfo() != null; // != null have
        boolean isNetWorkConnected = cm.getActiveNetworkInfo().isConnected() && isNetWorkAvailable;
        return isNetWorkConnected;
    }
}
