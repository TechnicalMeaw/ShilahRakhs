package com.coprotect.myapplication.firebaseClasses

class CommentItem(val postId: String,
                  val commentText: String,
                  val likeCount: Long,
                  val timeStamp: Long) {
    constructor() : this("", "", 0, -1)
}