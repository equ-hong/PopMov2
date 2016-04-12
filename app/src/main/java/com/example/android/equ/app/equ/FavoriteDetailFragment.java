package com.example.android.equ.app.equ;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.equ.app.equ.database.EquContract;
import com.example.android.equ.app.equ.database.EquContract.FavoriteEntry;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by i on 2016-04-08.
 */
public class FavoriteDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = FavoriteDetailFragment.class.getSimpleName();
    static final String FAVORITEDETAIL_URI = "URI";
    private static final int FAVORITE_DETAIL_LOADER = 11;

    String[] FAVORITE_COLUMNS = new String[]{
            FavoriteEntry.TABLE_FAVORITE + "." + FavoriteEntry._ID,
            FavoriteEntry.COLUMN_POSTER_PATH,
            FavoriteEntry.COLUMN_TITLE,
            FavoriteEntry.COLUMN_OVERVIEW,
            FavoriteEntry.COLUMN_RELEASE_DATE,
            FavoriteEntry.COLUMN_VOTE_AVERAGE,
            FavoriteEntry.COLUMN_CREATE_TIME,
            FavoriteEntry.COLUMN_MOV_ID
    };

    static final int COL_ID = 0;
    static final int COL_POSTER_PATH = 1;
    static final int COL_TITLE = 2;
    static final int COL_OVERVIEW = 3;
    static final int COL_RELEASE_DATE = 4;
    static final int COL_VOTE_AVERAGE = 5;
    static final int COL_CREATE_TIME = 6;
    static final int COL_MOV_ID = 7;

    private ImageView imageView;
    private TextView titleView;
    private TextView releaseDateView;
    private TextView voteRateView;
    private Button favoButton;
    private TextView synopsisView;

    private Uri mUri;

    public FavoriteDetailFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite_detail, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.detail_image);
        titleView = (TextView) rootView.findViewById(R.id.detail_title);
        releaseDateView = (TextView) rootView.findViewById(R.id.detail_release_date);
        voteRateView = (TextView) rootView.findViewById(R.id.detail_vote_rate);
        favoButton = (Button) rootView.findViewById(R.id.detail_mark_pref);
        synopsisView = (TextView) rootView.findViewById(R.id.detail_synopsis);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(FavoriteDetailFragment.FAVORITEDETAIL_URI);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FAVORITE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FAVORITE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        titleView.setText(data.getString(COL_TITLE));
        releaseDateView.setText(data.getString(COL_RELEASE_DATE));
        voteRateView.setText(data.getString(COL_VOTE_AVERAGE) + "/10");
        synopsisView.setText(data.getString(COL_OVERVIEW));
        favoButton.setVisibility(View.VISIBLE);
        favoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deleted = getActivity().getContentResolver().delete(
                        FavoriteEntry.CONTENT_URI,
                        FavoriteEntry.COLUMN_MOV_ID + " = ? ", new String[]{data.getString(COL_MOV_ID)}
                );

                File favoriteStorage = getActivity().getDir("favoriteDir", Context.MODE_PRIVATE);
                File favoritePath = new File(favoriteStorage, data.getString(COL_POSTER_PATH));
                Log.d("del", String.valueOf(favoritePath));

                favoritePath.delete();
            }
        });

        imageView.setAdjustViewBounds(true);

        File favoriteStorage = getActivity().getDir("favoriteDir", Context.MODE_PRIVATE);
        File favoritePath = new File(favoriteStorage, data.getString(COL_POSTER_PATH));

        Picasso.with(getActivity())
                .load(favoritePath)
                .into(imageView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}

