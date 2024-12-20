package com.dicoding.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.data.StoryRepository
import com.dicoding.data.UserRepository
import com.dicoding.data.pref.UserModel
import com.dicoding.data.remote.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: UserRepository,
    private val storyRepository: StoryRepository
) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    val getStoriesPager : LiveData<PagingData<ListStoryItem>> =
        storyRepository
        .getStoriesPage().cachedIn(viewModelScope)

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

}

