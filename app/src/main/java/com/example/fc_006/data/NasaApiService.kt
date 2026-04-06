package com.example.fc_006.data

import retrofit2.http.GET
import retrofit2.http.Query

interface NasaApiService {
    @GET("neo/rest/v1/neo/browse")
    suspend fun getAsteroids(
        @Query("api_key") apiKey: String
    ): NeoResponse
}
