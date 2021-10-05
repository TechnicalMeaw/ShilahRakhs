package com.coprotect.myapplication.firebaseClasses

class CommentItem(val userId: String,
                  val postId: String,
                  val commentId: String,
                  val commentText: String,
                  val likeCount: Long,
                  val timeStamp: Long) {
    constructor() : this("", "", "", "", 0, -1)
}