package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();

        RequestBody body = new FormBody.Builder()
                .add("start", "0")
                .add("count", "1")
                .build();

        Request request = new Request.Builder()
                .url(urlSpec)
                .post(body)
                .build();


        Response response = client.newCall(request).execute();
        return response.body().bytes();
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<PhotoItem> fetchItem() {
        List<PhotoItem> items = new ArrayList<>();
       /* String url = Uri.parse("https://api.douban.com//v2/movie/top250")
                .buildUpon()
                .appendQueryParameter("start", "0")     //没这字段会无视
                .appendQueryParameter("count", "250")
                .build()
                .toString();*/
        try {
            String json = getUrlString("https://api.douban.com//v2/movie/top250");
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

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject itemObject = jsonArray.getJSONObject(i);

            PhotoItem item = new PhotoItem();
            item.setId(itemObject.getString("id"));
            item.setImgUrl(itemObject.getJSONObject("images").getString("medium"));
            item.setTitle(itemObject.getString("title"));

            list.add(item);
        }
    }
}
