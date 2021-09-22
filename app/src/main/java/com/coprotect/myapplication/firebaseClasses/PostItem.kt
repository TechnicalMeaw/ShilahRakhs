package com.coprotect.myapplication.firebaseClasses

class PostItem(val userId: String,
               val postId: String,
               val postType: String,
               val postMediaUrl: String,
               val postCaption: String,
               val likeCount: Long,
               val commentCount: String,
               val postTimeInMillis: Long) {
    constructor(): this("", "", "", "", "", -1, "", -1)
}