package eu.javimar.popularmovies.model;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.VideoEntry;

public class MovieProvider extends ContentProvider
{
    public static final String LOG_TAG = MovieProvider.class.getName();

    private static final int MOVIES = 100;
    private static final int MOVIES_ID = 101;
    private static final int VIDEOS = 200;
    private static final int VIDEOS_ID = 201;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,    // the authority
                MovieContract.PATH_MOVIES,          // the path
                MOVIES);                            // the matcher code
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,
                MovieContract.PATH_MOVIES + "/#",
                MOVIES_ID);

        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,
                MovieContract.PATH_VIDEOS,
                VIDEOS);
        sUriMatcher.addURI(
                MovieContract.CONTENT_AUTHORITY,
                MovieContract.PATH_VIDEOS + "/#",
                VIDEOS_ID);
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
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match)
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

        // Track the number of rows that were deleted
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case MOVIES:
                // Delete ALL rows that are NOT favorites per Loader instructions
                rowsDeleted = database.delete(MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
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
            case VIDEOS:
                return VideoEntry.CONTENT_LIST_TYPE;
            case VIDEOS_ID:
                return VideoEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues)
    {
        final int match = sUriMatcher.match(uri);
        switch (match)
        {
            case MOVIES:
                return insertMovie(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    private Uri insertMovie(Uri uri, ContentValues values)
    {
        Integer movieId = values.getAsInteger(MovieEntry.COLUMN_ID);
        if (movieId == null)
        {
            throw new IllegalArgumentException("Movie requires a name");
        }
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(MovieEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1)
        {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
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
                        // Check that the name is not null
                        String name = value.getAsString(MovieEntry.COLUMN_ID);
                        if (name == null)
                        {
                            throw new IllegalArgumentException("Movie requires a name");
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
            default:
                // If the URI does match match FALLAS, return the super implementation of bulkInsert
                return super.bulkInsert(uri, values);
        }
    }
}
