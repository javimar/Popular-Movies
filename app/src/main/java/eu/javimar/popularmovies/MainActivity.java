package eu.javimar.popularmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.popularmovies.model.Movie;
import eu.javimar.popularmovies.model.MovieLoader;
import eu.javimar.popularmovies.view.MovieAdapter;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Movie>>,
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

    private static final int MOVIE_LOADER_ID = 0;

    /**
     * To check if we are connected to the Internet
     */
    boolean hasConnectivity = false;

    /**
     * The Master List of MOVIES where everything revolves around
     */
    public static List<Movie> master_list = new ArrayList<>();

    /**
     * URL bits and pieces for the themoviedb.org
     */
    private static final String BASE_URL = "http://api.themoviedb.org/3";
    private static final String TOP_RATED = "movie/top_rated";
    private static final String POPULAR = "movie/popular";
    private static final String API_KEY_TAG = "api_key";


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

        // start loader
        startServerLoader();
    }


    private void startServerLoader()
    {
        // If there is a network connection, fetch data
        if (Utils.isNetworkAvailable(this))
        {
            hasConnectivity = true;
            mLoadingIndicator.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        }
        else
        {
            // not connected, show error
            Utils.showSnackbar(this, mRecyclerView,
                    getString(R.string.error_no_internet_connection));
            hasConnectivity = false;
            // First, hide loading indicator so error message will be visible
            mLoadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection message
            mErrorMessageDisplay.setText(R.string.error_no_internet_connection);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // update subtitle on screen rotation
        if (getPreferenceOrderBy().equals(getString(R.string.settings_order_by_popular_value)))
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_popular_label);
        }
        else
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_top_rated_label);
        }
    }

    /**
     * LOADER METHODS
     */
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args)
    {
        // start building the URL
        Uri baseUri = Uri.parse(BASE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        if (getPreferenceOrderBy().equals(getString(R.string.settings_order_by_popular_value)))
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_popular_label);
            uriBuilder.appendEncodedPath(POPULAR);
        }
        else
        {
            getSupportActionBar().setSubtitle(R.string.settings_order_by_top_rated_label);
            uriBuilder.appendEncodedPath(TOP_RATED);
        }
        // add the API_KEY to the query
        uriBuilder.appendQueryParameter(API_KEY_TAG, getString(R.string.API_KEY));

        return new MovieLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies)
    {
        // If there is a valid list of movies, then add them to the adapter's data set.
        if (movies != null && !movies.isEmpty())
        {
            // Hide loading indicator because the data has been loaded OK
            mLoadingIndicator.setVisibility(View.GONE);
            // swap old data with new data
            mMovieAdapter.swap(movies);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        // Clear out our old data
    }


    private String getPreferenceOrderBy()
    {
        // Check shared preferences and return the value of the selected preference
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }


    /**
     * Gives me the exact movie being clicked so that I can display its detail
     */
    @Override
    public void onListItemClick(int clickedItemIndex)
    {
        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra("index", clickedItemIndex);
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
        switch (id) {
            case R.id.action_settings:
                i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key)
    {
        if (key.equals(getString(R.string.settings_order_by_key))) // ORDER_BY
        {
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }
    }


}// END OF MAIN
