package eu.javimar.popularmovies;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;


@SuppressWarnings("All")
public final class Utils
{
    private static final String LOG_TAG = Utils.class.getName();

    /** URLs */
    public static final String BASE_URL = "http://api.themoviedb.org/3";
    public static final String BASE_POSTER_URL = "http://image.tmdb.org/t/p";

    /** URL bits and pieces to form URLs */
    public static final String MOVIE_PATH = "movie";
    public static final String VIDEOS_PATH = "videos";
    public static final String REVIEWS_PATH = "reviews";
    public static final String API_KEY_TAG = "api_key";
    //http://api.themoviedb.org/3/movie/ID/videos?api_key=API_KEY
    //http://api.themoviedb.org/3/movie/ID/reviews?api_key=API_KEY

    /** Poster sizes */
    private static final String SIZE185 = "w185";
    private static final String SIZE500 = "w500";
    private static final String SIZE342 = "w342";

    /** Constants to build correct poster URL */
    public static final int MOVIE_ADAPTER = 60;
    public static final int DETAIL_ACTIVITY = 61;

    /** Store the value of the settings preference. FAV, POPULAR or TOP_RATED */
    public static String sMovieType;

    /** CV lists to bulk insert into the 3 DBs */
    public static List<ContentValues> sMoviesCv, sTrailersCv, sReviewsCv;


    // Get all movie info asynchronously
    public static void loadMoviesData(String requestUrl, Context context)
    {
        // 1. Create URL object
        URL url = createUrl(requestUrl);

        // 2. Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try
        {
            jsonResponse = makeHttpRequest(url);
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // 3. Extract relevant fields from the JSON response and create a list of content values
        extractMoviesFromJson(jsonResponse, context);
        // 4. insert complete list movies in DB
        int m = context.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI,
                sMoviesCv.toArray(new ContentValues[sMoviesCv.size()]));
    }



    /**
     * Return a list of CV for movies built up from parsing the given JSON response.
     */
    private static void extractMoviesFromJson(String movieJSON, Context context)
    {
        // If the JSON string is empty or null, return early.
        if (TextUtils.isEmpty(movieJSON))
        {
            return;
        }
        // initialize
        ContentValues movieCv;
        sMoviesCv = new ArrayList<>();
        int movie_id = 0;

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try
        {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(movieJSON);

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of movies.
            JSONArray movieArray = baseJsonResponse.getJSONArray("results");

            // For each movie in the movie array, create a CV object
            for (int i = 0; i < movieArray.length(); i++)
            {
                movieCv = new ContentValues();
                // Get a single movie at position i
                JSONObject currentMovie = movieArray.getJSONObject(i);
                movie_id = currentMovie.getInt("id");
                // Create a new CV
                movieCv.put(MovieEntry.COLUMN_ID, movie_id);
                movieCv.put(MovieEntry.COLUMN_TITLE, currentMovie.getString("original_title"));
                movieCv.put(MovieEntry.COLUMN_OVERVIEW, currentMovie.getString("overview"));
                movieCv.put(MovieEntry.COLUMN_DATE, currentMovie.getString("release_date"));
                movieCv.put(MovieEntry.COLUMN_RATING, currentMovie.getDouble("vote_average"));
                movieCv.put(MovieEntry.COLUMN_POSTER, currentMovie.getString("poster_path"));
                movieCv.put(MovieEntry.COLUMN_FAV, 0);
                movieCv.put(MovieEntry.COLUMN_TYPE, sMovieType);

                // Add the new Movie to the list of movies
                sMoviesCv.add(movieCv);

                // for every movie added, process all its trailers
                processTrailers(movie_id, context);
                // insert complete list of trailer in DB
                int t = context.getContentResolver().bulkInsert(TrailerEntry.CONTENT_URI,
                        sTrailersCv.toArray(new ContentValues[sTrailersCv.size()]));
            }
        }
        catch (JSONException e)
        {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the movies JSON results", e);
        }
    }


