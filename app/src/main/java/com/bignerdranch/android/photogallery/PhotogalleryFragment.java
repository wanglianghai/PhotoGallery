package com.bignerdranch.android.photogallery;

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
    private List<PhotoItem> mPhotoItemList;


    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotoItemList = new ArrayList<>();
        new FetcherItemTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setAdapter();

        return view;
    }

    private void setAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mPhotoItemList));
        }
    }

    private class FetcherItemTask extends AsyncTask<Void, Void, List<PhotoItem>>{

        @Override
        protected List<PhotoItem> doInBackground(Void... params) {
            return new PhotoFetcher().fetchItem();
        }

        @Override
        protected void onPostExecute(List<PhotoItem> list) {
            mPhotoItemList = list;
            setAdapter();
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
        }

        @Override
        public int getItemCount() {
            return mPhotoItem.size();
        }
    }
}
