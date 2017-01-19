package eu.javimar.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import eu.javimar.popularmovies.model.Movie;

import static eu.javimar.popularmovies.MainActivity.master_list;


@SuppressWarnings("all")
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>
{
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private Context mContext;

    /** An on-click handler to make it easy for an Activity to interface with our RecyclerView */
    private final ListItemClickListener mOnClickListener;
    /** The interface that receives onClick messages */
    public interface ListItemClickListener
    {
        void onListItemClick(int clickedItemIndex);
    }


    /** Adapter constructor */
    public MovieAdapter(ListItemClickListener listener, Context context)
    {
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
        Picasso
            .with(mContext)
            .load(Utils.buildPosterUrl(position, Utils.MOVIE_ADAPTER))
            .into(holder.mImageView);
    }

    @Override
    public int getItemCount()
    {
        return master_list.size();
    }

    public void swap(List<Movie> data)
    {
        master_list.clear();
        master_list.addAll(data);
        notifyDataSetChanged();
    }


    /**
     * Cache of the children views for a movie list item.
     */
    public class MovieViewHolder extends RecyclerView.ViewHolder
                                    implements View.OnClickListener
    {
        private ImageView mImageView;

        public MovieViewHolder(View itemView)
        {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.moviePosterView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }



}
