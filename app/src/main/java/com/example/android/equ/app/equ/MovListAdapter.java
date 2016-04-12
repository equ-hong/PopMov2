package com.example.android.equ.app.equ;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * {@link MovListAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class MovListAdapter extends CursorAdapter {

    public MovListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_mov, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view;
        imageView.setAdjustViewBounds(true);

        File internalStorage = mContext.getDir("movDir", Context.MODE_PRIVATE);
        File movPath = new File(internalStorage, cursor.getString(MovListFragment.COL_POSTER_PATH));

        Picasso.with(context)
                .load(movPath)
                .into(imageView);
    }
}
