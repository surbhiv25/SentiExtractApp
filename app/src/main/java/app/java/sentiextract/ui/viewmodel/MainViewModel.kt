package app.java.sentiextract.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import app.java.sentiextract.data.model.ApiRequest
import app.java.sentiextract.data.repository.SentimentRepo
import app.java.sentiextract.utils.Resource
import kotlinx.coroutines.Dispatchers

class MainViewModel(private val mainRepository: SentimentRepo) : ViewModel() {

    fun getUsers(request : ApiRequest) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getUsers(request)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}