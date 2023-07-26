package com.example.bookhub.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookhub.Models.AudioData
import com.example.bookhub.Models.BookData
import com.example.bookhub.Repositories.Repository
import com.example.bookhub.Utils.DeleteState
import com.example.bookhub.Utils.LikeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val _likeStateLiveData: MutableLiveData<LikeState> = MutableLiveData()
    private val _DeleteStateLiveData: MutableLiveData<DeleteState> = MutableLiveData()
    private val _likeStatusLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _DeleteLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val _likeCount: MutableLiveData<Int> = MutableLiveData()
    private val _getTopAudio: MutableLiveData<ArrayList<AudioData>> = MutableLiveData()
    private val _getTopBook: MutableLiveData<ArrayList<BookData>> = MutableLiveData()

    val likeStateLiveData: LiveData<LikeState> = _likeStateLiveData
    val DeleteStateLiveData: LiveData<DeleteState> = _DeleteStateLiveData
    val likeStatusLiveData: LiveData<Boolean> = _likeStatusLiveData
    val DeleteLiveData: LiveData<Boolean> = _DeleteLiveData
    val likeCount: LiveData<Int> get() = _likeCount
    val getTopAudio: LiveData<ArrayList<AudioData>> get() = _getTopAudio
    val getTopBook: LiveData<ArrayList<BookData>> get() = _getTopBook
    fun DeleteData(
        documentId: String,
        userId: String,
        isDelete: Boolean,
        result: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val isVisible = withContext(Dispatchers.IO) {
                    repository.Delete(documentId, userId, isDelete) {
                        if (isDelete) {
                            result.invoke(it)
                            _DeleteStateLiveData.postValue(DeleteState.Delete)
                        }
                    }
                }
                _DeleteLiveData.postValue(isVisible)
            } catch (e: Exception) {
                _DeleteStateLiveData.postValue(DeleteState.Error(e.message))
            }
        }
    }

    fun checkLikeStatus(documentId: String, userId: String) {
        viewModelScope.launch {
            try {
                val isLiked = withContext(Dispatchers.IO) {
                    repository.isUserLiked(documentId, userId)
                }
                _likeStatusLiveData.postValue(isLiked)
                _likeStateLiveData.postValue(if (isLiked) LikeState.Liked else LikeState.Unliked)
            } catch (e: Exception) {
                _likeStateLiveData.postValue(LikeState.Error(e.message))
            }
        }
    }

    fun like(documentId: String, userId: String, result: (String) -> Unit) {
        viewModelScope.launch {
            repository.addUserLike(documentId, userId) {
                result.invoke(it)
            }
        }
    }

    fun unlike(documentId: String, userId: String, result: (String) -> Unit) {
        viewModelScope.launch {
            repository.removeUserLike(documentId, userId) {
                result.invoke(it)
            }
        }
    }

    fun getTopaudio(result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
             repository.AudioList(){list->
                _getTopAudio.postValue(list)
                }
            } catch (e: Exception) {
                result.invoke(e.message.toString())
            }

        }
    }
    fun getTopbook(result: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
             repository.BookList(){list->
                _getTopBook.postValue(list)
                }
            } catch (e: Exception) {
                result.invoke(e.message.toString())
            }

        }
    }
}