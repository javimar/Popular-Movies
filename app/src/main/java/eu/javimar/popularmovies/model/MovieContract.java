package eu.javimar.popularmovies.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

@SuppressWarnings("all")
public final class MovieContract
{
    private MovieContract() {}

    public static final String CONTENT_AUTHORITY = "eu.javimar.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_MOVIE_TRAILER = "movie_trailer";


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
        public static final String TABLE_NAME = "movie";

        public static final String _mID = BaseColumns._ID;
        public static final String COLUMN_ID = "movieid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_POSTER = "poster_path";
        public static final String COLUMN_FAV = "favorite";
        public static final String COLUMN_TYPE = "type";

        public static final String MOVIE_POS = TABLE_NAME + _mID;
    }


    public static class TrailerEntry implements BaseColumns
    {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_TRAILERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_TRAILERS;
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TRAILERS);

        /**
         * Name and columns of database table for trailer videos
         */
        public static final String TABLE_NAME = "trailer";

        public static final String _tID = BaseColumns._ID;
        public static final String COLUMN_NAME = "trailer_name";
        public static final String COLUMN_KEY = "trailer_key";
        public static final String MOVIE_POS = "tmovie_pos";

        public static final String TRAILER_POS = TABLE_NAME + _tID;
    }


    public static class ReviewEntry implements BaseColumns
    {
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_REVIEWS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY +
                        "/" + PATH_REVIEWS;
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_REVIEWS);

        /**
         * Name and columns of database table for reviews
         */
        public static final String TABLE_NAME = "review";

        public static final String _rID = BaseColumns._ID;
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String MOVIE_POS = "rmovie_pos";

        public static final String REVIEW_POS = TABLE_NAME + _rID;
    }

}
