package com.rakha.hadirapp.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

object NetworkModule {
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        engine {
            // configure underlying OkHttpClient timeouts
            config {
                // receiver is OkHttpClient.Builder here
                connectTimeout(15, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
                // you can add more OkHttp configuration here if needed
            }
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY
        }
    }
}
