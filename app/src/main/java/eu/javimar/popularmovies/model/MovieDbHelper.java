package eu.javimar.popularmovies.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;
import eu.javimar.popularmovies.model.MovieContract.TrailerEntry;
import eu.javimar.popularmovies.model.MovieContract.ReviewEntry;

public class MovieDbHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "movies.db";
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
                        + MovieEntry.MOVIE_POS + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + MovieEntry.COLUMN_ID + " INTEGER NOT NULL, "
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
                        + TrailerEntry.TRAILER_POS + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TrailerEntry.COLUMN_NAME + " TEXT, "
                        + TrailerEntry.COLUMN_KEY + " TEXT, "
                        + TrailerEntry.MOVIE_POS + " INTEGER, "
                        + " CONSTRAINT fk_movie_id "
                        + " FOREIGN KEY (" + TrailerEntry.MOVIE_POS + ") REFERENCES "
                        + MovieEntry.TABLE_NAME + " (" + MovieEntry.MOVIE_POS + ")"
                        + " ON DELETE CASCADE "
                       // + "UNIQUE ( "
                       // + TrailerEntry.MOVIE_POS
                       // + " ) ON CONFLICT REPLACE "
                        + " );";

        String SQL_CREATE_REVIEW_TABLE =
                "CREATE TABLE "
                        + ReviewEntry.TABLE_NAME + " ("
                        + ReviewEntry.REVIEW_POS + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ReviewEntry.COLUMN_AUTHOR + " TEXT, "
                        + ReviewEntry.COLUMN_CONTENT + " TEXT, "
                        + ReviewEntry.MOVIE_POS + " INTEGER, "
                        + " CONSTRAINT fk_movie_id "
                        + " FOREIGN KEY (" + ReviewEntry.MOVIE_POS + ") REFERENCES "
                        + MovieEntry.TABLE_NAME + " (" + MovieEntry.MOVIE_POS + ")"
                        + " ON DELETE CASCADE "
                        + " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

}
