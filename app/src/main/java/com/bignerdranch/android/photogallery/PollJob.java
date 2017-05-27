package com.bignerdranch.android.photogallery;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/5/27/027.
 */

public class PollJob extends Job {
    public static final String TAG = "job_poll_tag";
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        // run your job here
        notification("net connection information");
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(PollJob.TAG)
                .setExecutionWindow(1_000L, 2_000L)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .setPersisted(true)
                .build()
                .schedule();
    }

    private void notification(String text) {
        Intent i = PhotoGalleryActivity.newIntent(getContext());
        PendingIntent pi = PendingIntent.getActivity(getContext(), 0, i, 0);

        Notification notification = new NotificationCompat.Builder(getContext())
                .setTicker(text)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(text)
                .setContentText(text)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
        notificationManagerCompat.notify(0, notification);
    }

}
