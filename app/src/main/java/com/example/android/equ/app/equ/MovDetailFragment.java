package com.example.android.equ.app.equ;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.equ.app.equ.database.EquContract;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by i on 2016-04-06.
 */
public class MovDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MovDetailFragment.class.getSimpleName();
    static final String MOVDETAIL_URI = "URI";

    private static final String MOV_SHARE_HASHTAG = " #PopMovApp";

    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private String mMovLink;

    /**
     *  The variables below are for user reviews.
     */
    private RecyclerView mReviewRecyclerView;
    private MovDetailAdapter mMovDetailAdapter;
    private MovDetailFragment mMovDetailFragment;

    private static final int MOVDETAIL_LOADER = 0;

    String[] MOV_COLUMNS = new String[]{
            EquContract.MovEntry.TABLE_MOV + "." + EquContract.MovEntry._ID,
            EquContract.MovEntry.COLUMN_ORDER_KEY,
            EquContract.MovEntry.COLUMN_RANKING,
            EquContract.MovEntry.COLUMN_MOV_ID,
            EquContract.MovEntry.COLUMN_TITLE,
            EquContract.MovEntry.COLUMN_RELEASE_DATE,
            EquContract.MovEntry.COLUMN_VOTE_AVERAGE,
            EquContract.MovEntry.COLUMN_OVERVIEW,
            EquContract.MovEntry.COLUMN_POSTER_PATH,
            EquContract.MovEntry.COLUMN_CREATE_TIME
    };

    static final int COL_ID = 0;
    static final int COL_ORDER_KEY = 1;
    static final int COL_RANKING = 2;
    static final int COL_MOV_ID = 3;
    static final int COL_TITLE = 4;
    static final int COL_RELEASE_DATE = 5;
    static final int COL_VOTE_AVERAGE = 6;
    static final int COL_OVERVIEW = 7;
    static final int COL_POSTER_PATH = 8;
    static final int COL_CREATE_TIME = 9;



    public MovDetailFragment() {
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(MovDetailFragment.MOVDETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_mov_detail, container, false);

        /**
         *  The movie reviews in the detail view are showed with recycler view.
         *  The recycler view adapter has two types of view. One is the details of the movie and firs review.
         *  the other is for reviews except first one.
         */
        mReviewRecyclerView = (RecyclerView) rootView
                .findViewById(R.id.review_recycler_view);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movdetailfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovIntent());
        }
    }

    private Intent createShareMovIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovLink + MOV_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVDETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onOrderChanged( String newSortOrder ) {
        Uri uri = mUri;
        if (null != uri) {
            int ranking = EquContract.MovEntry.getRankingFromUri(uri);
            Uri updatedUri = EquContract.MovEntry.buildMovOrderWithRanking(newSortOrder, ranking);
            mUri = updatedUri;
            getLoaderManager().restartLoader(MOVDETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("uri", String.valueOf(mUri));
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    MOV_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        /**
         *  Delegation of showing details of the movie to recycler view adapter.
         */
        if (!data.moveToFirst()) {
            return;
        }
        ContentValues movValues = new ContentValues();
        movValues.put(EquContract.MovEntry.COLUMN_MOV_ID, data.getInt(COL_MOV_ID));
        movValues.put(EquContract.MovEntry.COLUMN_TITLE, data.getString(COL_TITLE));
        movValues.put(EquContract.MovEntry.COLUMN_RELEASE_DATE, data.getString(COL_RELEASE_DATE));
        movValues.put(EquContract.MovEntry.COLUMN_VOTE_AVERAGE, data.getString(COL_VOTE_AVERAGE));
        movValues.put(EquContract.MovEntry.COLUMN_OVERVIEW, data.getString(COL_OVERVIEW));
        movValues.put(EquContract.MovEntry.COLUMN_POSTER_PATH, data.getString(MovListFragment.COL_POSTER_PATH));
        movValues.put(EquContract.MovEntry.COLUMN_CREATE_TIME, data.getLong(COL_MOV_ID));

        mMovDetailFragment = (MovDetailFragment) getFragmentManager().findFragmentById(R.id.mov_detail_container);
        mMovDetailAdapter = new MovDetailAdapter(movValues, getActivity(), mMovDetailFragment);
        mReviewRecyclerView.setAdapter(mMovDetailAdapter);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    /**
     *  Review data come async, recycler view update should be done one more.
     */
    public void updateReviewAdapter() {
        mMovDetailAdapter.notifyDataSetChanged();
    }
    public void updateShareAction(String link) {
        mMovLink = link;
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareMovIntent());
        }
    }
}
