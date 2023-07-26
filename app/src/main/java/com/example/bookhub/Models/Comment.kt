package com.example.bookhub.Models

data class Comment(
    var comment: String? = null,
    var usid: String? = null,
    var time: Long? = null,
    var commentid: String? = null,
    var postid: String? = null,
)