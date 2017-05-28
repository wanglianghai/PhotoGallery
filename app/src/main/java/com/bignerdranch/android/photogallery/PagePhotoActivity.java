package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2017/5/28/028.
 */

public class PagePhotoActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context, Uri uri) {
        Intent i = new Intent(context, PagePhotoActivity.class);
        i.setData(uri);
        return i;
    }

    @Override
    protected Fragment newInstance() {
        return PagePhotoFragment.newInstance(getIntent().getData());
    }
}
