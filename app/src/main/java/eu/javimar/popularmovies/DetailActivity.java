package eu.javimar.popularmovies;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;


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

    String mMovieTitle = "";
    int mMovieId = 0;
    boolean mMovieIsFavorite = false;

    /** Content URI for the existing movie */
    private Uri mCurrentMovieUri;

    /** youtube URL */
    private static final String YOUTUBE_URL = "https://youtu.be";
    //http://api.themoviedb.org/3/movie/ID/videos?api_key=API_KEY
    //http://api.themoviedb.org/3/movie/ID/reviews?api_key=API_KEY

    /** Identifier for the movie detail loader */
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
            // Initialize a loader to read the movie data from the database
            // and display the current values, since we kill the activity
            // everytime we update, loader data is always initialize
            getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        }
    }


    /** LOADER STUFF */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        // Include all attributes in projection that we need from the movie table
        String[] projection =
        {
            MovieEntry._ID,
            MovieEntry.COLUMN_ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_OVERVIEW,
            MovieEntry.COLUMN_POSTER,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_DATE
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,                   // Parent activity context
                mCurrentMovieUri,       // Query the content URI for the current movie
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
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
        // (This should be the only row in the cursor)
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

        // This is a good spot to take care of member variables a fab button
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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // is there anything to do here?
    }



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

                //finish();
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
