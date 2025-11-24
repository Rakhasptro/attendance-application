package com.rakha.hadirapp.data.network

import com.rakha.hadirapp.data.network.dto.LoginRequest
import com.rakha.hadirapp.data.network.dto.LoginResponse
import com.rakha.hadirapp.data.network.dto.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

class AuthApi(private val client: HttpClient, private val baseUrl: String = "http://10.0.2.2:3000/api/") {

    suspend fun login(email: String, password: String): LoginResponse {
        val url = "${baseUrl}auth/login"
        val request = LoginRequest(email = email, password = password)

        val response = client.post(url) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            setBody(request)
        }
        return response.body()
    }

    suspend fun register(email: String, password: String): LoginResponse {
        val url = "${baseUrl}auth/register"
        val request = RegisterRequest(email = email, password = password)

        val response = client.post(url) {
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
            setBody(request)
        }
        return response.body()
    }
}
