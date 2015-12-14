package com.example.app.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Ranjith on 9/10/15.
 */
public class Movie  implements Parcelable{

    private String id;
    private String movieUrl ="";
    private String title = "";
    private String synopsis="";
    private String rating;
    private String releaseDate;
    private boolean favorite;
    private ArrayList<Trailer> trailers;
    private ArrayList<Review> reviews;

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }
    public ArrayList<Trailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(ArrayList<Trailer> trailers) {
        this.trailers = trailers;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMovieUrl() {
        return movieUrl;
    }

    public void setMovieUrl(String movieUrl) {
        this.movieUrl = movieUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            Movie mMovie = new Movie();
            mMovie.movieUrl = source.readString();
            mMovie.title = source.readString();
            mMovie.synopsis = source.readString();
            mMovie.rating = source.readString();
            mMovie.releaseDate = source.readString();
            mMovie.id = source.readString();
           // mMovie.favorite = source.readByte() != 0;
            mMovie.trailers = source.readArrayList(Trailer.class.getClassLoader());
            mMovie.reviews = source.readArrayList(Review.class.getClassLoader());

            return mMovie;
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieUrl);
        dest.writeString(title);
        dest.writeString(synopsis);
        dest.writeString(rating);
        dest.writeString(releaseDate);
      //  dest.writeByte((byte) (favorite ? 1 : 0));
        dest.writeString(id);
        dest.writeList(trailers);
        dest.writeList(reviews);

    }
}
