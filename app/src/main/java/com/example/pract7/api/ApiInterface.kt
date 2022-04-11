package com.example.pract7.api

import com.example.pract7.BuildConfig
import com.example.pract7.model.RestaurantData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET(".")
    suspend fun getRestaurants(
        @Query("text") text: String,
        @Query("type") type: String,
        @Query("lang") lang: String,
        @Query("apikey") apikey: String,
        @Query("results") results: Int
    ) : Response<RestaurantData>
}