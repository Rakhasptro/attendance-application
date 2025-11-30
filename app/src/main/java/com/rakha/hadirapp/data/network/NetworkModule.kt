package com.rakha.hadirapp.data.network

import com.google.gson.GsonBuilder
import com.rakha.hadirapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    // Use BuildConfig.BASE_URL provided by Gradle buildConfigField
    private val BASE_URL: String = BuildConfig.BASE_URL

    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .build()
    }

    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun provideAuthApi(): AuthApi = provideRetrofit(provideOkHttpClient()).create(AuthApi::class.java)
}
