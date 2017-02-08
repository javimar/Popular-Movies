package eu.javimar.popularmovies.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

@SuppressWarnings("all")
public class MovieContract
{
    private MovieContract() {}

    public static final String CONTENT_AUTHORITY = "eu.javimar.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_VIDEOS = "videos";

    public static class MovieEntry implements BaseColumns
    {
        /**
         * The MIME type of the #CONTENT_URI for a list of movies
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_MOVIES;
        /**
         * The MIME type of the #CONTENT_URI for a single movie
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_MOVIES;
        /**
         * The content URI to access the movie data in the provider
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIES);
        /**
         * Name and columns of database table for favorites movie
         */
        public static final String TABLE_NAME = "favmovies";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_POSTER = "poster_path";
        public static final String COLUMN_FAV = "favorite";
        public static final String COLUMN_TYPE = "type";

    }


    public static class VideoEntry implements BaseColumns
    {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_VIDEOS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_VIDEOS;

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VIDEOS);

        /**
         * Name and columns of database table for trailer videos
         */
        public static final String TABLE_NAME = "videos";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }

}
