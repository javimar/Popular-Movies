package eu.javimar.popularmovies;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.popularmovies.model.MovieContract;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;
import eu.javimar.popularmovies.model.MovieContract.ReviewEntry;


public class DetailActivity extends AppCompatActivity
                implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.tv_title) TextView mTvMovieTitle;
    @BindView(R.id.placePoster) ImageView mIvPlacePoster;
    @BindView(R.id.tv_date) TextView mTvReleaseDate;
    @BindView(R.id.tv_rating) TextView mTvRating;
    @BindView(R.id.tv_synopsis) TextView mTvSynopsis;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapse_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.imgToolbarParallax) ImageView mImgToolbarParallax;
    @BindView(R.id.movieTrailerContainer) LinearLayout mMovieTrailerContainer;
    TextView mTvTrailer;


    String mMovieTitle = "";
    int mMovieId = 0;
    boolean mMovieIsFavorite = false;

    /** Content URI for the existing movie */
    private Uri mCurrentMovieUri;

    /** youtube URL */
    private static final String YOUTUBE_URL = "https://youtu.be";
    //https://youtu.be/key to visualize

    /** Identifier for the loaders */
    private static final int MOVIE_DETAIL_LOADER = 0;


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
            getLoaderManager().restartLoader(MOVIE_DETAIL_LOADER, null, this);
        }
    }


    /** LOADER STUFF */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        // add the path to recognize the JOIN of movies and trailers
        Uri.Builder ub = mCurrentMovieUri.buildUpon();
        ub.appendPath(MovieContract.PATH_MOVIE_TRAILER);

        String[] projection =
                {
                    MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
                    MovieEntry.COLUMN_ID,
                    MovieEntry.COLUMN_TITLE,
                    MovieEntry.COLUMN_OVERVIEW,
                    MovieEntry.COLUMN_POSTER,
                    MovieEntry.COLUMN_RATING,
                    MovieEntry.COLUMN_DATE,
                    TrailerEntry.TABLE_NAME + "." + TrailerEntry._ID,
                    TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID,
                    TrailerEntry.COLUMN_NAME,
                    TrailerEntry.COLUMN_KEY
                };
        String selection = MovieEntry.TABLE_NAME + "." + MovieEntry._ID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(mCurrentMovieUri)) };
        return new CursorLoader(
                this,
                ub.build(),
                projection,
                selection,
                selectionArgs,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor)
    {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1)
        {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst())
        {
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
            mTvRating.setText(String.format(getString(R.string.detail_ratings_literal),
                    String.valueOf(rating)));
            Picasso
                    .with(this)
                    .load(Utils.buildPosterUrl(cursor, Utils.DETAIL_ACTIVITY))
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(mIvPlacePoster);
            Picasso
                    .with(this)
                    .load(Utils.buildPosterUrl(cursor, Utils.DETAIL_ACTIVITY))
                    .error(R.drawable.error_image)
                    .into(mImgToolbarParallax);
        }

        // This is a good spot to take care of member variables
        // make subtitle in toolbar equal to movie title
        getSupportActionBar().setSubtitle(mMovieTitle);

        // check to see if movie is already our favorite
        mMovieIsFavorite = isMovieFavorite(mMovieId);

        mTvMovieTitle.setText(mMovieTitle);

        // Setup FAB
        if (mMovieIsFavorite)
        {
            fab.setImageResource(R.drawable.ic_favorite_heart);
        }
        else
        {
            fab.setImageResource(R.drawable.ic_nofavorite_heart);
        }
        // add listener to FAB
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // add/delete movie to/from database
                updateMovieFavorite();
            }
        });


        /**
         * Time for the TRAILERS
         */
        Log.d(LOG_TAG, "JAVIER JOIN CURSOR= " + DatabaseUtils.dumpCursorToString(cursor));
        // Go back to first element, traverse the trailers, and create views programmatically
        cursor.moveToFirst();
        while (cursor.moveToNext())
        {
            String name = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_NAME));
            String trailer_key = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_KEY));

            View mMovieTrailerItem = LayoutInflater.from(this).inflate(
                    R.layout.movie_trailer_item, null);

            mMovieTrailerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // will do something later
                }
            });
            mTvTrailer = (TextView)mMovieTrailerItem.findViewById(R.id.tv_trailer);
            mTvTrailer.setText(name);
            mMovieTrailerContainer.addView(mMovieTrailerItem);
        }

    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // is there anything to do here?
    }


    /** Fake favorite delete */
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



    private boolean isMovieFavorite(int movieId)
    {
        String[] projection = new String[]
        {
            MovieEntry._ID,
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
