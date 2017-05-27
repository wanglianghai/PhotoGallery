package com.bignerdranch.android.photogallery;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by Administrator on 2017/5/27/027.
 */

public class PollJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case PollJob.TAG:
                return new PollJob();
            default:
                return null;
        }
    }
}
