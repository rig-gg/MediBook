package edu.cit.amihan.medibook.core.network

import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.feature.auth.network.AuthApiService
import edu.cit.amihan.medibook.feature.auth.network.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
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

        if (response.code == 401 && originalRequest.header("X-Refresh-Retry") != "true") {
            val refreshToken = TokenManager.getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                response.close()

                val refreshResponse = runBlocking {
                    try {
                        val api = RetrofitClient.authApi
                        api.refreshToken(RefreshTokenRequest(refreshToken))
                    } catch (e: Exception) {
                        null
                    }
                }

                if (refreshResponse != null && refreshResponse.isSuccessful) {
                    val body = refreshResponse.body()
                    if (body?.accessToken != null) {
                        TokenManager.updateAccessToken(body.accessToken)
                        if (body.refreshToken != null) {
                            TokenManager.saveSession(
                                body.accessToken,
                                body.refreshToken,
                                TokenManager.getUserId(),
                                TokenManager.getFullName() ?: "",
                                TokenManager.getRole() ?: ""
                            )
                        }

                        val retryRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer ${body.accessToken}")
                            .addHeader("X-Refresh-Retry", "true")
                            .build()

                        return chain.proceed(retryRequest)
                    }
                }
            }

            TokenManager.clear()
            onUnauthorized?.invoke()
        }

        return response
    }
}
