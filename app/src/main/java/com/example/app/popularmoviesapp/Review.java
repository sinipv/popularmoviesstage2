package com.example.app.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ranjith on 10/13/15.
 */
public class Review implements Parcelable {
    private String author ;
    private String content ;


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public static final Parcelable.Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel source) {
            Review mReview = new Review();
            mReview.author = source.readString();
            mReview.content = source.readString();

            return mReview;
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
    }
}
