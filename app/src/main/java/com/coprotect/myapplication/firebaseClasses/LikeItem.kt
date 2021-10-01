package com.coprotect.myapplication.firebaseClasses

class LikeItem (val userId: String, val firstName: String, val lastName: String, val profilePictureUrl: String, val timeStamp: Long) {
    constructor() : this("", "", "", "", -1)
}