    /**
     * Init point to process trailers table
     */
    private static void processTrailers(int movie_id, Context context)
    {
        // Start building: http://api.themoviedb.org/3/movie/ID/videos?api_key=API_KEY
        Uri baseUri = Uri.parse(BASE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendEncodedPath(MOVIE_PATH);
        uriBuilder.appendEncodedPath(String.valueOf(movie_id));
        uriBuilder.appendEncodedPath(VIDEOS_PATH);
        // add the API_KEY to the query ?q=
        uriBuilder.appendQueryParameter(API_KEY_TAG, context.getString(R.string.API_KEY));

        // 1. Create URL object
        URL url = createUrl(uriBuilder.toString());

        // 2. Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try
        {
            jsonResponse = makeHttpRequest(url);
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // 3. Extract relevant fields from the JSON response and create a list of content values
        extractTrailersFromJson(jsonResponse);

        // 4. insert complete list in DB. Static list sTrailersCv is completed
        // this is done in the caller function after inserting the movies first to avoid
        // foreign key constraints
    }

    /**
     * Return a list of CV for trailers built up from parsing the given JSON response.
     */
    private static void extractTrailersFromJson(String trailerJSON)
    {
        // If the JSON string is empty or null, return early.
        if (TextUtils.isEmpty(trailerJSON))
        {
            return;
        }
        // initialize
        ContentValues trailerCv;
        sTrailersCv = new ArrayList<>();

        try
        {
            JSONObject baseJsonResponse = new JSONObject(trailerJSON);
            JSONArray trailerArray = baseJsonResponse.getJSONArray("results");

            for (int i = 0; i < trailerArray.length(); i++)
            {
                trailerCv = new ContentValues();
                JSONObject currentTrailer = trailerArray.getJSONObject(i);
                trailerCv.put(TrailerEntry.COLUMN_ID, currentTrailer.getString("id"));
                trailerCv.put(TrailerEntry.COLUMN_NAME, currentTrailer.getString("name"));
                trailerCv.put(TrailerEntry.COLUMN_KEY, currentTrailer.getString("key"));
                trailerCv.put(TrailerEntry.COLUMN_MOVIE_ID, baseJsonResponse.getInt("id"));

                // Add the new Trailer to the list
                sTrailersCv.add(trailerCv);
            }
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, "Problem parsing the trailers JSON results", e);
        }
    }



    /**
     * Simply returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl)
    {
        URL url = null;
        try
        {
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e)
        {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }



    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException
    {
        String jsonResponse = "";

        // If the URL is null, then exit
        if (url == null)
        {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try
        {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200)
            {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else
            {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Problem retrieving the movies JSON results.", e);
        }
        finally
        {
            if (urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if (inputStream != null)
            {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * Convert the InputStream into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException
    {
        StringBuilder output = new StringBuilder();
        if (inputStream != null)
        {
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null)
            {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }



    /** HELPER METHODS */

    /** Returns true if the network is connected or about to become available */
    public static boolean isNetworkAvailable(Context c)
    {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    /** Displays colorful snackbar messages */
    public static void showSnackbar (Context context, View view, String message)
    {
        Snackbar snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View sbview = snack.getView();
        sbview.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView =
                (TextView) sbview.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snack.show();
    }


    /** Helper method to build the poster URL */
    public static String buildPosterUrl(Cursor cursor, int caller)
    {
        // start building the URL
        Uri baseUri = Uri.parse(BASE_POSTER_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        switch(caller)
        {
            case MOVIE_ADAPTER:
                uriBuilder.appendEncodedPath(SIZE185);
                break;
            case DETAIL_ACTIVITY:
                uriBuilder.appendEncodedPath(SIZE342);
                break;
        }
        uriBuilder.appendEncodedPath(cursor.getString(
                    cursor.getColumnIndex(MovieEntry.COLUMN_POSTER)));

        return uriBuilder.toString();
    }


}
