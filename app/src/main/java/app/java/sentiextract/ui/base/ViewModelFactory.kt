package app.java.sentiextract.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.java.sentiextract.data.api.ApiHelper
import app.java.sentiextract.data.repository.SentimentRepo
import app.java.sentiextract.ui.viewmodel.MainViewModel

class ViewModelFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(SentimentRepo(apiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}