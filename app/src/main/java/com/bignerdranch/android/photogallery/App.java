package com.bignerdranch.android.photogallery;

import android.app.Application;

import com.evernote.android.job.JobManager;

/**
 * Created by Administrator on 2017/5/27/027.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
      //  JobManager.create(this).addJobCreator(new PollJobCreator());

     //   PollJob.scheduleJob();
    }
}
