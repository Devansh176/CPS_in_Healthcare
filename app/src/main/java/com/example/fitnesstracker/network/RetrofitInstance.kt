package com.example.fitnesstracker.network

import android.annotation.SuppressLint
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "http:172.17.128.1:8080"
    @SuppressLint("StaticFieldLeak")
    private lateinit var googleFitService: GoogleFitService

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun initGoogleFitService(context: Context) {
        googleFitService = GoogleFitService(context)
    }

    fun getGoogleFitService(): GoogleFitService {
        if (!::googleFitService.isInitialized) {
            throw IllegalStateException("GoogleFitService not initialized. Call initGoogleFitService first.")
        }
        return googleFitService
    }
}
