package com.murali.worldfind.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            try {
                currentUser.getIdToken(false).await().token
            } catch (e: Exception) {
                null
            }
        }

        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
