package app.java.sentiextract.data.api

import app.java.sentiextract.data.model.ApiRequest

class ApiHelper(private val apiService: ApiService) {
    suspend fun getUsers(request : ApiRequest) = apiService.getUsers(request)
}