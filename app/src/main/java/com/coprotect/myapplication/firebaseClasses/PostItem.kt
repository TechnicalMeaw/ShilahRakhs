package com.coprotect.myapplication.firebaseClasses

class PostItem(val userId: String,
               val postId: String,
               val postType: String,
               val postMediaUrl: String,
               val postCaption: String,
               val likeCount: Long,
               val commentCount: Long,
               val postTimeInMillis: Long) {
    constructor(): this("", "", "", "", "", -1, 0, -1)
}