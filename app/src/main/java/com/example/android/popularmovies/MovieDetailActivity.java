package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
//        return false;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings)
//        {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

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

            Intent intent = getActivity().getIntent();

            Bundle bundle = intent.getExtras();

            if (bundle != null)
            {
                movieData = bundle.getParcelable("movieData");
            }

            ImageView backgroundImage = (ImageView) rootView.findViewById(R.id.detailBackground);

            Picasso.with(getActivity()).load(movieData.getBackground()).into(backgroundImage);

            ImageView posterImage = (ImageView) rootView.findViewById(R.id.moviePosterThumbnail);

            Picasso.with(getActivity()).load(movieData.getPoster()).into(posterImage);

            ((TextView) rootView.findViewById(R.id.movieTitle))
                    .setText(movieData.getTitle());
            ((TextView) rootView.findViewById(R.id.movieVoterRating))
                    .setText(movieData.getUserRating());
            ((TextView) rootView.findViewById(R.id.movieDate))
                    .setText(movieData.getReleaseDate());
            ((TextView) rootView.findViewById(R.id.movieSynopsis))
                    .setText(movieData.getPlotOverview());

            return rootView;
        }
    }

}