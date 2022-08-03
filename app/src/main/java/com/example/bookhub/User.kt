package com.example.bookhub

import com.google.firebase.auth.FirebaseUser

data class User(
    var name:String?=null,
    var Email:String?=null,
    var Password:String?=null,
    var phone:String?=null,
    var DOB:String?=null,
    var bio:String?=null, var review: Double?=null) {
}