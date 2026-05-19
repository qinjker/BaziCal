package com.bazical.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.security.MessageDigest
import javax.inject.Inject

class SignatureInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val APP_KEY = "apkey20260519"
        private const val HEADER_APP_KEY = "X-App-Key"
        private const val HEADER_TIMESTAMP = "X-Timestamp"
        private const val HEADER_SIGNATURE = "X-Signature"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 只对 /api/v1/bazi/* 添加签名
        if (!originalRequest.url.encodedPath.contains("/api/v1/bazi")) {
            return chain.proceed(originalRequest)
        }

        val timestamp = System.currentTimeMillis().toString()
        val bodyString = getRequestBodyString(originalRequest)

        val signature = sha256(APP_KEY + timestamp + bodyString)

        val signedRequest = originalRequest.newBuilder()
            .addHeader(HEADER_APP_KEY, APP_KEY)
            .addHeader(HEADER_TIMESTAMP, timestamp)
            .addHeader(HEADER_SIGNATURE, signature)
            .build()

        return chain.proceed(signedRequest)
    }

    private fun getRequestBodyString(request: Request): String {
        return try {
            val body = request.body
            if (body != null) {
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } else {
                "{}"
            }
        } catch (e: Exception) {
            "{}"
        }
    }

    private fun sha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}