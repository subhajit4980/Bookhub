package com.example.bookhub.Models

data class AudioData(
    var title: String? = null,
    var usid: String? = null,
    var decription: String? = null,
    var time: String ?= null,
    var review: String? = null,
    var postid: String? = null,
    val likedUserIds: List<String> = emptyList()
)