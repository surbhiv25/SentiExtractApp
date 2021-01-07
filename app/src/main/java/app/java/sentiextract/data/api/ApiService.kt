package app.java.sentiextract.data.api

import app.java.sentiextract.data.model.ApiRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("sentiment")
    suspend fun getUsers(@Body request : ApiRequest): String
}