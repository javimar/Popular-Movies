package eu.javimar.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static eu.javimar.popularmovies.MainActivity.master_list;

public class DetailActivity extends AppCompatActivity
{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private TextView mTvMovieTitle, mTvReleaseDate, mTvSynopsis, mTvRating;
    private ImageView mIvPlacePoster;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // get movie position clicked
        int i = getIntent().getExtras().getInt("index", 0);

        initScreenItems();

        mTvMovieTitle.setText(master_list.get(i).getmTitle());
        mTvSynopsis .setText(master_list.get(i).getmOverview());
        // display only year
        mTvReleaseDate.setText(master_list.get(i).getmReleaseDate().substring(0,4));
        mTvRating.setText(String.format(getString(R.string.detail_ratings_literal),
                    String.valueOf(master_list.get(i).getmVoteAverage())));
        Picasso
            .with(this)
            .load(Utils.buildPosterUrl(i, Utils.DETAIL_ACTIVITY))
            .into(mIvPlacePoster);
    }


    private void initScreenItems()
    {
        mTvMovieTitle = (TextView)findViewById(R.id.tv_title);
        mTvReleaseDate = (TextView)findViewById(R.id.tv_date);
        mTvSynopsis = (TextView)findViewById(R.id.tv_synopsis);
        mTvRating = (TextView) findViewById(R.id.tv_rating);
        mIvPlacePoster = (ImageView)findViewById(R.id.placePoster);
    }


}
