package eu.javimar.popularmovies.model;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.util.Log;

import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;
import eu.javimar.popularmovies.model.MovieContract.ReviewEntry;

public class MovieProvider extends ContentProvider
{
    public static final String LOG_TAG = MovieProvider.class.getName();

    private static final int MOVIES = 100;
    private static final int MOVIES_ID = 101;
    private static final int TRAILERS = 200;
    private static final int REVIEWS = 300;

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
                MovieContract.PATH_REVIEWS,
                REVIEWS);
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
                selection = MovieEntry._mID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case TRAILERS:
                cursor = database.query(TrailerEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case REVIEWS:
                cursor = database.query(ReviewEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
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
                selection = MovieEntry.MOVIE_POS + "=?";
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
    public Uri insert(@NonNull Uri uri, ContentValues values)
    {
        switch (sUriMatcher.match(uri))
        {
            case MOVIES:
                SQLiteDatabase database = mDbHelper.getWritableDatabase();
                long id = database.insert(MovieEntry.TABLE_NAME, null, values);
                // If the ID is -1, then the insertion failed. Log an error and return null.
                if (id == -1)
                {
                    Log.e(LOG_TAG, "Failed to insert row for " + uri);
                    return null;
                }
                // Notify all listeners that the data has changed for content URI
                getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, id);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }



    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values)
    {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsInserted;
        switch (sUriMatcher.match(uri))
        {
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

            case REVIEWS:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values)
                    {
                        long _id = db.insert(ReviewEntry.TABLE_NAME, null, value);
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
            case REVIEWS:
                return ReviewEntry.CONTENT_LIST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
