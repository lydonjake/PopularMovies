package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieOverviewFragment extends Fragment
{
    private MovieData[] movieData;

    private int movieDataLength;

    private ImageAdapter movieAdapter;

    public MovieOverviewFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_movie_overview_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        final String LOG_TAG = MovieOverviewFragment.class.getSimpleName();

        try
        {
            updateMovies();
        }
        catch (ExecutionException e)
        {
            Log.e(LOG_TAG, "Error ", e);
        }
        catch (InterruptedException e)
        {
            Log.e(LOG_TAG, "Error ", e);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        final String LOG_TAG = MovieOverviewFragment.class.getSimpleName();

        try
        {
            updateMovies();
        }
        catch (ExecutionException e)
        {
            Log.e(LOG_TAG, "Error ", e);
        }
        catch (InterruptedException e)
        {
            Log.e(LOG_TAG, "Error ", e);
        }

    }

    public class ImageAdapter extends BaseAdapter
    {
        private Context mContext;

        public ImageAdapter(Context c)
        {
            mContext = c;
        }

        public int getCount()
        {
            return movieDataLength;
        }

        public Object getItem(int position)
        {
            return null;
        }

        public long getItemId(int position)
        {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso.with(mContext).load(movieData[position].getPoster()).into(imageView);

            return imageView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_overview, container, false);

        movieAdapter = new ImageAdapter(getActivity());

        final GridView gridView = (GridView) rootView.findViewById(R.id.gridView);

        gridView.setAdapter(movieAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
            {
                MovieData movie = movieData[pos];

                Bundle bundle = new Bundle();
                bundle.putParcelable("movieData", movie);

                Intent detailIntent = new Intent(view.getContext(),MovieDetailActivity.class);
                detailIntent.putExtras(bundle);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    private void updateMovies() throws ExecutionException, InterruptedException
    {
        movieData = new FetchMovieDataTask().execute().get();
        movieDataLength = movieData.length;
    }

    public class FetchMovieDataTask extends AsyncTask<String,Void,MovieData[]>
    {
        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        private MovieData[] movieDataArray;

        @Override
        protected MovieData[] doInBackground(String... params)
        {
            String sortType = getString(R.string.pref_sort_key);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String tmdbJsonStr;

            try
            {
                Uri.Builder builder = new Uri.Builder();

                builder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("discover")
                        .appendPath("movie")
                        .appendQueryParameter("certification_country", "US")
                        .appendQueryParameter("sort_by", sortType)
                        .appendQueryParameter("api_key", getString(R.string.api_key));
                URL url = new URL(builder.build().toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                tmdbJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            Log.v("JSON String", tmdbJsonStr);

            try
            {
                movieDataArray = getMovieDataFromJson(tmdbJsonStr);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                movieDataArray = null;
            }

//            for (int i = 0; i < movieDataArray.length; i++)
//            {
//                Log.v("Movie Title", movieDataArray[i].getTitle());
//            }

            return movieDataArray;
        }

        private MovieData[] getMovieDataFromJson(String tmdbJsonStr) throws JSONException
        {
            final String TMDB_PAGE = "page";
            final String TMDB_RESULTS = "results";
            final String TMDB_BACKGROUND = "backdrop_path";
            final String TMDB_POSTER = "poster_path";
            final String TMDB_TITLE = "original_title";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_DATE = "release_date";
            final String TMDB_POPULARITY = "popularity";
            final String TMDB_USER_RATING = "vote_average";

            JSONObject tmdbJson = new JSONObject(tmdbJsonStr);
            JSONArray jsonArray = tmdbJson.getJSONArray(TMDB_RESULTS);

            MovieData[] movies = new MovieData[jsonArray.length()];

            for(int i = 0; i < jsonArray.length(); i++)
            {
                movies[i] = new MovieData();

                movies[i].setBackground(jsonArray.getJSONObject(i).getString(TMDB_BACKGROUND));
                movies[i].setTitle(jsonArray.getJSONObject(i).getString(TMDB_TITLE));
                movies[i].setPlotOverview(jsonArray.getJSONObject(i).getString(TMDB_OVERVIEW));
                movies[i].setReleaseDate(jsonArray.getJSONObject(i).getString(TMDB_DATE));
                movies[i].setPoster(jsonArray.getJSONObject(i).getString(TMDB_POSTER));
                movies[i].setPopularity(jsonArray.getJSONObject(i).getString(TMDB_POPULARITY));
                movies[i].setUserRating(jsonArray.getJSONObject(i).getString(TMDB_USER_RATING));

            }

            return movies;
        }

        @Override
        protected void onPostExecute(MovieData[] array)
        {
            movieData = array;
        }

    }

}
