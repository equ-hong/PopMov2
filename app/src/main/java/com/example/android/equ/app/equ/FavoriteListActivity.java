package com.example.android.equ.app.equ;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by i on 2016-04-08.
 */
public class FavoriteListActivity extends AppCompatActivity implements FavoriteListFragment.Callback {
    private final String LOG_TAG = FavoriteListActivity.class.getSimpleName();
    private final String FAVORITEDETAILFRAGMENT_TAG = "FDFTAG";

    private boolean mTwoPane;
    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_list);
        if (findViewById(R.id.favorite_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.favorite_detail_container, new FavoriteDetailFragment(), FAVORITEDETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movlistactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(FavoriteDetailFragment.FAVORITEDETAIL_URI, contentUri);

            FavoriteDetailFragment fragment = new FavoriteDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.favorite_detail_container, fragment, FAVORITEDETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, FavoriteDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}

