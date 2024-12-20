package com.dicoding.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.data.StoryRepository
import com.dicoding.data.remote.response.StoryResponse
import com.dicoding.view.ResultStories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _stories = MutableLiveData<ResultStories<StoryResponse>>()
    val stories: LiveData<ResultStories<StoryResponse>> get() = _stories

    fun getStories(location: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            _stories.postValue( ResultStories.Loading )
            val result = repository.getStories(location)
            _stories.postValue(result)
        }
    }
}