package com.example.app.popularmoviesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MovieActivity extends AppCompatActivity implements MovieFragment.Callback {

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private MovieAppHelper helper = new MovieAppHelper();
    private String mSortCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

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
