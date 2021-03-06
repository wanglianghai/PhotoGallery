package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/17/017.
 */

public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private ProgressBar mProgressBar;
    private SearchView mSearchView;

    private List<PhotoItem> mPhotoItemList;
    private FetcherItemTask mTask;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_tool_bar, menu);

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.start_polling);
        } else {
            toggleItem.setTitle(R.string.stop_polling);
        }

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                QueryPreference.setPreference(getActivity(), query);
                collapseAfterSearch();
                updateItem();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                mSearchView.setQuery("", false);
                QueryPreference.setPreference(getActivity(), null);
                updateItem();
                return true;

            case R.id.menu_item_toggle_polling:
                //就两种情况的选择
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);

                getActivity().invalidateOptionsMenu();
                return true;

            default:return super.onOptionsItemSelected(item);
        }
    }

    private void updateItem() {
        mProgressBar.setVisibility(View.VISIBLE);
        mPhotoRecyclerView.setVisibility(View.GONE);
        new FetcherItemTask(QueryPreference.getPreference(getActivity())).execute();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mPhotoItemList = new ArrayList<>();
        setRetainInstance(true);
        mTask = new FetcherItemTask(QueryPreference.getPreference(getActivity()));
        mTask.execute();
       /* mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();*/
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
//        mThumbnailDownloader.clearQueue();
        Log.i(TAG, "onDestroy: thumbnail clear");
    }



    private void setAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mPhotoItemList));
            /*mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    switch (newState) {
                        case RecyclerView.SCROLL_STATE_DRAGGING:
                            mThumbnailDownloader.clearQueue();
                            break;
                        case RecyclerView.SCROLL_STATE_IDLE:
                            GridLayoutManager gridLayoutManager =
                                    (GridLayoutManager) mPhotoRecyclerView.getLayoutManager();
                            PhotoAdapter photoAdapter = (PhotoAdapter) mPhotoRecyclerView.getAdapter();
                            int startingPos = gridLayoutManager.findLastVisibleItemPosition() + 1;
                            int upperLimit = Math.min(startingPos + 10, photoAdapter.getItemCount());
                            for (int i = startingPos; i < upperLimit; i++) {
                                mThumbnailDownloader.queuePreThumbnail(mPhotoItemList.get(i).getImgUrl());
                            }

                            startingPos = gridLayoutManager.findFirstVisibleItemPosition() - 1;
                            int lowerLimit = Math.max(startingPos - 10, 0);
                            for (int i = startingPos; i > lowerLimit; i--) {
                                mThumbnailDownloader.queuePreThumbnail(mPhotoItemList.get(i).getImgUrl());
                            }

                            break;
                    }
                }
            });*/
        }
    }

    //It is intended for work that is short-lived and not repeated too often
    public class FetcherItemTask extends AsyncTask<Void, Integer, List<PhotoItem>> implements PhotoFetcher.ListenPreset {
        private String mQuery;

        public FetcherItemTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<PhotoItem> doInBackground(Void... params) {
            return new PhotoFetcher(mTask).searchPhotos(mQuery);
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
            mPhotoRecyclerView.setVisibility(View.VISIBLE);
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
        private PhotoItem mItem;

        public PhotoHolder(final View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.list_item_text);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_img);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = PagePhotoActivity.newIntent(getActivity(), mItem.getAlt());
                    startActivity(i);
                }
            });
        }

        public void bindItem(PhotoItem item) {
            mItem = item;
            Picasso.with(getActivity()).
                    load(item.getImgUrl()).
                    placeholder(R.drawable.ic_action_wait).
                    into(mImageView);
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
            //          holder.bindDrawable(getResources().getDrawable(R.drawable.ic_action_wait));
            //          mThumbnailDownloader.queueThumbnail(holder, mPhotoItem.get(position).getImgUrl());
        }

        @Override
        public int getItemCount() {
            return mPhotoItem.size();
        }

    }

    /*private void collapseSearch() {
        mSearchView.onActionViewCollapsed();
        View view = getActivity().getCurrentFocus();  // hide the soft keyboard
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }*/

    private void collapseAfterSearch() {
        mSearchView.onActionViewCollapsed();
        View view = getActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
