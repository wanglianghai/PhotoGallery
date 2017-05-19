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

    public List<PhotoItem> fetchItem() {
        List<PhotoItem> items = new ArrayList<>();
        String url = Uri.parse("https://api.douban.com//v2/movie/top250")
                .buildUpon()
                .appendQueryParameter("start", "0")     //没这字段会无视
                .appendQueryParameter("count", "250")
                .build()
                .toString();
        try {
            String json = getUrlString(url);
            parseItems(items, json);
            Log.i(TAG, "Received JSON: " +
                    json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
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
