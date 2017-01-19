package eu.javimar.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

import eu.javimar.popularmovies.model.Movie;


@SuppressWarnings("all")
public class MovieLoader extends AsyncTaskLoader<List<Movie>>
{
    private String mUrl;
    private List<Movie> mMovie;

    public MovieLoader(Context context, String url)
    {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading()
    {
        forceLoad();
    }


    @Override
    public List<Movie> loadInBackground()
    {
        if (mUrl == null) {
            return null;
        }
        // Perform the network request, parse the response, and extract a list of movies.
        // pass the context since it will be needed to get the preferences
        return Utils.fetchMoviesData(mUrl);
    }

}
