package com.ternaryop.tumblr

import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.set

fun Tumblr.getQueue(tumblrName: String, params: Map<String, String>): List<TumblrPost> {
    val apiUrl = Tumblr.getApiUrl(tumblrName, "/posts/queue")
    val list = mutableListOf<TumblrPost>()

    try {
        val json = consumer.jsonFromGet(apiUrl, params)
        val arr = json.getJSONObject("response").getJSONArray("posts")
        Tumblr.addPostsToList(list, arr)
    } catch (e: JSONException) {
        throw TumblrException(e)
    }

    return list
}

fun Tumblr.schedulePost(tumblrName: String, post: TumblrPost, timestamp: Long): Long {
    try {
        val apiUrl = Tumblr.getApiUrl(tumblrName, "/post/edit")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        val gmtDate = dateFormat.format(Date(timestamp))

        val params = HashMap<String, String>()
        params["id"] = post.postId.toString() + ""
        params["state"] = "queue"
        params["publish_on"] = gmtDate

        if (post is TumblrPhotoPost) {
            params["caption"] = post.caption
        }
        params["tags"] = post.tagsAsString

        return consumer.jsonFromPost(apiUrl, params).getJSONObject("response").getLong("id")
    } catch (e: JSONException) {
        throw TumblrException(e)
    }
}

fun Tumblr.queueAll(tumblrName: String): List<TumblrPost> {
    val list = mutableListOf<TumblrPost>()
    var readCount: Int

    val params = HashMap<String, String>(1)
    do {
        val queue = getQueue(tumblrName, params)
        readCount = queue.size
        list.addAll(queue)
        params["offset"] = list.size.toString()
    } while (readCount == Tumblr.MAX_POST_PER_REQUEST)

    return list
}
