package com.ternaryop.tumblr.android.coil

import android.annotation.SuppressLint
import android.content.Context
import coil.ImageLoader
import coil.util.CoilUtils
import com.ternaryop.tumblr.Tumblr
import com.ternaryop.tumblr.android.TumblrManager
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayOutputStream

object CoilTumblrOAuth {
    @SuppressLint("StaticFieldLeak")
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        val currentInstance = instance

        if (currentInstance != null) {
            return currentInstance
        }

        return synchronized(CoilTumblrOAuth::class.java) {
            var newInstance = instance
            if (newInstance == null) {
                newInstance = create(context)
                instance = newInstance
            }
            newInstance
        }
    }

    private fun create(context: Context) = ImageLoader(context) {
        okHttpClient {
            OkHttpClient.Builder()
                .cache(CoilUtils.createDefaultCache(context))
                .addInterceptor { load(TumblrManager.getInstance(context), it.request()) }
                .build()
        }
    }

    private fun load(tumblr: Tumblr, request: Request): Response {
        val url = request.url.toString()
        val buffer = ByteArrayOutputStream()
        val oauthResponse = tumblr.consumer.getSignedGetResponse(url, null)
        oauthResponse.stream.use { stream ->
            stream.copyTo(buffer)

            return Response.Builder()
                .code(oauthResponse.code)
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url(url).build())
                .message(oauthResponse.message)
                .body(buffer.toByteArray().toResponseBody())
                .build()
        }
    }
}
