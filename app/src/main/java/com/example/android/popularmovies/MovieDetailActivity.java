package com.example.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * MovieDataActivity.java
 * Created by Jake Lydon on 9/28/2015.
 *
 * Purpose: Movie detail page activity and fragment.
 */
public class MovieDetailActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /**
     * Movie detail fragment display movie data on background image
     */
    public static class DetailFragment extends Fragment
    {
        private MovieData movieData;

        public DetailFragment() {
            setHasOptionsMenu(false);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

            //gets the MovieData object from intent
            Bundle bundle = getActivity().getIntent().getExtras();

            if (bundle != null)
            {
                movieData = bundle.getParcelable("movieData");
            }

            //loads background image
            ImageView backgroundImage = (ImageView) rootView.findViewById(R.id.detailBackground);
            Picasso.with(getActivity()).load(movieData.getBackground()).into(backgroundImage);

            //loads poster image
            ImageView posterImage = (ImageView) rootView.findViewById(R.id.moviePosterThumbnail);
            Picasso.with(getActivity()).load(movieData.getPoster()).into(posterImage);

            //loads movie title
            ((TextView) rootView.findViewById(R.id.movieTitle))
                    .setText(movieData.getTitle());

            //loads movie user rating
            ((TextView) rootView.findViewById(R.id.movieVoterRating))
                    .setText(movieData.getUserRating());

            //loads movie release date
            ((TextView) rootView.findViewById(R.id.movieDate))
                    .setText(movieData.getReleaseDate());

            //loads movie overview
            ((TextView) rootView.findViewById(R.id.movieSynopsis))
                    .setText(movieData.getPlotOverview());

            return rootView;
        }
    }

}