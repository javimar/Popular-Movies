package eu.javimar.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable
{
    private int mId;
    private String mTitle;
    private String mOverview;
    private String mReleaseDate;
    private double mVoteAverage;
    private String mPosterPath;
    private int mFavorite;
    private String mType;

    public Movie(int id, String title, String overview, String releaseDate,
                  double voteAverage, String poster, int favorite, String type)
    {
        this.mId = id;
        this.mPosterPath = poster;
        this.mOverview = overview;
        this.mReleaseDate = releaseDate;
        this.mTitle = title;
        this.mVoteAverage = voteAverage;
        this.mFavorite = favorite;
        this.mType = type;
    }


    /** PARCELABLE STUFF */

    private Movie(Parcel in)
    {
        mId = in.readInt();
        mTitle = in.readString();
        mOverview = in.readString();
        mReleaseDate = in.readString();
        mVoteAverage = in.readDouble();
        mPosterPath = in.readString();
        mFavorite = in.readInt();
        mType = in.readString();
    }

    // This is where you write the values you want to save to the "Parcel".
    // Note that there are only methods defined for simple values, lists, and other Parcelable objects.
    // You may need to make several classes Parcelable to send the data you want.
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mId);
        dest.writeString(mTitle);
        dest.writeString(mOverview);
        dest.writeString(mReleaseDate);
        dest.writeDouble(mVoteAverage);
        dest.writeString(mPosterPath);
        dest.writeInt(mFavorite);
        dest.writeString(mType);
    }

    // After implementing the "Parcelable" interface, we need to create the
    // "Parcelable.Creator<MyParcelable> CREATOR" constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>()
    {
        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }
        // We just need to copy this and change the type to match our class.
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }


    // Getters
    public String getmOverview() {
        return mOverview;
    }
    public String getmReleaseDate() {
        return mReleaseDate;
    }
    public String getmTitle() {
        return mTitle;
    }
    public double getmVoteAverage() {
        return mVoteAverage;
    }
    public String getmPosterPath() {
        return mPosterPath;
    }
    public int getmId() {
        return mId;
    }
    public int getmFavorite() {
        return mFavorite;
    }
    public String getmType() {
        return mType;
    }
}
