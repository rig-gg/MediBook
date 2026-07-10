package edu.cit.amihan.medibook.core.network

import edu.cit.amihan.medibook.core.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    companion object {
        var onUnauthorized: (() -> Unit)? = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenManager.getToken()

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(newRequest)

        if (response.code == 401) {
            TokenManager.clear()
            onUnauthorized?.invoke()
        }

        return response
    }
}