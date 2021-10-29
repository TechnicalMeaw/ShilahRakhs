package com.coprotect.myapplication.listeners
import com.coprotect.myapplication.firebaseClasses.CommentItem

interface CommentListener {
    fun onUserClicked(currentComment: CommentItem)
}