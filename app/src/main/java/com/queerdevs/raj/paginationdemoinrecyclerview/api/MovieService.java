package com.queerdevs.raj.paginationdemoinrecyclerview.api;

import com.queerdevs.raj.paginationdemoinrecyclerview.models.TopRatedMovies;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieService {


    @GET("search/movie")
    Call<TopRatedMovies> getSearchedMovies(
            @Query("api_key") String apiKey,
            @Query("query") String title,
            @Query("page") int pageIndex
    );


}
