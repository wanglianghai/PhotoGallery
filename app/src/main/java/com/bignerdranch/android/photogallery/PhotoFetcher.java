package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by Administrator on 2017/5/17/017.
 */

public class PhotoFetcher {
    private static final String TAG = "PhotoFetcher";

    private ListenPreset mListenPreset;

    public PhotoFetcher() {

    }

    public PhotoFetcher(PhotoGalleryFragment.FetcherItemTask task) {
        mListenPreset = task;
    }

    public interface ListenPreset{
        void setPreset(Integer integer);
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int byteReader;
            byte[] buffer = new byte[1024];
            while ((byteReader = in.read(buffer)) > 0) {
                out.write(buffer, 0, byteReader);
            }

            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
        /*Response response = null;
        try {
            OkHttpClient client = new OkHttpClient.Builder()  只能有一个在一个application中
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build();

            RequestBody body = new FormBody.Builder()
                    .add("start", "0")
                    .add("count", "30")
                    .build();

            Request request = new Request.Builder()
                    .url(urlSpec)
                    .post(body)
                    .build();


            response = client.newCall(request).execute();
            return response.body().bytes();
        } finally {
            response.body().close();
        }*/
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<PhotoItem> fetchItem() {
        List<PhotoItem> items = new ArrayList<>();
        String url = Uri.parse("https://api.douban.com//v2/movie/top250")
                .buildUpon()
                .appendQueryParameter("start", "0")     //没这字段会无视
                .appendQueryParameter("count", "30")
                .build()
                .toString();
        try {
            String json = getUrlString(url);
            parseItems(items, json);
            Log.i(TAG, "Received JSON: " +
                    json);
        } catch (IOException e) {
            Log.e(TAG, "fetchItem: fail to fetch", e);
        } catch (JSONException e) {
            Log.e(TAG, "fetchItem: JSON error", e);
        }

        return items;
    }

    private void parseItems(List<PhotoItem> list, String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("subjects");

        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject itemObject = jsonArray.getJSONObject(i);

            PhotoItem item = new PhotoItem();
            item.setId(itemObject.getString("id"));
            item.setImgUrl(itemObject.getJSONObject("images").getString("medium"));
            item.setTitle(itemObject.getString("title"));

            list.add(item);
            mListenPreset.setPreset(i * 100 / length);
        }
    }

}
