package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Administrator on 2017/5/28/028.
 */

public class PagePhotoFragment extends Fragment {
    private static final String TAG = "PagePhotoFragment";
    private static final String ARG_URL = "photo_page_url";

    private Uri mUri;
    private WebView mWebView;

    public static PagePhotoFragment newInstance(Uri uri) {
        Bundle arg = new Bundle();
        arg.putParcelable(ARG_URL, uri);

        PagePhotoFragment photoFragment = new PagePhotoFragment();
        photoFragment.setArguments(arg);
        return photoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_URL);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mWebView = (WebView) v.findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(mUri.toString());
        Log.i(TAG, "onCreateView: url:" + mUri.toString());

        return v;
    }
}
