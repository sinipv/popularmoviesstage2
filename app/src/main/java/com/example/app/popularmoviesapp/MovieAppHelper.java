package com.example.app.popularmoviesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Ranjith on 9/8/15.
 */
public class MovieAppHelper {
    private final String LOG_TAG = MovieAppHelper.class.getSimpleName();

    final String apiKey = "49d399755ca88ffaa8cc33f029f71c79";
    final String API_KEY_PARAM = "api_key";
    final String API_SELECT_PARAM = "append_to_response";
    final String appendToResponse = "reviews,trailers";
    public static AtomicBoolean favoriteUpdated = new AtomicBoolean(false);

    public String getJsonString(Uri builtUri) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String JsonStr = null;
        URL url = null;
        //Log.v(LOG_TAG, "Built URI getJsonString " + builtUri.toString());

        try {
            url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            JsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            JsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing the stream", e);
                }
            }
        }

        return JsonStr;
    }


    public ArrayList<Movie> getMoviesDataFromJson(String movieJsonStr)
            throws JSONException {

        final String OWN_RESULTS = "results";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(OWN_RESULTS);

        ArrayList<Movie> movieList = new ArrayList<Movie>();
        for (int i = 0; i < movieArray.length(); i++) {
            JSONObject movieItem = movieArray.getJSONObject(i);
            Movie movieObj = getMovieDataFromJson(movieItem);
            movieList.add(movieObj);
        }
        return movieList;
    }

    public Movie getMovieFromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return getMovieDataFromJson(jsonObject);
    }


    @NonNull
    private Movie getMovieDataFromJson(JSONObject movieItem) throws JSONException {
        final String OWN_POSTER_PATH = "poster_path";
        final String OWN_ORIGINAL_TITLE = "original_title";
        final String OWN_SYNOPSIS = "overview";
        final String OWN_RATING = "vote_average";
        final String OWN_RELEASE_DATE = "release_date";
        final String OWN_ID = "id";


        Movie movieObj = new Movie();
        String title;
        String posterPath;
        String synopsis;
        String rating;
        String releaseDate;
        String id;
        ArrayList<Trailer> trailers;
        ArrayList<Review> reviews;
        String trailerJsonStr;

        posterPath = movieItem.getString(OWN_POSTER_PATH);
        title = movieItem.getString(OWN_ORIGINAL_TITLE);
        synopsis = movieItem.getString(OWN_SYNOPSIS);
        rating = movieItem.getString(OWN_RATING);
        releaseDate = movieItem.getString(OWN_RELEASE_DATE);
        id = movieItem.getString(OWN_ID);

        movieObj.setTitle(title);
        movieObj.setSynopsis(synopsis);
        movieObj.setRating(rating);
        movieObj.setReleaseDate(releaseDate);
        movieObj.setId(id);

        trailerJsonStr = getTrailerJsonStr(id);
        if(trailerJsonStr != null){
            trailers = getTrailerDataFromJson(trailerJsonStr);
            movieObj.setTrailers(trailers);
            reviews = getReviewDataFromJson(trailerJsonStr);
            movieObj.setReviews(reviews);
        }

        String imageUrl = getImageUrl(posterPath);
        movieObj.setMovieUrl(imageUrl);
        return movieObj;
    }

    private String getTrailerJsonStr(String id) {
        String trailerJsonStr = null;
        StringBuilder trailerBase = new StringBuilder("http://api.themoviedb.org/3/movie/");

        final String TRAILER_BASE_URL = trailerBase.append(id).append("?").toString();
        Uri builtUriTrailer = Uri.parse(TRAILER_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .appendQueryParameter(API_SELECT_PARAM, appendToResponse)
                .build();
        trailerJsonStr = getJsonString(builtUriTrailer);

        return trailerJsonStr;
    }

    private ArrayList<Trailer> getTrailerDataFromJson(String trailerJsonStr) throws JSONException {
        final String OWN_RESULT = "trailers";
        final String OWN_YOUTUBE = "youtube";
        final String OWN_SOURCE = "source";
        final String OWN_NAME = "name";

        JSONArray trailerArray = null;
        JSONArray reviewArray = null;
        try {
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONObject jb = trailerJson.getJSONObject(OWN_RESULT);
            trailerArray = jb.getJSONArray(OWN_YOUTUBE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<Trailer> trailerList = new ArrayList<Trailer>();
        for (int i = 0; i < trailerArray.length(); i++) {
            Trailer trailerObj = new Trailer();
            String key;
            String name;
            String source;

            JSONObject trailerItem = trailerArray.getJSONObject(i);
            name = trailerItem.getString(OWN_NAME);
            trailerObj.setName(name);

            source = trailerItem.getString(OWN_SOURCE);
            trailerObj.setSource(source);

            trailerList.add(trailerObj);
        }

        return trailerList;
    }

    private ArrayList<Review> getReviewDataFromJson(String trailerJsonStr) throws JSONException {
        final String OWN_REVIEWS = "reviews";
        final String OWN_RESULTS = "results";
        final String OWN_AUTHOR = "author";
        final String OWN_CONTENT = "content";

        JSONArray reviewArray = null;
        try {
            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONObject jb = trailerJson.getJSONObject(OWN_REVIEWS);
            reviewArray = jb.getJSONArray(OWN_RESULTS);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayList<Review> reviewList = new ArrayList<Review>();
        for (int i = 0; i < reviewArray.length(); i++) {
            Review reviewObj = new Review();
            String author;
            String content;

            JSONObject reviewItem = reviewArray.getJSONObject(i);
            author = reviewItem.getString(OWN_AUTHOR);
            reviewObj.setAuthor(author);

            content = reviewItem.getString(OWN_CONTENT);
            reviewObj.setContent(content);
            reviewList.add(reviewObj);
        }

        return reviewList;
    }

    private String getImageUrl(String posterPath) {
        String imageUrl;
        String baseUrl = "http://image.tmdb.org/t/p/";
        String size = "w185";

        imageUrl = baseUrl + size + posterPath;

        return imageUrl;
    }

    public void addToFavorites(Context context, String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.pref_sortby_favorites), new HashSet<String>());
        stringSet.add(movieId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(context.getString(R.string.pref_sortby_favorites), stringSet);
        editor.apply();
    }

    public void removeFromFavorites(Context context, String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.pref_sortby_favorites), new HashSet<String>());
        stringSet.remove(movieId);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(context.getString(R.string.pref_sortby_favorites), stringSet);
        editor.apply();
    }

    public boolean isFavorite(Context context, String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.pref_sortby_favorites), new HashSet<String>());
        return stringSet.contains(movieId);
    }


    public Set<String> getFavorites(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(R.string.pref_sortby_favorites), new HashSet<String>());
    }

    public String getSortCriteria(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sortby_key), context.getString(R.string.pref_sortby_popularity));
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
