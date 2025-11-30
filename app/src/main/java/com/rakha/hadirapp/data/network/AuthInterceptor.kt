package com.rakha.hadirapp.data.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = com.rakha.hadirapp.data.store.TokenHolder.token

        return if (!token.isNullOrBlank()) {
            val newReq = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newReq)
        } else {
            chain.proceed(original)
        }
    }
}

