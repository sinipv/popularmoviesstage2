package com.example.app.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {
    private final String LOG_TAG = MovieFragment.class.getSimpleName();

    private static final String SAVED_MOVIE_LIST = "movies_list_save";
    private static final String SELECTED_MOVIE_POSITION = "selected_movie_position";
    private static final String TWO_PANE = "two_pane";
    private ImageAdapter imageAdapter;
    private boolean mTwoPane = false;
    private int mPosition = ListView.INVALID_POSITION;
    private GridView gridView = null;
    ArrayList<Movie> moviesList = new ArrayList<Movie>();


    private MovieAppHelper helper = new MovieAppHelper();

    public MovieFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        Log.v(LOG_TAG, "inside oncreate :savedInstanceState:" + savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_MOVIE_LIST)) {
            moviesList = savedInstanceState.getParcelableArrayList(SAVED_MOVIE_LIST);
            mPosition = savedInstanceState.getInt(SELECTED_MOVIE_POSITION);
            mTwoPane = savedInstanceState.getBoolean(TWO_PANE);
        }
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_MOVIE_LIST, moviesList);
        outState.putInt(SELECTED_MOVIE_POSITION, mPosition);
        outState.putBoolean(TWO_PANE, mTwoPane);
        super.onSaveInstanceState(outState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        imageAdapter = new ImageAdapter(getContext(), R.layout.fragment_movie, moviesList);
        gridView = (GridView) rootView.findViewById(R.id.movieGridView);
        imageAdapter.notifyDataSetChanged();
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                if(mTwoPane) {
                    ((Callback) getActivity()).onItemSelected(imageAdapter.getItem(i));
                }
                    else{
                        Movie movie = imageAdapter.getItem(i);
                        intent.putExtra("movie", movie);
                        startActivity(intent);
                    }
                mPosition = i;
            }
        });

        return rootView;
    }

    private void getMovies() {

        final String POPULARITY_DESC = "popularity.desc";
        final String VOTE_AVERAGE_DESC = "vote_average.desc";
        final String POPULARITY = "Popularity";
        final String FAVORITES = "Favorites";

        FetchMovieTask movieTask = new FetchMovieTask();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = preferences.getString(getString(R.string.pref_sortby_key),
                (getString(R.string.pref_sortby_default)));

       if(POPULARITY.equalsIgnoreCase(sortBy)) {
            sortBy = POPULARITY_DESC;
        } else if(VOTE_AVERAGE_DESC.equalsIgnoreCase(sortBy)){
            sortBy = VOTE_AVERAGE_DESC;
        } else if(FAVORITES.equalsIgnoreCase(sortBy)){
           sortBy = FAVORITES;
       } else {
           sortBy = POPULARITY_DESC;
       }
        movieTask.execute(sortBy);
    }

    public void onStart() {
        Log.v(LOG_TAG, " inside onStart");

        super.onStart();
        getMovies();
    }

    public void refetchMovies() {
        String sortBy = helper.getSortCriteria(getActivity());
        FetchMovieTask fetchMoviesTask = new FetchMovieTask();
        fetchMoviesTask.execute(sortBy);
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        final String apiKey = "";
        final String API_KEY_PARAM = "api_key";
        final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        final String MOVIE_BASE_URL_FAVORITE = "http://api.themoviedb.org/3/movie?";
        final String SORT_BY_PARAM = "sort_by";

        protected ArrayList<Movie> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            String movieJsonStr;
            String sortBy = params[0];
            ArrayList<Movie> movies = null;


            if (getString(R.string.pref_sortby_favorites).equals(params[0])) {

                movies = new ArrayList<>();
                Set<String> movieids = helper.getFavorites(getActivity());
                if (movieids == null || movieids.isEmpty()) {
                    return movies;
                }

                for (String movieId : movieids) {
                    Uri builtUriMovie = Uri.parse(MOVIE_BASE_URL_FAVORITE).buildUpon()
                            .appendPath(movieId)
                            .appendQueryParameter(API_KEY_PARAM, apiKey)
                            .build();

                    movieJsonStr = helper.getJsonString(builtUriMovie);

                    try {
                        movies.add(helper.getMovieFromJson(movieJsonStr));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {

                Uri builtUriMovie = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sortBy)
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();

                movieJsonStr = helper.getJsonString(builtUriMovie);

                try {
                    movies = helper.getMoviesDataFromJson(movieJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG,"Exception in doInBackground()-FetchMovieTask", e);
                }
            }
            return movies;
        }


        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            ArrayList<Movie> movies = new ArrayList<Movie>();

            if (result != null) {
                for (Movie movie : result) {
                    movies.add(movie);
                }
                imageAdapter.setMovie(movies);
            }

            imageAdapter.notifyDataSetChanged();

            // Solved by creating boolean mTwoPane
            if (mTwoPane && movies != null && !movies.isEmpty() && mPosition > 0 ){
                ((Callback) getActivity()).onItemSelected(movies.get(mPosition));

            } else if (mTwoPane && movies != null && !movies.isEmpty()) {
                mPosition = 0;
                ((Callback) getActivity()).onItemSelected(movies.get(mPosition));
            } else if (mTwoPane) {
                // Tablet special case, when only 1 favorite being displayed,
                // is unfavorited, the detail fragment is not cleared
                ((Callback) getActivity()).onItemSelected(null);
            }
            gridView.smoothScrollToPosition(mPosition);

        }
    }

    class ImageAdapter extends ArrayAdapter<Movie> {
        private Context context;
        List<Movie> movies = new ArrayList<Movie>();

        public ImageAdapter(Context context, int layoutResourceId, ArrayList<Movie> movies) {
            super(context, 0, movies);
            this.context = context;
            this.movies = movies;
        }

        public void setMovie(ArrayList<Movie> movie) {
            movies.clear();
            this.movies = movie;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

       @Override
        public Movie getItem(int position) {
            return movies.get(position);
        }

        @Override
        public int getCount() {
            if (movies != null)
                return movies.size();
            else
                return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setPadding(0, 0, 0, 0);

            } else {
                imageView = (ImageView) convertView;
            }
            if (movies != null) {
                Picasso.with(context)
                        .load(movies.get(position).getMovieUrl())
                        .into(imageView);
                imageView.setAdjustViewBounds(true);
            }
            return imageView;
        }
    }

    public interface Callback {
         void onItemSelected(Movie movie);
    }

    public void setIfTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }


}