package com.example.android.equ.app.equ;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.equ.app.equ.database.EquContract;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by i on 2016-04-07.
 */
public class MovDetailAdapter extends RecyclerView.Adapter<MovDetailAdapter.MainHolder> {
    public final String LOG_TAG = MovDetailAdapter.class.getSimpleName();

    /**
     * VIEW_TYPE_DETAIL : for the movie details and first review
     * VIEW_TYPE_REVIEW : for the reviews except for first one
     */
    private static final int VIEW_TYPE_DETAIL = 0;
    private static final int VIEW_TYPE_REVIEW = 1;

    private MovDetailFragment mMovDetailFragment;
    private Context mContext;
    private ContentValues mMovValues;
    private int mMovId;
    private List<TrailerResult> mTrailerResults;
    private List<ReviewResult> mReviewResults;
    private int trailerSeq = 0;
    private TextView mAuthorView;
    private TextView mContentView;

    /**
     * getting review data with retrofit in the constructor.
     */
    public MovDetailAdapter(ContentValues movValues, Context context, final MovDetailFragment fragment) {
        mMovDetailFragment = fragment;
        mContext = context;
        mMovValues = movValues;
        mMovId = movValues.getAsInteger(EquContract.MovEntry.COLUMN_MOV_ID);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ReviewService reviewService = retrofit.create(ReviewService.class);
        Call<Review> reviews = reviewService.listReviews(mMovId, BuildConfig.THEMOVIEDB_API_KEY);

        reviews.enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                if (response.isSuccessful()) {
                    Review review = response.body();
                    mReviewResults = review.getResults();
                    try {
                        if(mReviewResults.size() > 0) {
                            mAuthorView = (TextView) mMovDetailFragment.getView().findViewById(R.id.list_item_review_author);
                            mContentView = (TextView) mMovDetailFragment.getView().findViewById(R.id.list_item_review_content);
                            mAuthorView.setEnabled(true);
                            mContentView.setEnabled(true);
                            mAuthorView.setText(mReviewResults.get(0).getAuthor());
                            mContentView.setText(mReviewResults.get(0).getContent());
                            mMovDetailFragment.updateReviewAdapter();
                        }
                    } catch (NullPointerException e) {
                        Log.e(LOG_TAG, "NULL Point error", e);
                    }
                }
            }
            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                Log.d("Error", t.getMessage());
            }
        });
    }

    @Override
    public MainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_DETAIL: {
                layoutId = R.layout.fragment_mov_detail_main;
                View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                return new DetailHolder(view);
            }
            case VIEW_TYPE_REVIEW: {
                layoutId = R.layout.fragment_mov_detail_review;
                View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
                return new ReviewHolder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(MainHolder holder, int position) {
        if(holder.getItemViewType() == VIEW_TYPE_DETAIL){
            DetailHolder detailHolder = (DetailHolder) holder;
            detailHolder.bindDetail(mMovValues);
        }
        else {
            ReviewHolder reviewHolder = (ReviewHolder) holder;
            reviewHolder.bindReview(mReviewResults.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if(mReviewResults == null || mReviewResults.size() < 1) {
            return 1;
        }
        return mReviewResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_DETAIL : VIEW_TYPE_REVIEW;
    }

    class DetailHolder extends MainHolder {
        private ImageView imageView;
        private TextView titleView;
        private TextView releaseDateView;
        private TextView voteRateView;
        private Button favoButton;
        private TextView synopsisView;
        private LinearLayout trailerLinearLayout;
        private TextView authorView;
        private TextView contentView;

        public DetailHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.detail_image);
            titleView = (TextView) itemView.findViewById(R.id.detail_title);
            releaseDateView = (TextView) itemView.findViewById(R.id.detail_release_date);
            voteRateView = (TextView) itemView.findViewById(R.id.detail_vote_rate);
            favoButton = (Button) itemView.findViewById(R.id.detail_mark_pref);
            synopsisView = (TextView) itemView.findViewById(R.id.detail_synopsis);
            trailerLinearLayout = (LinearLayout) itemView.findViewById(R.id.detail_layout_trailer);
            mAuthorView = (TextView) itemView.findViewById(R.id.list_item_review_author);
            mContentView = (TextView) itemView.findViewById(R.id.list_item_review_content);
        }

        public void bindDetail(final ContentValues contentValues) {
            titleView.setText(contentValues.getAsString(EquContract.MovEntry.COLUMN_TITLE));
            releaseDateView.setText(contentValues.getAsString(EquContract.MovEntry.COLUMN_RELEASE_DATE));
            voteRateView.setText(contentValues.getAsString(EquContract.MovEntry.COLUMN_VOTE_AVERAGE) + "/10");
            synopsisView.setText(contentValues.getAsString(EquContract.MovEntry.COLUMN_OVERVIEW));
            favoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri insertedUri = mContext.getContentResolver().insert(
                            EquContract.FavoriteEntry.CONTENT_URI,
                            mMovValues
                    );

                    File movStorage = mContext.getDir("movDir", Context.MODE_PRIVATE);
                    File movPath = new File(movStorage, contentValues.getAsString(EquContract.MovEntry.COLUMN_POSTER_PATH));

                    File favoriteStorage = mContext.getDir("favoriteDir", Context.MODE_PRIVATE);
                    File favoritePath = new File(favoriteStorage, contentValues.getAsString(EquContract.MovEntry.COLUMN_POSTER_PATH));

                    FileOutputStream fos = null;

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(movPath));
                        fos = new FileOutputStream(favoritePath);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fos);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageView.setAdjustViewBounds(true);

            File internalStorage = mContext.getDir("movDir", Context.MODE_PRIVATE);
            File movPath = new File(internalStorage, contentValues.getAsString(EquContract.MovEntry.COLUMN_POSTER_PATH));

            Picasso.with(mContext)
                    .load(movPath)
                    .into((ImageView) imageView);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://api.themoviedb.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            TrailerService trailerService = retrofit.create(TrailerService.class);
            Call<Trailer> trailers = trailerService.listTrailers(mMovId, BuildConfig.THEMOVIEDB_API_KEY);

            trailers.enqueue(new Callback<Trailer>() {
                 @Override
                 public void onResponse(Call<Trailer> call, Response<Trailer> response) {
                     if (response.isSuccessful()) {
                         Trailer trailer = response.body();
                         mTrailerResults = trailer.getResults();
                         if (trailerSeq == 0) {
                             for (final TrailerResult result : mTrailerResults) {
                                 trailerSeq = trailerSeq + 1;

                                 Button button = new Button(mContext);
                                 button.setText("Movie Trailer " + String.valueOf(trailerSeq));
                                 trailerLinearLayout.addView(button);
                                 button.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         Intent intent = new Intent(Intent.ACTION_VIEW,
                                                 Uri.parse("http://www.youtube.com/watch?v=" + result.getKey()));
                                         mContext.startActivity(intent);
                                     }
                                 });
                             }
                         }
                         if(mTrailerResults.size() > 0) {
                             mMovDetailFragment.updateShareAction("http://www.youtube.com/watch?v=" + mTrailerResults.get(0).getKey());
                         }
                     }
                 }

                 @Override
                 public void onFailure(Call<Trailer> call, Throwable t) {
                     Log.d("Error", t.getMessage());
                 }
            });
        }
    }

    class ReviewHolder extends MainHolder {
        private TextView authorView;
        private TextView contentView;
        public ReviewHolder(View itemView) {
            super(itemView);
            authorView = (TextView) itemView.findViewById(R.id.list_item_review_author);
            contentView = (TextView) itemView.findViewById(R.id.list_item_review_content);
        }
        public void bindReview(ReviewResult reviewResult) {
            authorView.setText(reviewResult.getAuthor());
            contentView.setText(reviewResult.getContent());
        }
    }

    public class MainHolder extends  RecyclerView.ViewHolder {
        public MainHolder(View itemView) {
            super(itemView);
        }
    }

    public interface TrailerService {
        @GET("3/movie/{id}/videos")
        Call<Trailer> listTrailers(@Path("id") int id, @Query("api_key") String apiKey);
    }

    public interface ReviewService {
        @GET("3/movie/{id}/reviews")
        Call<Review> listReviews(@Path("id") int id, @Query("api_key") String apiKey);
    }
}


