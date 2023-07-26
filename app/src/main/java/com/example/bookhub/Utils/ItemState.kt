package com.example.bookhub.Utils

import com.example.bookhub.Models.User

sealed class ItemState {
    object Loading : ItemState()
    data class Success(val item: User?) : ItemState()
    data class Error(val errorMessage: String?) : ItemState()
}
sealed class LikeState {
    object Liked : LikeState()
    object Unliked : LikeState()
    data class Error(val errorMessage: String?) : LikeState()
}
sealed class DeleteState {
    object Delete : DeleteState()
    data class Error(val errorMessage: String?) : DeleteState()
}
