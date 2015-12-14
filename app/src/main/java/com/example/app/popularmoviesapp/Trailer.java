package com.example.app.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ranjith on 10/8/15.
 */
public class Trailer implements Parcelable {
    private String key ;
    private String name ;
    private String source ;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public static final Parcelable.Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel source) {
            Trailer mTrailer = new Trailer();
            mTrailer.key = source.readString();
            mTrailer.name = source.readString();
            mTrailer.source = source.readString();

            return mTrailer;
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(source);
    }

}
