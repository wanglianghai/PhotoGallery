package com.bignerdranch.android.photogallery;

import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2017/5/21/021.
 */
//identify each download and to determine which UI element to update
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 1;

    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap;

    private boolean mHasQuit = false;
    public ThumbnailDownloader() {
        super(TAG);
        mRequestMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "handleMessage: getUrl" + mRequestMap.get(target));
                    handlerRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "get a url: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    private void handlerRequest(T target) {
        String url = mRequestMap.get(target);
        
        try {
            byte[] bitImg = new PhotoFetcher().getUrlBytes(url);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitImg, 0, bitImg.length);
            Log.i(TAG, "handlerRequest: bitmap create");
        } catch (IOException e) {
            Log.e(TAG, "handlerRequest: Error download image", e);
        }
    }
}
