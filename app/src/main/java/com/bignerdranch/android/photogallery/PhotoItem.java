package com.bignerdranch.android.photogallery;

/**
 * Created by Administrator on 2017/5/19/019.
 */

public class PhotoItem {
    private String mId;
    private String mTitle;
    private String mImgUrl;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        mImgUrl = imgUrl;
    }
}
