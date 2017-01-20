package eu.javimar.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static eu.javimar.popularmovies.MainActivity.master_list;

public class DetailActivity extends AppCompatActivity
{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.tv_title) TextView mTvMovieTitle;
    @BindView(R.id.placePoster) ImageView mIvPlacePoster;
    @BindView(R.id.tv_date) TextView mTvReleaseDate;
    @BindView(R.id.tv_rating) TextView mTvRating;
    @BindView(R.id.tv_synopsis) TextView mTvSynopsis;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        // get movie position clicked
        int i = getIntent().getExtras().getInt("index", 0);

        mTvMovieTitle.setText(master_list.get(i).getmTitle());
        mTvSynopsis.setText(master_list.get(i).getmOverview());
        // display only year
        mTvReleaseDate.setText(master_list.get(i).getmReleaseDate().substring(0, 4));
        mTvRating.setText(String.format(getString(R.string.detail_ratings_literal),
                String.valueOf(master_list.get(i).getmVoteAverage())));
        Picasso
                .with(this)
                .load(Utils.buildPosterUrl(i, Utils.DETAIL_ACTIVITY))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(mIvPlacePoster);
    }

}
