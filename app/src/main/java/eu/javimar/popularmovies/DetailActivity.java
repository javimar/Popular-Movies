package eu.javimar.popularmovies;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;
import eu.javimar.popularmovies.model.MovieContract.ReviewEntry;


public class DetailActivity extends AppCompatActivity
                implements LoaderManager.LoaderCallbacks<Cursor>
{
    @BindView(R.id.tv_title) TextView mTvMovieTitle;
    @BindView(R.id.placePoster) ImageView mIvPlacePoster;
    @BindView(R.id.tv_date) TextView mTvReleaseDate;
    @BindView(R.id.tv_rating) TextView mTvRating;
    @BindView(R.id.tv_synopsis) TextView mTvSynopsis;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapse_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @Nullable @BindView(R.id.imgToolbarParallax) ImageView mImgToolbarParallax;
    @BindView(R.id.movieTrailerContainer) LinearLayout mMovieTrailerContainer;

    TextView mTvTrailer, mTvReviewAuthor, mTvReviewBody;


    String mMovieTitle = "";
    int mMovieId = 0;
    boolean mMovieIsFavorite = false;

    /** Content URI for the existing movie */
    private Uri mCurrentMovieUri;

    /** youtube URL */
    private static final String YOUTUBE_URL = "https://youtu.be";

    /** Identifier for the loaders */
    private static final int MOVIE_TRAILER_LOADER = 10;
    private static final int MOVIE_REVIEW_LOADER = 11;
    private static final int MOVIE_LOADER = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_main);

        // init views
        ButterKnife.bind(this);

        // enable toolbar and disable collapsing toolbar title
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout.setTitleEnabled(false);

        // get movie position clicked
        mCurrentMovieUri = getIntent().getData();

        if (mCurrentMovieUri != null)
        {
            // Initialize loader to read the movie data from the database
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            getLoaderManager().initLoader(MOVIE_TRAILER_LOADER, null, this);
            getLoaderManager().initLoader(MOVIE_REVIEW_LOADER, null, this);
        }
    }


    /** LOADER STUFF */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        switch (id)
        {
            case MOVIE_LOADER:
                String[] projection =
                        {
                            MovieEntry.MOVIE_POS,
                            MovieEntry.COLUMN_ID,
                            MovieEntry.COLUMN_TITLE,
                            MovieEntry.COLUMN_OVERVIEW,
                            MovieEntry.COLUMN_POSTER,
                            MovieEntry.COLUMN_RATING,
                            MovieEntry.COLUMN_DATE,
                        };
                String selection = MovieEntry.MOVIE_POS + "=?";
                String[] selectionArgs =
                        new String[] { String.valueOf(ContentUris.parseId(mCurrentMovieUri)) };
                return new CursorLoader(
                        this,
                        MovieEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);

            case MOVIE_TRAILER_LOADER:
                projection = new String[]
                        {
                            TrailerEntry.TRAILER_POS,
                            TrailerEntry.COLUMN_NAME,
                            TrailerEntry.COLUMN_KEY
                        };
                selection = TrailerEntry.MOVIE_POS + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(mCurrentMovieUri)) };
                return new CursorLoader(
                        this,
                        TrailerEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);

            case MOVIE_REVIEW_LOADER:
                projection = new String[]
                        {
                            ReviewEntry.REVIEW_POS,
                            ReviewEntry.COLUMN_AUTHOR,
                            ReviewEntry.COLUMN_CONTENT
                        };
                selection = ReviewEntry.MOVIE_POS + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(mCurrentMovieUri)) };
                return new CursorLoader(
                        this,
                        ReviewEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor)
    {
        int id = loader.getId();
        switch (id)
        {
            case MOVIE_LOADER:
                // Bail early if the cursor is null or there is less than 1 row in the cursor
                if (cursor == null || cursor.getCount() < 1)
                {
                    return;
                }
                // Proceed with moving to the first row of the cursor and reading data from it
                if (cursor.moveToFirst()) {
                    // Find the columns of movie attributes that we're interested in
                    int idColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_ID);
                    int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_TITLE);
                    int overviewColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW);
                    int ratingColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_RATING);
                    int dateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_DATE);

                    // find the values of those columns
                    mMovieId = cursor.getInt(idColumnIndex);
                    mMovieTitle = cursor.getString(titleColumnIndex);
                    String overview = cursor.getString(overviewColumnIndex);
                    double rating = cursor.getDouble(ratingColumnIndex);
                    String date = cursor.getString(dateColumnIndex);

                    mTvSynopsis.setText(overview);
                    // display only year
                    mTvReleaseDate.setText(date.substring(0, 4));
                    mTvRating.setText(String.valueOf(rating));
                    Picasso
                            .get()
                            .load(Utils.buildPosterUrl(cursor, Utils.DETAIL_ACTIVITY))
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.error_image)
                            .into(mIvPlacePoster);
                    if (getResources().getConfiguration().orientation ==
                            Configuration.ORIENTATION_PORTRAIT)
                    {
                        Picasso
                                .get()
                                .load(Utils.buildPosterUrl(cursor, Utils.DETAIL_ACTIVITY))
                                .error(R.drawable.error_image)
                                .into(mImgToolbarParallax);
                    }
                }

                // This is a good spot to take care of member variables
                // make subtitle in toolbar equal to movie title
                getSupportActionBar().setSubtitle(mMovieTitle);

                // check to see if movie is already our favorite
                mMovieIsFavorite = isMovieFavorite(mMovieId);

                mTvMovieTitle.setText(mMovieTitle);

                // Setup FAB according to fav or not fav
                if (mMovieIsFavorite)
                {
                    fab.setImageResource(R.drawable.ic_favorite_heart);
                }
                else
                {
                    fab.setImageResource(R.drawable.ic_nofavorite_heart);
                }
                // add listener to FAB
                fab.setOnClickListener(view -> {
                    // add/delete movie to/from database
                    updateMovieFavorite();
                });
                break;

            case MOVIE_TRAILER_LOADER:
                // don't inflate anything if there are no trailers
                if (cursor == null || cursor.getCount() < 1)
                {
                    return;
                }

                // draw the divider into the detail_content screen
                mMovieTrailerContainer.addView(LayoutInflater.from(this)
                        .inflate(R.layout.trailer_divider, null));

                // Go back to first element, traverse the trailers, and create views programmatically
                cursor.moveToPosition(-1);
                while (cursor.moveToNext())
                {
                    String name = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_NAME));
                    final String trailer_key =
                            cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_KEY));

                    View movieTrailerItem = LayoutInflater.from(this)
                            .inflate(R.layout.movie_trailer_item, null);

                    movieTrailerItem.setOnClickListener(view -> playYouTubeTrailerIntent(trailer_key));
                    mTvTrailer = (TextView)movieTrailerItem.findViewById(R.id.tv_trailer);
                    mTvTrailer.setText(name);
                    mMovieTrailerContainer.addView(movieTrailerItem);
                }
                break;

            case MOVIE_REVIEW_LOADER:
                // don't inflate anything if there are no reviews
                if (cursor == null || cursor.getCount() < 1)
                {
                    return;
                }

                mMovieTrailerContainer.addView(LayoutInflater.from(this)
                        .inflate(R.layout.review_divider, null));
                //traverse the reviews, and create views programmatically
                cursor.moveToPosition(-1);
                while (cursor.moveToNext())
                {
                    String author = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR));
                    String review = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT));

                    View reviewTrailerItem = LayoutInflater.from(this)
                            .inflate(R.layout.movie_review_item, null);
                    mTvReviewAuthor = reviewTrailerItem.findViewById(R.id.tv_review_author);
                    mTvReviewAuthor.setText(author);
                    mTvReviewBody = reviewTrailerItem.findViewById(R.id.tv_review_body);
                    mTvReviewBody.setText(review);

                    mMovieTrailerContainer.addView(reviewTrailerItem);
                }
                break;
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    // Build the URL and connect via Intent to https://youtu.be/key
    private void playYouTubeTrailerIntent(String key)
    {
        Uri.Builder uriBuilder = Uri.parse(YOUTUBE_URL).buildUpon();
        uriBuilder.appendEncodedPath(key);

        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uriBuilder.toString()));
        // Verify the intent will resolve to at least one activity
        if (i.resolveActivity(getPackageManager()) != null)
        {
            startActivity(Intent.createChooser(i, getString(R.string.detail_choose_web_action)));
        }
        else
        {
            Utils.showSnackbar(this, toolbar, getString(R.string.error_wrong_url));
        }
    }



    /** Fake the delete action for the user on the favorites */
    private void updateMovieFavorite()
    {
        ContentValues values = new ContentValues();

        if (!mMovieIsFavorite)
        {
            values.put(MovieEntry.COLUMN_FAV, 1);
            // Pass in null for the selection and selection args because mCurrentMovieUri
            // will already identify the correct row in the database that we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentMovieUri, values, null, null);
            if (rowsAffected != 0)
            {
                // update was successful
                Utils.showSnackbar(DetailActivity.this, toolbar,
                        String.format(getString(R.string.detail_movie_added), mMovieTitle));
            }
        }
        else
        {
            // movie is already in favorites, user want to "delete it"
            values.put(MovieEntry.COLUMN_FAV, 0);
            int rowsAffected = getContentResolver().update(mCurrentMovieUri, values, null, null);
            if (rowsAffected != 0)
            {
                // update was successful,
                Utils.showSnackbar(DetailActivity.this, toolbar,
                        String.format(getString(R.string.detail_movie_delete), mMovieTitle));
            }
            else
            {
                Utils.showSnackbar(DetailActivity.this, toolbar,
                        getString(R.string.error_movie_delete));
            }
        }
    }


    /**
     * Quick check if the movie is a favorite or not
     */
    private boolean isMovieFavorite(int movieId)
    {
        String[] projection = new String[]
        {
            MovieEntry.MOVIE_POS,
            MovieEntry.COLUMN_ID,
            MovieEntry.COLUMN_FAV
        };
        String selection = MovieEntry.COLUMN_ID + "=?";
        String [] selectionArgs = new String[] { String.valueOf(movieId) };
        Cursor cursor = getContentResolver()
                .query(MovieEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor != null && cursor.getCount() >= 1)
        {
            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if (cursor.moveToFirst())
            {
                int fav = cursor.getInt(cursor.getColumnIndex(MovieEntry.COLUMN_FAV));
                return (fav == 1);
            }
            cursor.close();
        }
        return false;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

}
