package com.ternaryop.tumblr

import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

class Blog @Throws(JSONException::class) constructor(jsonResponse: JSONObject) : Serializable {

    val name: String = jsonResponse.getString("name")
    val url: String = jsonResponse.getString("url")
    val title: String = jsonResponse.getString("title")
    val isPrimary = jsonResponse.getBoolean("primary")
    val drafts = jsonResponse.getInt("drafts")
    val posts = jsonResponse.getInt("posts")
    val queue = jsonResponse.getInt("queue")
    val totalPosts = jsonResponse.getInt("total_posts")

    val avatar: List<TumblrAltSize>

    init {
        val jsonSizes = jsonResponse.getJSONArray("avatar")
        avatar = (0 until jsonSizes.length()).map { TumblrAltSize(jsonSizes.getJSONObject(it)) }
    }

    fun getAvatarUrlBySize(size: Int): String = getAvatarUrlBySize(name, size)

    override fun toString(): String = name

    companion object {
        private const val serialVersionUID = -7241228948040188270L

        fun getAvatarUrlBySize(baseHost: String, size: Int): String {
            return "https://api.tumblr.com/v2/blog/$baseHost.tumblr.com/avatar/$size"
        }
    }
}
