package app.java.sentiextract.data.repository

import app.java.sentiextract.data.api.ApiHelper
import app.java.sentiextract.data.model.ApiRequest

class SentimentRepo(private val apiHelper: ApiHelper) {
    suspend fun getUsers(request : ApiRequest) = apiHelper.getUsers(request)
}