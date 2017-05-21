package com.bignerdranch.android.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/17/017.
 */

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private ProgressBar mProgressBar;

    private List<PhotoItem> mPhotoItemList;
    private FetcherItemTask mTask;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoItemList = new ArrayList<>();
        setRetainInstance(true);
        mTask = new FetcherItemTask();
        mTask.execute();
        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "onCreate: thumbnail start");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setAdapter();

        mProgressBar = (ProgressBar) view.findViewById(R.id.photo_progress_bar);
        if (mPhotoItemList.size() > 0) {
            mProgressBar.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        mTask.cancel(false);
        mThumbnailDownloader.quit();
        Log.i(TAG, "onDestroy: thumbnail quit");
    }

    private void setAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mPhotoItemList));
        }
    }

    public class FetcherItemTask extends AsyncTask<Void, Integer, List<PhotoItem>> implements PhotoFetcher.ListenPreset{

        @Override
        protected List<PhotoItem> doInBackground(Void... params) {

            return new PhotoFetcher(mTask).fetchItem();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progress = values[0];
            Log.i(TAG, "onProgressUpdate: " + progress);
            mProgressBar.setProgress(progress);
        }

        @Override
        protected void onPostExecute(List<PhotoItem> list) {
            mPhotoItemList = list;
            mProgressBar.setVisibility(View.GONE);
            setAdapter();
        }

        @Override
        public void setPreset(Integer integer) {
            publishProgress(integer);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private ImageView mImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.list_item_text);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_img);
        }

        public void bindItem(PhotoItem item) {
            mTextView.setText(item.getTitle());
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<PhotoItem> mPhotoItem;
        public PhotoAdapter(List<PhotoItem> list) {
            mPhotoItem = list;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater myInflater = LayoutInflater.from(getActivity());
            View view = myInflater.inflate(R.layout.photo_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            holder.bindItem(mPhotoItem.get(position));
            mThumbnailDownloader.queueThumbnail(holder, mPhotoItem.get(position).getImgUrl());
        }

        @Override
        public int getItemCount() {
            return mPhotoItem.size();
        }
    }
}
