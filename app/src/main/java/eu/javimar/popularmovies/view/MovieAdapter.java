package eu.javimar.popularmovies.view;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.popularmovies.R;
import eu.javimar.popularmovies.Utils;
import eu.javimar.popularmovies.model.MovieContract.MovieEntry;


@SuppressWarnings("all")
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>
{
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;

    /**
     * An on-click handler to make it easy for an Activity to interface with our RecyclerView
     */
    private final ListItemClickListener mOnClickListener;

    /**
     * The interface that receives onClick messages
     */
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }


    /**
     * Adapter constructor
     */
    public MovieAdapter(ListItemClickListener listener, Context context) {
        mContext = context;
        mOnClickListener = listener;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     */
    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.movies_list_item, parent, false);
        return new MovieViewHolder(view);
    }


    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * details for this particular position, using the "position" argument passed into us.
     */
    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position)
    {
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null
        mCursor.moveToPosition(position);

        Picasso
                .with(mContext)
                .load(Utils.buildPosterUrl(mCursor, Utils.MOVIE_ADAPTER))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null)
            return mCursor.getCount();
        return 0;
    }

    public void swapCursor(Cursor newCursor)
    {
        if (newCursor != null)
        {
            mCursor = newCursor;
            notifyDataSetChanged();
        }
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public class MovieViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        @BindView(R.id.moviePosterView) ImageView mImageView;

        public MovieViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            mOnClickListener.onListItemClick(getIdMovie(getAdapterPosition()));
        }
    }

    /** Returns value of column from current position */
    private int getIdMovie(int position)
    {
        if (mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getInt(mCursor.getColumnIndex(MovieEntry._ID));
            }
            else {
                return -1;
            }
        }
        else {
            return -1;
        }
    }

    public Cursor getCursor()
    {
        return mCursor;
    }

}
