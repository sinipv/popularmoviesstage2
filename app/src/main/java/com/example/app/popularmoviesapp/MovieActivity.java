package com.example.app.popularmoviesapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MovieActivity extends AppCompatActivity implements MovieFragment.Callback {

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public MovieAppHelper helper = new MovieAppHelper();
    private String mSortCriteria;
    private final String LOG_TAG = MovieActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!helper.isConnected(this))
        {
            Context context = getApplicationContext();
            CharSequence text = "No internet connection";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        Log.v(LOG_TAG, "inside oncreate :savedInstanceState:" + savedInstanceState );

        mSortCriteria = helper.getSortCriteria(this);

        if (findViewById(R.id.fragment_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                DetailFragment fragment = new DetailFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_detail_container, fragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        MovieFragment ff = (MovieFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
        ff.setIfTwoPane(mTwoPane);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Movie movie) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.MOVIE_TAG, movie);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).putExtra(DetailFragment.MOVIE_TAG, movie);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortCriteria = helper.getSortCriteria(this);
        if ((sortCriteria != null && !sortCriteria.equals(mSortCriteria)) ||
                (getString(R.string.pref_sortby_favorites).equals(sortCriteria) && helper.favoriteUpdated.get())) {
            MovieFragment ff = (MovieFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_movie);
            if (ff != null) {
                ff.refetchMovies();
            }
            mSortCriteria = sortCriteria;
        }
    }

}
