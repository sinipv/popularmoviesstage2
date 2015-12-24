package com.example.app.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment containing movie details.
 */
public class DetailFragment extends Fragment {

    public DetailFragment() {
    }

    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;
    private MovieAppHelper helper = new MovieAppHelper();
    public static final String MOVIE_TAG = "movie";
    private Movie movie = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MOVIE_TAG)) {
            movie = intent.getParcelableExtra(MOVIE_TAG);

        } else if (getArguments() != null && getArguments().containsKey(MOVIE_TAG)){
            movie = getArguments().getParcelable(MOVIE_TAG);
        }

        if(movie != null) {
            ((TextView) rootView.findViewById(R.id.title_text)).setText(movie.getTitle());
            ((TextView) rootView.findViewById(R.id.release_date_text)).setText(movie.getReleaseDate());
            ((TextView) rootView.findViewById(R.id.ratings_text)).setText(movie.getRating());
            ((TextView) rootView.findViewById(R.id.synopsis_text)).setText(movie.getSynopsis());

            movie.setFavorite(helper.isFavorite(getActivity(), movie.getId()));

            ImageButton favoriteButton = (ImageButton) rootView.findViewById(R.id.favorite_btn);
            favoriteButton.setSelected(movie.isFavorite());
            favoriteButton.setTag(movie.getId());
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    movie.setFavorite(!movie.isFavorite());
                    view.setSelected(movie.isFavorite());
                    FavoritesTask favoritesTask = new FavoritesTask();
                    favoritesTask.execute(movie.getId(), String.valueOf(movie.isFavorite()));
                }
            });

            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

            Picasso.with(getActivity())
                    .load(movie.getMovieUrl())
                    .placeholder(R.drawable.user_placeholder)
                    .error(R.drawable.user_placeholder)
                    .into(imageView);


            trailerAdapter = new TrailerAdapter(getContext(), R.layout.fragment_detail, movie.getTrailers());

            ListView listView = (ListView) rootView.findViewById(R.id.listview_trailer);
            trailerAdapter.notifyDataSetChanged();
            listView.setAdapter(trailerAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Trailer trailer = trailerAdapter.getItem(i);
                    Uri youtube = Uri.parse("http://www.youtube.com/watch?v=" + trailer.getSource());
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, youtube);
                    startActivity(webIntent);
                }
            });
            reviewAdapter = new ReviewAdapter(getContext(), R.layout.fragment_detail, movie.getReviews());

            ListView listView2 = (ListView) rootView.findViewById(R.id.listview_review);
            reviewAdapter.notifyDataSetChanged();
            listView2.setAdapter(reviewAdapter);
        }
        return rootView;
    }

    public class FavoritesTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String movieId = params[0];
            boolean isFavorite = Boolean.parseBoolean(params[1]);

            if (isFavorite) {
                helper.addToFavorites(getActivity(), movieId);
                return "Added to favorites";
            } else {
                helper.removeFromFavorites(getActivity(), movieId);
                return "Removed from favorites";
            }
        }

        @Override
        protected void onPostExecute(String operation) {
            Toast.makeText(getContext(), operation, Toast.LENGTH_LONG).show();
        }

    }



    class TrailerAdapter extends ArrayAdapter<Trailer> {
        private Context context;
        List<Trailer> trailers = new ArrayList<Trailer>();

        public TrailerAdapter(Context context, int layoutResourceId, ArrayList<Trailer> trailers) {
            super(context, 0, trailers);
            this.context = context;
            this.trailers = trailers;
        }

        public void setTrailer(ArrayList<Trailer> trailers) {
            trailers.clear();
            this.trailers = trailers;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Trailer getItem(int position) {
            return trailers.get(position);
        }

        @Override
        public int getCount() {
            if (trailers != null)
                return trailers.size();
            else
                return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final String KEY;
            Trailer trailer = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_detail_list, parent, false);
            }

            TextView trailerName = (TextView) convertView.findViewById(R.id.detail_text);
            trailerName.setText(trailer.getName());
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView3);
            imageView.setImageResource(R.drawable.play_btn);
           return convertView;
        }

    }

    class ReviewAdapter extends ArrayAdapter<Review> {
        private Context context;
        List<Review> reviews = new ArrayList<Review>();

        public ReviewAdapter(Context context, int layoutResourceId, ArrayList<Review> reviews) {
            super(context, 0, reviews);
            this.context = context;
            this.reviews = reviews;
        }

        public void setTrailer(ArrayList<Review> reviews) {
            reviews.clear();
            this.reviews = reviews;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Review getItem(int position) {
            return reviews.get(position);
        }

        @Override
        public int getCount() {
            if (reviews != null)
                return reviews.size();
            else
                return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Review review = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_detail_review_list, parent, false);
            }
            TextView reviewAuthor = (TextView) convertView.findViewById(R.id.author_text);
            TextView reviewContent = (TextView) convertView.findViewById(R.id.content_text);
            reviewAuthor.setText(review.getAuthor());
            reviewContent.setText(review.getContent());

            ImageView imageView = (ImageView) convertView.findViewById(R.id.person_icon);
            imageView.setImageResource(R.drawable.user_icon);
            return convertView;
        }
    }
}
