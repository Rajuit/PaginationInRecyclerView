package com.queerdevs.raj.paginationdemoinrecyclerview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.queerdevs.raj.paginationdemoinrecyclerview.api.MovieApi;
import com.queerdevs.raj.paginationdemoinrecyclerview.api.MovieService;
import com.queerdevs.raj.paginationdemoinrecyclerview.models.Result;
import com.queerdevs.raj.paginationdemoinrecyclerview.models.TopRatedMovies;
import com.queerdevs.raj.paginationdemoinrecyclerview.utils.PaginationScrollListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "MainActivity";
    Toolbar toolbar;
    PaginationAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    List<Result> results = new ArrayList<>();
    RecyclerView rv;
    ProgressBar progressBar;
    public String searched_word = "";
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this time, since total pages in actual API is very large. Feel free to modify.
    private int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;

    private MovieService movieService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        rv = (RecyclerView) findViewById(R.id.main_recycler);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);

        adapter = new PaginationAdapter(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);

        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (searched_word.equals("") || searched_word == "") {
                            loadNextPage("jack");
                        } else {
                            loadNextPage(searched_word);
                        }
                    }
                }, 1000);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        //init service and load data
        movieService = MovieApi.getClient().create(MovieService.class);

        if (searched_word.equals("") || searched_word == "") {
            loadFirstPage("jack");
        } else {
            loadFirstPage(searched_word);
        }
    }


    public void loadFirstPage(String search_query) {
        Log.d(TAG, "loadFirstPage: ");

        callSearchedMoviesApi(search_query).enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                // Got data. Send it to adapter

                //  List<Result> results = fetchResults(response);
                results = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                adapter.addAll(results);
                adapter.notifyDataSetChanged();

                if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    /**
     * @param response extracts List<{@link Result>} from response
     * @return
     */
    public List<Result> fetchResults(Response<TopRatedMovies> response) {
        TopRatedMovies topRatedMovies = response.body();
        return topRatedMovies.getResults();
    }

    private void loadNextPage(String search_key) {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callSearchedMoviesApi(search_key).enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                // List<Result> results = fetchResults(response);
                results = fetchResults(response);
                adapter.addAll(results);
                adapter.notifyDataSetChanged();

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    /**
     * Performs a Retrofit call to the top rated movies API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */


    private Call<TopRatedMovies> callSearchedMoviesApi(String key) {
        return movieService.getSearchedMovies(
                getString(R.string.my_api_key),
                key,
                currentPage
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener((SearchView.OnQueryTextListener) MainActivity.this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {

        if (query.toString().trim().length() >= 1) {
            loadFirstPage(query);
            Log.d("COMING", query);
            searched_word = query;
        } else {
            searched_word = "jack";
            loadFirstPage(searched_word);
        }

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


}
