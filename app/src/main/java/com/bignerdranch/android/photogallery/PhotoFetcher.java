package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/5/17/017.
 */

public class PhotoFetcher {
    private static final String TAG = "PhotoFetcher";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new  Request.Builder()
                .url(urlSpec)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().bytes();
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public void fetcherItem() {
        String url = Uri.parse("https://api.douban.com//v2/movie/top250")
                .buildUpon()
                .appendQueryParameter("format", "json")
                .appendQueryParameter("extras", "url_s")
                .build()
                .toString();
        try {
            String json = getUrlString(url);
            Log.i(TAG, "Received JSON: " +
                    json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
