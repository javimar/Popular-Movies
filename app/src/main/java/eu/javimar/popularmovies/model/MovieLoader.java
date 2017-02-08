package eu.javimar.popularmovies.model;

import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.List;

import eu.javimar.popularmovies.Utils;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;


@SuppressWarnings("all")
public class MovieLoader extends AsyncTaskLoader<Cursor>
{
    private String mUrl, mMovieType;
    private Context mContext;

    private static final String LOG_TAG = MovieLoader.class.getSimpleName();

    public MovieLoader(Context context, String url, String movieType)
    {
        super(context);
        mMovieType = movieType;
        mContext = context;
        mUrl = url;
    }

    @Override
    protected void onStartLoading()
    {
        forceLoad();
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

        // Perform the network request
        List<ContentValues> listCv = Utils.fetchMoviesData(mUrl, mMovieType);
        // insert complete list in DB
        final Integer count = listCv.size();
        mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI,
                listCv.toArray(new ContentValues[count]));

        return null;
    }

}
