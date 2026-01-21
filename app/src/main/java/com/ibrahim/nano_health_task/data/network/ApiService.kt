package com.ibrahim.nano_health_task.data.network

import com.ibrahim.nano_health_task.data.models.PostsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/m1/1181613-1175965-default/getHomeFeeds")
    suspend fun getHomeFeeds(@Query("page") page: Int = 0): PostsResponse
}
