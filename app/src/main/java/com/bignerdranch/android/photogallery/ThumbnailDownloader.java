package com.bignerdranch.android.photogallery;

import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
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
    private static final int MESSAGE_PRE_DOWNLOAD = 2;

    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private LruCache<String, Bitmap> mLruCache;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    private ConcurrentMap<T, String> mRequestMap;
    private boolean mHasQuit = false;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    public ThumbnailDownloader() {
        super(TAG);
        mRequestMap = new ConcurrentHashMap<>();
        int cacheSize = 4 * 1024 * 1024; // 4MiB
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        mResponseHandler = new Handler();
       /* mResponseHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_DOWNLOADED) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "handleMessage: downloaded " + mRequestMap.get(target) + target.toString());
                    handlerResponse(target);
                }
            }
        };*/
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
                    Log.i(TAG, "handleMessage: getUrl " + mRequestMap.get(target) + target.toString());
                    handlerRequest(target);
                }

                if (msg.what == MESSAGE_PRE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handlerPre(target);
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

    public void queuePreThumbnail(String url) {
        Log.i(TAG, "get a url: " + url);
        mRequestHandler.obtainMessage(MESSAGE_PRE_DOWNLOAD, url)
                .sendToTarget();
    }

    private void handlerPre(final T target) {
        try {
            cacheLru((String) target);
            Log.i(TAG, "handlerPre: ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlerRequest(final T target) {
        final String url = mRequestMap.get(target);
        
        try {
            final Bitmap bitmap = cacheLru(url);
            Log.i(TAG, "handlerRequest: bitmap create " + target.toString());

   ////         mResponseHandler.obtainMessage(MESSAGE_DOWNLOADED, target)
     //               .sendToTarget();

            mResponseHandler.post(new Runnable() {//在自己的线程
     //              Because mResponseHandler is associated with the main thread’s
    //                    Looper, all of the code inside of run() will be executed on the main thread.
                @Override
                public void run() {
// By the time ThumbnailDownloader finishes downloading the Bitmap,
 //       RecyclerView may have recycled the PhotoHolder and requested a
 //       different URL for it
                    if (url != mRequestMap.get(target) || mHasQuit) {
                        Log.i(TAG, "return handlerRequest: bitmap create " + target.toString());
                        return;
                    }
                        mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);

                }
            });
        } catch (IOException e) {
            Log.e(TAG, "handlerRequest: Error download image", e);
        }
    }

    private Bitmap cacheLru(String url) throws IOException {
        final Bitmap bitmap;
        if (mLruCache.get(url) == null) {
            byte[] bitImg = new PhotoFetcher().getUrlBytes(url);
            bitmap = BitmapFactory.decodeByteArray(bitImg, 0, bitImg.length);
            mLruCache.put(url, bitmap);
        } else {
            bitmap = mLruCache.get(url);
        }
        return bitmap;
    }

    /*private void handlerResponse(T target) {
        if (url != mRequestMap.get(target) || mHasQuit) {
            return;
        }
        mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
    }*/

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
