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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieOverviewFragment extends Fragment
{
    private ArrayList<MovieData> movieData;

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

            Picasso.with(mContext).load(movieData.get(position).getPoster()).into(imageView);

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
                MovieData movie = movieData.get(pos);

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
        movieDataLength = movieData.size();
    }

    public class FetchMovieDataTask extends AsyncTask<String,Void,ArrayList<MovieData>>
    {
        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        private ArrayList<MovieData> movieDataArray;

        @Override
        protected ArrayList<MovieData> doInBackground(String... params)
        {
            final int PAGES_TO_FETCH = 5;

            String sortType = getString(R.string.pref_sort_key);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            //String[] tmdbJsonStr = new String[PAGES_TO_FETCH];
            ArrayList<String> tmdbJsonStr = new ArrayList<>();

            for (int page = 1; page <= PAGES_TO_FETCH; page++)
            {
                try
                {
                    Uri.Builder builder = new Uri.Builder();

                    builder.scheme("http")
                            .authority("api.themoviedb.org")
                            .appendPath("3")
                            .appendPath("discover")
                            .appendPath("movie")
                            .appendQueryParameter("page", "" + page)
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

                    tmdbJsonStr.add(buffer.toString());

                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data,
                    // there's no point in attempting
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


            }

            try
            {
                movieDataArray = getMovieDataFromJson(tmdbJsonStr, PAGES_TO_FETCH);
            }
            catch (JSONException e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                movieDataArray = null;
            }

            return movieDataArray;
        }

        private ArrayList<MovieData> getMovieDataFromJson(ArrayList<String> tmdbJsonStr, int pages)
                throws JSONException
        {
            final String TMDB_RESULTS = "results";
            final String TMDB_BACKGROUND = "backdrop_path";
            final String TMDB_POSTER = "poster_path";
            final String TMDB_TITLE = "original_title";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_DATE = "release_date";
            final String TMDB_POPULARITY = "popularity";
            final String TMDB_USER_RATING = "vote_average";

            final int PAGES_PER = 20;

            ArrayList<MovieData> movies = new ArrayList<>();

            for(int i = 0; i < pages; i++)
            {
                JSONObject tmdbJson = new JSONObject(tmdbJsonStr.get(i));
                JSONArray jsonArray = tmdbJson.getJSONArray(TMDB_RESULTS);

                int rangeLow = i * PAGES_PER;
                int rangeHigh = rangeLow + PAGES_PER;

                int count = 0;

                for(int j = rangeLow; j < rangeHigh; j++)
                {
                    movies.add(new MovieData());

                    movies.get(j).setBackground(jsonArray.getJSONObject(count)
                            .getString(TMDB_BACKGROUND));
                    movies.get(j).setTitle(jsonArray.getJSONObject(count)
                            .getString(TMDB_TITLE));
                    movies.get(j).setPlotOverview(jsonArray.getJSONObject(count)
                            .getString(TMDB_OVERVIEW));
                    movies.get(j).setReleaseDate(jsonArray.getJSONObject(count)
                            .getString(TMDB_DATE));
                    movies.get(j).setPoster(jsonArray.getJSONObject(count)
                            .getString(TMDB_POSTER));
                    movies.get(j).setPopularity(jsonArray.getJSONObject(count)
                            .getString(TMDB_POPULARITY));
                    movies.get(j).setUserRating(jsonArray.getJSONObject(count)
                            .getString(TMDB_USER_RATING));

                    count++;
                }
            }

            return movies;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieData> arrayList)
        {
            movieData = arrayList;
        }

    }

}
