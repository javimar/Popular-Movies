package eu.javimar.popularmovies;

import android.content.Context;
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

import eu.javimar.popularmovies.model.Movie;

import static eu.javimar.popularmovies.MainActivity.master_list;

@SuppressWarnings("All")
public final class Utils
{
    private static final String LOG_TAG = Utils.class.getName();

    public static final String BASE_POSTER_URL = "http://image.tmdb.org/t/p";
    public static final String SIZE185 = "w185";
    public static final String SIZE500 = "w500";
    public static final String SIZE342 = "w342";

    public static final int MOVIE_ADAPTER = 90;
    public static final int DETAIL_ACTIVITY = 91;

    public static List<Movie> fetchMoviesData(String requestUrl)
    {
        //  this is the only public method here that the EarthquakeAsyncTask needs to interact with
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try
        {
            jsonResponse = makeHttpRequest(url);
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Movie> movies = extractMoviesFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return movies;
    }



    /**
     * Return a list of MOVIE objects that has been built up from parsing the given JSON response.
     */
    private static List<Movie> extractMoviesFromJson(String movieJSON)
    {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON))
        {
            return null;
        }

        // Create an empty ArrayList that we can start adding movies to
        List<Movie> movies = new ArrayList<>();

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

            // For each movie in the movie array, create a Movie object
            for (int i = 0; i < movieArray.length(); i++)
            {
                // Get a single movie at position i within the list of earthquakes
                JSONObject currentMovie = movieArray.getJSONObject(i);

                // Create a new Movie object with the magnitude, location, time,
                // and url from the JSON response.
                Movie movie = new Movie(
                        currentMovie.getInt("id"),
                        currentMovie.getString("original_title"),
                        currentMovie.getString("overview"),
                        currentMovie.getString("release_date"),
                        currentMovie.getDouble("vote_average"),
                        currentMovie.getDouble("popularity"),
                        currentMovie.getString("poster_path")
                );
                // Add the new Movie to the list of movies
                movies.add(movie);
            }
        }
        catch (JSONException e)
        {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the movies JSON results", e);
        }
        // Return the list of movies
        return movies;
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
        Snackbar snack = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        View sbview = snack.getView();
        sbview.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        TextView textView =
                (TextView) sbview.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        snack.show();
    }


    /** Helper method to build the poster URL */
    public static String buildPosterUrl(int position, int caller)
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
        uriBuilder.appendEncodedPath(master_list.get(position).getmPosterPath());

        return uriBuilder.toString();
    }


}
