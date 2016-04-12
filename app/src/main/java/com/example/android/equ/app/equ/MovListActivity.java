package com.example.android.equ.app.equ;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.equ.app.equ.sync.PopMovSyncAdapter;

public class MovListActivity extends AppCompatActivity implements MovListFragment.Callback {

    private final String LOG_TAG = MovListActivity.class.getSimpleName();
    private final String MOVDETAILFRAGMENT_TAG = "MDFTAG";

    private boolean mTwoPane;
    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSortOrder = Utility.getPreferredOrder(this);

        setContentView(R.layout.activity_mov_list);
        if (findViewById(R.id.mov_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mov_detail_container, new MovDetailFragment(), MOVDETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        PopMovSyncAdapter.initializeSyncAdapter(this);
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
    protected void onResume() {
        super.onResume();
        String sortOrder = Utility.getPreferredOrder( this );
        if (sortOrder != null && !sortOrder.equals(mSortOrder)) {
            MovListFragment mlf = (MovListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_mov_list);
            if ( null != mlf ) {
                mlf.onOrderChanged();
            }
            MovDetailFragment mdf = (MovDetailFragment)getSupportFragmentManager().findFragmentByTag(MOVDETAILFRAGMENT_TAG);
            if ( null != mdf ) {
                mdf.onOrderChanged(sortOrder);
            }
            mSortOrder = sortOrder;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(MovDetailFragment.MOVDETAIL_URI, contentUri);

            MovDetailFragment fragment = new MovDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mov_detail_container, fragment, MOVDETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}

