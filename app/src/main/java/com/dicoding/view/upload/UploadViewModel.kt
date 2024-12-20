package com.dicoding.view.upload

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class UploadViewModel(
    private val storyRepository: com.dicoding.data.StoryRepository,
) : ViewModel() {
    private var _image = MutableLiveData<Uri?>()
    val image: MutableLiveData<Uri?> = _image

    fun seImage(uri: Uri?) {
        _image.value = uri
    }

    fun uploadStory(description: String, file: File,lan: Float? = null, lon: Float? = null) = storyRepository.postStory(description, file,lan,lon)
}