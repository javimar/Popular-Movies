package eu.javimar.popularmovies.model;


public class Movie
{
    private int mId;
    private String mTitle;
    private String mOverview;
    private String mReleaseDate;
    private double mVoteAverage;
    private double mPopularity;
    private String mPosterPath;

    public Movie(int id, String title, String overview, String releaseDate,
                  double voteAverage, double popularity, String poster)
    {
        this.mId = id;
        this.mPosterPath = poster;
        this.mOverview = overview;
        this.mReleaseDate = releaseDate;
        this.mTitle = title;
        this.mPopularity = popularity;
        this.mVoteAverage = voteAverage;
    }


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
    public double getmPopularity() {
        return mPopularity;
    }
    public int getmId() {
        return mId;
    }
    public String getmPosterPath() {
        return mPosterPath;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "Id=" + mId +
                ", Title='" + mTitle + '\'' +
                ", Overview='" + mOverview + '\'' +
                ", ReleaseDate='" + mReleaseDate + '\'' +
                ", VoteAverage=" + mVoteAverage +
                ", Popularity=" + mPopularity +
                ", PosterPath='" + mPosterPath + '\'' +
                '}';
    }
}
