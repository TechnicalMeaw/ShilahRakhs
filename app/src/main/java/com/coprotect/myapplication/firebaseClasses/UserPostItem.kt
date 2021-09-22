package com.coprotect.myapplication.firebaseClasses

class UserPostItem (val postId: String,
                    val postType: String,
                    val postMediaUrl: String,
                    val modifiedInMillis: Long) {
    constructor(): this("", "", "", -1)
}