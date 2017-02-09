package eu.javimar.popularmovies.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;
import eu.javimar.popularmovies.model.MovieContract.ReviewEntry;

public class MovieDbHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "favmovies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE "
                        + MovieEntry.TABLE_NAME + " ("
                        + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + MovieEntry.COLUMN_ID + " INTEGER NOT NULL UNIQUE, "
                        + MovieEntry.COLUMN_TITLE + " TEXT, "
                        + MovieEntry.COLUMN_RATING + " REAL, "
                        + MovieEntry.COLUMN_OVERVIEW + " TEXT, "
                        + MovieEntry.COLUMN_DATE + " TEXT, "
                        + MovieEntry.COLUMN_POSTER + " TEXT, "
                        + MovieEntry.COLUMN_FAV + " INTEGER, "
                        + MovieEntry.COLUMN_TYPE + " TEXT, "

                        // To ensure we don't overwrite fav movies create a UNIQUE constraint
                        // with IGNORE strategy
                        + " UNIQUE (" + MovieEntry.COLUMN_ID + ") "
                        + " ON CONFLICT IGNORE);";

        String SQL_CREATE_TRAILER_TABLE =
                "CREATE TABLE "
                        + TrailerEntry.TABLE_NAME + " ("
                        + TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TrailerEntry.COLUMN_ID + " TEXT NOT NULL , "
                        + TrailerEntry.COLUMN_NAME + " TEXT, "
                        // the ID of the movie associated with this video
                        + TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                        // the key needed to visualize it in youtube
                        + TrailerEntry.COLUMN_KEY + " TEXT, "

                        + " CONSTRAINT fk_trailer_id "
                        + " FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES "
                        + MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_ID + ")"
                        + " ON DELETE CASCADE "
                        + " );";


        // coming soon
        String SQL_CREATE_REVIEW_TABLE =
                "CREATE TABLE "
                        + ReviewEntry.TABLE_NAME + " ("
                        + ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ReviewEntry.COLUMN_ID + " TEXT NOT NULL, "
                        + ReviewEntry.COLUMN_AUTHOR + " TEXT, "
                        + ReviewEntry.COLUMN_CONTENT + " TEXT, "
                        + TrailerEntry.COLUMN_MOVIE_ID + " INTEGER, "

                        // Set up foreign key to movie table.
                        + " FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES "
                        + MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_ID + "));";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        //db.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }
}
