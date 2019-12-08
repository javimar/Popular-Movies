package eu.javimar.popularmovies.model;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import eu.javimar.popularmovies.Utils;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;



@SuppressWarnings("all")
public class MovieLoader extends AsyncTaskLoader<Cursor>
{
    private String mUrl;
    private Context mContext;

    private static final String LOG_TAG = MovieLoader.class.getSimpleName();

    public MovieLoader(Context context, String url)
    {
        super(context);
        mContext = context;
        mUrl = url;
    }


    @Override
    protected void onStartLoading()
    {
        // only connect to the API once per run
        if(Utils.sConnectToApi)
            forceLoad();

        Utils.sConnectToApi = false;
    }


    @Override
    public Cursor loadInBackground()
    {
        if (mUrl == null) {
            return null;
        }

        // delete everything that is not a favorite
        String selection = MovieEntry.COLUMN_FAV + "=?";
        String [] selectionArgs = new String[] { String.valueOf(0) };
        int rowsDeleted = mContext.getContentResolver()
                .delete(MovieEntry.CONTENT_URI, selection, selectionArgs);

        // Perform the network request to store all movie info into the database
        Utils.loadMoviesData(mUrl, mContext);

        return null;
    }

}
