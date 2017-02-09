package eu.javimar.popularmovies;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieLoader;
import eu.javimar.popularmovies.view.MovieAdapter;

import static eu.javimar.popularmovies.Utils.API_KEY_TAG;
import static eu.javimar.popularmovies.Utils.BASE_URL;
import static eu.javimar.popularmovies.Utils.sMovieType;
@SuppressWarnings("all")


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MovieAdapter.ListItemClickListener
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapse_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view_movies) RecyclerView mRecyclerView;
    @BindView(R.id.tv_error_message_display) TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;

    private MovieAdapter mMovieAdapter;

    /** LOADER CONSTANTS */
    private static final int MOVIE_SERVER_LOADER = 90;
    private static final int MOVIE_DB_LOADER = 91;

    /**
     * URL bits and pieces for the themoviedb.org
     */
    private static final String TOP_RATED_PATH = "movie/top_rated";
    private static final String POPULAR_PATH = "movie/popular";

    /** Strings for the column COLUMN_TYPE */
    private static final String POPULAR = "popular";
    private static final String TOP_RATED = "toprated";
    private static final String FAVORITE = "favorites";



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // enable tool bar and disable collapsing toolbar title
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        collapsingToolbarLayout.setTitleEnabled(false);


        // add support for preferences changes callback
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // have different column count depending on the orientation (landscape or portrait),
        // If we added support for tablets, we must use the resources for simplicity
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        else
        {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }
        mRecyclerView.setHasFixedSize(true);

        // link movie data with the Views
        mMovieAdapter = new MovieAdapter(this, this);
        // Setting the adapter attaches it to the RecyclerView in our layout
        mRecyclerView.setAdapter(mMovieAdapter);

        // store preference display filter
        sMovieType = getPreferenceOrderBy();
        Bundle bundle = new Bundle();
        bundle.putString("movie_type", sMovieType);

        // start server loader
        startServerLoader(bundle);

        // show movies in the screen
        getLoaderManager().initLoader(MOVIE_DB_LOADER, bundle, this);
    }


    private void startServerLoader(Bundle bundle)
    {
        // show progress bar
        setLoadingIndicatorVisible(true);

        // If there is a network connection, fetch data for toprated or popular
        if (Utils.isNetworkAvailable(this))
        {
            getLoaderManager().initLoader(MOVIE_SERVER_LOADER, bundle, this);
        }
        else
        {
            // not connected, show message
            Utils.showSnackbar(this, mRecyclerView,
                    getString(R.string.error_no_internet_connection));
            // First, hide loading indicator so error message will be visible
            setLoadingIndicatorVisible(false);
            // Update empty state with no connection message
            mErrorMessageDisplay.setText(R.string.error_no_internet_connection);
        }
    }


    /**
     * LOADER METHODS
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String movieType;

        switch (id)
        {
            case MOVIE_SERVER_LOADER:
                // get which type of movie
                movieType = args.getString("movie_type");

                // start building the URL
                Uri baseUri = Uri.parse(BASE_URL);
                Uri.Builder uriBuilder = baseUri.buildUpon();

                if (movieType.equals(getString(R.string.settings_order_by_popular_value)))
                {
                    uriBuilder.appendEncodedPath(POPULAR_PATH);
                }
                else
                {
                    uriBuilder.appendEncodedPath(TOP_RATED_PATH);
                }
                // add the API_KEY to the query ?q=
                uriBuilder.appendQueryParameter(API_KEY_TAG, getString(R.string.API_KEY));
                return new MovieLoader(this, uriBuilder.toString());

            case MOVIE_DB_LOADER:
                // get which type of movie
                movieType = args.getString("movie_type");

                if (movieType.equals(getString(R.string.settings_order_by_popular_value)))
                {
                    getSupportActionBar().setSubtitle(R.string.settings_order_by_popular_label);
                }
                else if (movieType.equals(getString(R.string.settings_order_by_top_rated_value)))
                {
                    getSupportActionBar().setSubtitle(R.string.settings_order_by_top_rated_label);
                }
                else if (movieType.equals(getString(R.string.settings_order_by_fav_value)))
                {
                    getSupportActionBar().setSubtitle(R.string.settings_order_by_fav_label);
                    String [] projection = new String[]
                    {
                        MovieEntry._ID,
                        MovieEntry.COLUMN_POSTER
                    };
                    String selection = MovieEntry.COLUMN_FAV + "=?";
                    String [] selectionArgs = new String[] { String.valueOf(1) };
                    return new CursorLoader(
                        this,
                        MovieEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        null);
                }

                // only valid for top_rated or popular
                String [] projection = new String[]
                {
                    MovieEntry._ID,
                    MovieEntry.COLUMN_POSTER
                };
                String selection = MovieEntry.COLUMN_TYPE + "=?";
                String [] selectionArgs = new String[] { movieType };
                return new CursorLoader(
                    this,
                    MovieEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        int id = loader.getId();
        setLoadingIndicatorVisible(false);

        switch (id)
        {
            case MOVIE_SERVER_LOADER:
                if ((cursor == null || cursor.getCount() < 1))
                {
                    // error retrieving movies from server
                    mErrorMessageDisplay.setText(R.string.error_retrieving_movies);
                }
                break;
            case MOVIE_DB_LOADER:
                if (cursor == null || cursor.getCount() < 1)
                {
                    if (sMovieType.equals(FAVORITE))
                    {
                        // update empty state with no favorites stored
                        mErrorMessageDisplay.setText(R.string.error_no_favorites);
                    }
                }
                else
                {
                    mErrorMessageDisplay.setText("");
                }
                // Update CursorAdapter with new cursor containing updated movie data
                mMovieAdapter.swapCursor(cursor);
                mRecyclerView.invalidate();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        int id = loader.getId();
        // Loader reset, so we can clear out our existing data.
        switch (id)
        {
            case MOVIE_DB_LOADER:
                mMovieAdapter.swapCursor(null);
                break;
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key)
    {
        if (key.equals(getString(R.string.settings_order_by_key))) // ORDER_BY, for this app it's always true
        {
            sMovieType = getPreferenceOrderBy();
            Bundle bundle = new Bundle();
            bundle.putString("movie_type", sMovieType);

            if (getPreferenceOrderBy().equals(getString(R.string.settings_order_by_fav_value)))
            {
                getLoaderManager().restartLoader(MOVIE_DB_LOADER, bundle, this);
                // favorite => don't do network request
                return;
            }

            if (Utils.isNetworkAvailable(this))
            {
                // only valid for top_rated or popular, refresh and display again
                getLoaderManager().restartLoader(MOVIE_SERVER_LOADER, bundle, this);
                getLoaderManager().restartLoader(MOVIE_DB_LOADER, bundle, this);
            }
            else
            {
                getLoaderManager().restartLoader(MOVIE_DB_LOADER, bundle, this);
                // not connected, show message
                setLoadingIndicatorVisible(false);
                mErrorMessageDisplay.setText(R.string.error_no_internet_connection);
                Utils.showSnackbar(this, mRecyclerView,
                        getString(R.string.error_no_internet_connection));
            }
        }
    }


    private void setLoadingIndicatorVisible(boolean set)
    {
        if (set)
        {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            mErrorMessageDisplay.setVisibility(View.GONE);
        }
        else
        {
            mLoadingIndicator.setVisibility(View.GONE);
            mErrorMessageDisplay.setVisibility(View.VISIBLE);
        }

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        // update subtitle on screen rotation
        sMovieType = getPreferenceOrderBy();

        if (sMovieType.equals(getString(R.string.settings_order_by_popular_value)))
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_popular_label);
        }
        else if (sMovieType.equals(getString(R.string.settings_order_by_top_rated_value)))
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_top_rated_label);
        }
        else if (sMovieType.equals(getString(R.string.settings_order_by_fav_value)))
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_fav_label);
        }
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    /**
     * Check shared preferences and return the value of the selected preference
     */
    private String getPreferenceOrderBy()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }


    /**
     * Gives the exact movie being clicked so that I can display its detail
     */
    @Override
    public void onListItemClick(int clickedItemIndex)
    {
        Intent i = new Intent(this, DetailActivity.class);
        // Form the content URI that represents the specific movie that was clicked on,
        // by appending the "id" (passed as input to this method) onto the
        // FallaEntry#CONTENT_URI}.
        Uri moviesUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, clickedItemIndex);
        i.setData(moviesUri);
        startActivity(i);
    }


    /**
     * MENU OPTIONS
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        Intent i;
        switch (id)
        {
            case R.id.action_settings:
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}// END OF MAIN
