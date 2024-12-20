package com.dicoding.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.data.remote.response.ListStoryItem
import com.dicoding.data.remote.response.StoryResponse
import com.dicoding.data.remote.service.ApiService
import com.dicoding.view.ResultStories
import com.dicoding.data.remote.response.ErrorResponse
import com.dicoding.utils.StoryPagingSource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(private val apiService: ApiService) {

    suspend fun getStories(location: Int): ResultStories<StoryResponse> {
        return try {
            val response = apiService.getStoriesLoc(location)
            if (response.error) {
                ResultStories.Error(response.message)
            } else {
                ResultStories.Success(response)
            }
        } catch (e: Exception) {
            ResultStories.Error("Network error: ${e.message}")
        }
    }

    fun getStoriesPage(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                initialLoadSize = 5,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService)
            }
        ).liveData

    }


    fun postStory(description: String, photoFile: File, lan: Float? = null, lon: Float? = null) =
        liveData {
            emit(ResultStories.Loading)
            val requestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val requestImageFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                photoFile.name,
                requestImageFile
            )

            try {
                val successResponse = apiService.postStories(requestBody, multipartBody, lan, lon)
                emit(ResultStories.Success(successResponse))
            } catch (e: Exception) {
                val errorBody = e.message.toString()
                val errorResponse = ErrorResponse(true, errorBody)
                emit(errorResponse.message?.let { ResultStories.Error(it) })
            }
        }
}