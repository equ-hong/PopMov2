package com.example.android.equ.app.equ;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.android.equ.app.equ.database.EquContract;
import com.example.android.equ.app.equ.sync.PopMovSyncAdapter;

public class MovListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MovListFragment.class.getSimpleName();
    private MovListAdapter mMovListAdapter;

    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int MOV_LIST_LOADER = 0;

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

    public interface Callback {
        public void onItemSelected(Uri contentUri);
    }

    public MovListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movlistfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /**
         *  when selecting, users move to favorite movies view that they saved.
         */
        if (id == R.id.action_favorite) {
            Intent intent = new Intent(getActivity(), FavoriteListActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMovListAdapter = new MovListAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_mov_list, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.mov_gridview);
        mGridView.setAdapter(mMovListAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String sortOrder = Utility.getPreferredOrder(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(EquContract.MovEntry.buildMovOrderWithRanking(sortOrder, cursor.getInt(COL_RANKING)));
                }
                mPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOV_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onOrderChanged( ) {
        updateMov();
        getLoaderManager().restartLoader(MOV_LIST_LOADER, null, this);
    }

    private void updateMov() {
        PopMovSyncAdapter.syncImmediately(getActivity());
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String orderSetting = Utility.getPreferredOrder(getActivity());
        String sortOrder = EquContract.MovEntry.TABLE_MOV + "." + EquContract.MovEntry._ID + " ASC";
        Uri movForOrderUri = EquContract.MovEntry.buildMovOrder(orderSetting);

        Cursor cursor = getActivity().getContentResolver().query(movForOrderUri, MOV_COLUMNS, null, null, sortOrder);

        return new CursorLoader(
                getActivity(),
                movForOrderUri,
                MOV_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mMovListAdapter.swapCursor(cursor);
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovListAdapter.swapCursor(null);
    }
}

