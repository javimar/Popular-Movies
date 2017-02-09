package eu.javimar.popularmovies.model;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;

public class MovieProvider extends ContentProvider
{
    public static final String LOG_TAG = MovieProvider.class.getName();

    private static final int MOVIES = 100;
    private static final int MOVIES_ID = 101;
    private static final int TRAILERS = 200;
    private static final int MOVIE_TRAILER = 300;

    private static final SQLiteQueryBuilder sMovieAndTrailerJoin;
    static
    {
        sMovieAndTrailerJoin = new SQLiteQueryBuilder();
        sMovieAndTrailerJoin.setTables(
                    MovieEntry.TABLE_NAME + " INNER JOIN " +
                    TrailerEntry.TABLE_NAME +
                    " ON " +
                    MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_ID +
                    " = " +
                    TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID);
    }



    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,    // the authority
                MovieContract.PATH_MOVIES,          // the path
                MOVIES);                            // the matcher code
        // this will correspond to content://eu.javimar.popularmovies/movies/# and will be caught
        // by switching on MOVIES_ID (the matcher code)
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,
                MovieContract.PATH_MOVIES + "/#",
                MOVIES_ID);
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,
                MovieContract.PATH_TRAILERS,
                TRAILERS);
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,
                MovieContract.PATH_MOVIES + "/#/" + MovieContract.PATH_MOVIE_TRAILER,
                MOVIE_TRAILER);

    }
    /** Database helper object. All accesses to DB are done here */
    private MovieDbHelper mDbHelper;


    @Override
    public boolean onCreate()
    {
        mDbHelper = new MovieDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;

        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case MOVIES_ID:
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;


            /** HOW can I convert this into a query method using projection selection, etc.? */
            case MOVIE_TRAILER:
                cursor = sMovieAndTrailerJoin
                        .query(
                                database,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder);
                /*
                cursor = database.rawQuery(
                        "SELECT * FROM " +
                        MovieEntry.TABLE_NAME +
                        " INNER JOIN " +
                        TrailerEntry.TABLE_NAME +
                        " ON ( " +
                        MovieEntry.TABLE_NAME + "." + MovieEntry.COLUMN_ID +
                         " = " +
                        TrailerEntry.TABLE_NAME + "." + TrailerEntry.COLUMN_MOVIE_ID + " )" +
                        " WHERE " + MovieEntry.TABLE_NAME + "." + MovieEntry._ID + "=" + selection
                        , null);
                        */
                break;
            default:
                throw new IllegalArgumentException("Cannot query, unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return the cursor
        return cursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs)
    {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
                // Delete ALL rows that are NOT favorites per Loader instructions
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                rowsDeleted = database.delete(TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, notify listeners the data at the given URI has changed
        if (rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }



    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues,
                      String selection, String[] selectionArgs)
    {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case MOVIES:
                return updateMovie(uri, contentValues, selection, selectionArgs);
            case MOVIES_ID:
                selection = MovieEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateMovie(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    private int updateMovie(Uri uri, ContentValues values,
                          String selection, String[] selectionArgs)
    {
        // If the COLUMN_ID key is present, check that the ID value is not null.
        if (values.containsKey(MovieEntry.COLUMN_ID))
        {
            Integer movieId = values.getAsInteger(MovieEntry.COLUMN_ID);
            if (movieId == null)
            {
                throw new IllegalArgumentException("Movie requires a name");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0)
        {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(MovieEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }


    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values)
    {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri))
        {
            // only MOVIES is supported obviously, insert into DB
            case MOVIES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values)
                    {
                        // Check that the id is not null
                        Integer id = value.getAsInteger(MovieEntry.COLUMN_ID);
                        if (id == null)
                        {
                            throw new IllegalArgumentException("Movie requires an ID");
                        }
                        long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                        {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally
                {
                    db.endTransaction();
                }
                if (rowsInserted > 0)
                {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows inserted from our implementation of bulkInsert
                return rowsInserted;

            case TRAILERS:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values)
                    {
                        long _id = db.insert(TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1)
                        {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally
                {
                    db.endTransaction();
                }
                if (rowsInserted > 0)
                {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                // Return the number of rows inserted from our implementation of bulkInsert
                return rowsInserted;

            default:
                // If the URI does match match, return the super implementation of bulkInsert
                return super.bulkInsert(uri, values);
        }
    }



    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues)
    {
        // not implemented
        Log.i(LOG_TAG, "Insert is not implemented in this app");
        return null;
    }

    @Override
    public String getType(Uri uri)
    {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case MOVIES:
                return MovieEntry.CONTENT_LIST_TYPE;
            case MOVIES_ID:
                return MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return TrailerEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
