package com.example.fc_006.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://api.nasa.gov/"
    const val API_KEY = "14Q4BAzhk4e9nMHW5bPcLaFLD3ShiiOmhAeXwJTB"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: NasaApiService by lazy {
        retrofit.create(NasaApiService::class.java)
    }
}
