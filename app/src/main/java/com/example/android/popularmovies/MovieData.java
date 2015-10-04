package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MovieData.java
 * Created by Jake Lydon on 9/28/2015.
 *
 * Purpose: Movie data structure.
 */
public class MovieData implements Parcelable
{
    private String title;
    private String poster;
    private String userRating;
    private String popularity;
    private String plotOverview;
    private String background;
    private String releaseDate;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public MovieData()
    {
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(title);
        out.writeString(poster);
        out.writeString(userRating);
        out.writeString(popularity);
        out.writeString(plotOverview);
        out.writeString(background);
        out.writeString(releaseDate);
    }

    public void readFromParcel(Parcel in)
    {
        title = in.readString();
        poster = in.readString();
        userRating = in.readString();
        popularity = in.readString();
        plotOverview = in.readString();
        background = in.readString();
        releaseDate = in.readString();
    }

    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>()
    {
        public MovieData createFromParcel(Parcel in)
        {
            return new MovieData(in);
        }

        public MovieData[] newArray(int size)
        {
            return new MovieData[size];
        }
    };

    public MovieData(Parcel in)
    {
        readFromParcel(in);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getPoster()
    {
        return "http://image.tmdb.org/t/p/w185" + poster;
    }

    public void setPoster(String poster)
    {
        this.poster = poster;
    }

    public String getUserRating()
    {
        return userRating;
    }

    public void setUserRating(String userRating)
    {
        double rating = Double.parseDouble(userRating);

        this.userRating = "User Rating: " + decimalFormat.format(rating);
    }

    public String getPopularity()
    {
        return popularity;
    }

    public void setPopularity(String popularity)
    {
        this.popularity = popularity;
    }

    public String getPlotOverview()
    {
        return plotOverview;
    }

    public void setPlotOverview(String plotOverview)
    {
        this.plotOverview = plotOverview;
    }

    public String getBackground()
    {
        //if the background image is null, use the poster as background
        if(background.equals("null"))
        {
            background = "http://image.tmdb.org/t/p/w185" + poster;
        }
        else
        {
            background = "http://image.tmdb.org/t/p/w780" + background;
        }

        return background;
    }

    public void setBackground(String background)
    {
        this.background = background;
    }

    public String getReleaseDate()
    {
        String formattedDate = "Date not found";  //if date isn't found, use this

        //to format date. ex: Jan 01, 2015
        DateFormat original = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat formatted = new SimpleDateFormat("MMM dd, yyyy");

        try
        {
            //sets the date to formatted date
            Date date = original.parse(releaseDate);
            formattedDate = formatted.format(date);
        }
        catch (Exception e)
        {
            Log.e("Date Not Found", e.toString());
        }

        return "Released: " + formattedDate;
    }

    public void setReleaseDate(String releaseDate)
    {
        this.releaseDate = releaseDate;
    }
}
