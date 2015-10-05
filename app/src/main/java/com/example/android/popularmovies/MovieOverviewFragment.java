package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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

/**
 * MovieOverviewFragment.java
 * Created by Jake Lydon on 9/28/2015.
 *
 * Purpose: Fragment that shows main movie grid.
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

        setRetainInstance(true);  //saves the instance

        updateMovies();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_movie_overview_fragment, menu); // inflate the menu

        //sets up the spinner sort
        MenuItem item = menu.findItem(R.id.menu_spinner);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_sort_list, R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //sort key needed to properly set spinner
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String sortType = sharedPref.getString("pref_sort_key",
                getString(R.string.pref_sort_default));

        int sortSpot = 0;  //spinner defaults to popularity

        //if the sort key is vote_average, spinner should be on vote average
        if(sortType.equals(getString(R.string.pref_sort_setUserRating)))
        {
            sortSpot = 1;
        }

        spinner.setSelection(sortSpot, false);  //sets the spinner to the proper key

        spinner.setOnItemSelectedListener(new SpinnerListener());
    }

    /**
     * Spinner listener class for sort by spinner.
     */
    public class SpinnerListener implements AdapterView.OnItemSelectedListener
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            //if sort by popularity is selected, set sort key to popularity and call update movies.
            if (position == 0)
            {
                String sort = getString(R.string.pref_sort_setPopularity);

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_sort_key", sort);
                editor.apply();

                updateMovies();
            }
            else  //else set sort key to vote_average and call update movies.
            {
                String sort = getString(R.string.pref_sort_setUserRating);

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("pref_sort_key", sort);
                editor.apply();

                updateMovies();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {
        }
    }

    /**
     * Image adapter that handles the movie poster grid images.
     */
    public class ImageAdapter extends BaseAdapter
    {
        private Context mContext;

        public ImageAdapter(Context c)
        {
            mContext = c;
        }

        public int getCount()
        {
            return movieDataLength;  //length of the grid from arraylist length
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

            //loads image into view via picasso, with placeholder if image error occurs
            Picasso.with(mContext).load(movieData.get(position).getPoster())
                    .placeholder(R.drawable.abc_dialog_material_background_light)
                    .error(R.drawable.placeholderimage)
                    .into(imageView);

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

        //click listener to direct click on a poster to detail view
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
            {
                //bundle to send MovieData object parcel to detail activity, via intent
                Bundle bundle = new Bundle();
                bundle.putParcelable("movieData", movieData.get(pos));

                //sends the bundle to the detail activity with intent
                Intent detailIntent = new Intent(view.getContext(), MovieDetailActivity.class);
                detailIntent.putExtras(bundle);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    /**
     * Updates the movies shown in the grid view
     */
    private void updateMovies()
    {
        //check the network connection status
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity()
                .getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //if network is connected, get movie data
        if(networkInfo != null && networkInfo.isConnectedOrConnecting())
        {
            //need sort key to fetch proper movie list
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            String sortType = sharedPref.getString("pref_sort_key",
                    getString(R.string.pref_sort_default));

            new FetchMovieDataTask().execute(sortType); //get the movies

        }
        else  //display toast that there is "No Internet Connection"
        {
            Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchMovieDataTask extends AsyncTask<String,Void,ArrayList<MovieData>>
    {
        private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

        private ArrayList<MovieData> movieDataArray;  //arraylist to return

        @Override
        protected ArrayList<MovieData> doInBackground(String... params)
        {
            final int PAGES_TO_FETCH = 5;  //20 movies per page

            String sortType = params[0];  //sort type set to sort key param

            final String VOTE_COUNT = "20";  //at least 20 votes required to be included in list

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string, within an arraylist.
            ArrayList<String> tmdbJsonStr = new ArrayList<>();

            try
            {
                //for loop needed to load multiple pages
                for (int page = 1; page <= PAGES_TO_FETCH; page++)
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
                            .appendQueryParameter("vote_count.gte", VOTE_COUNT)
                            .appendQueryParameter("api_key", getString(R.string.api_key));
                    URL url = new URL(builder.build().toString());

                    // Create the request to TMDB, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null)
                    {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        buffer.append(line);
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return null;
                    }

                    tmdbJsonStr.add(buffer.toString());  //adds JSON data to next spot in arraylist
                }
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data,
                // there's no point in attempting
                // to parse it.
                return null;
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    } catch (final IOException e)
                    {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try
            {
                //get parsed movie data and assign to arraylist
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

            final int PAGES_PER = 20;  //tmdb returns 20 items per page

            //creates the array list of MovieData objects
            ArrayList<MovieData> movies = new ArrayList<>();

            //for loop to handle multiple pages
            for(int i = 0; i < pages; i++)
            {
                //set up the JSON data to parse
                JSONObject tmdbJson = new JSONObject(tmdbJsonStr.get(i));
                JSONArray jsonArray = tmdbJson.getJSONArray(TMDB_RESULTS);

                //ranges necessary to account for multiple pages
                int rangeLow = i * PAGES_PER;
                int rangeHigh = rangeLow + PAGES_PER;

                int count = 0;  //counter to keep track of spot in arraylist

                //for loop to load all movies in page
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
            movieDataLength = movieData.size();
            movieAdapter.notifyDataSetChanged();
        }
    }
}
