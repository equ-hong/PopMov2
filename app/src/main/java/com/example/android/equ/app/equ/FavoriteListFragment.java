package com.example.android.equ.app.equ;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.example.android.equ.app.equ.database.EquContract;
import com.example.android.equ.app.equ.database.EquContract.FavoriteEntry;

import java.util.Date;

/**
 * Created by i on 2016-04-08.
 */
public class FavoriteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    private static final int FAVORITE_LIST_LOADER = 10;

    String[] FAVORITE_COLUMNS = new String[]{
            FavoriteEntry.TABLE_FAVORITE + "." + FavoriteEntry._ID,
            FavoriteEntry.COLUMN_MOV_ID,
            FavoriteEntry.COLUMN_POSTER_PATH,
            FavoriteEntry.COLUMN_TITLE,
            FavoriteEntry.COLUMN_OVERVIEW,
            FavoriteEntry.COLUMN_RELEASE_DATE,
            FavoriteEntry.COLUMN_VOTE_AVERAGE,
            FavoriteEntry.COLUMN_CREATE_TIME
    };

    static final int COL_ID = 0;
    static final int COL_MOV_ID = 1;
    static final int COL_POSTER_PATH = 2;

    private FavoriteAdapter mFavoriteAdapter;

    public interface Callback {
        public void onItemSelected(Uri contentUri);
    }

    public FavoriteListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFavoriteAdapter = new FavoriteAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_favorite_list, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.favorite_gridview);
        mGridView.setAdapter(mFavoriteAdapter);


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(EquContract.FavoriteEntry.buildFavoriteUri(cursor.getLong(COL_MOV_ID)));
                }
                mPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FAVORITE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = FavoriteEntry.TABLE_FAVORITE + "." + FavoriteEntry._ID + " DESC";
        Uri favoriteUri = FavoriteEntry.buildFavoriteUri();

        return new CursorLoader(
                getActivity(),
                favoriteUri,
                FAVORITE_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mFavoriteAdapter.swapCursor(cursor);
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoriteAdapter.swapCursor(null);
    }
}


