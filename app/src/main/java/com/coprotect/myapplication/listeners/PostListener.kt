package com.coprotect.myapplication.listeners

import com.coprotect.myapplication.firebaseClasses.PostItem

interface PostListener {
    fun onLikeButtonClicked(currentPost: PostItem)
    fun onCommentButtonClicked(currentPost: PostItem)
    fun onClickedProfile(currentPost: PostItem)
    fun onShareButtonClicked(currentPost: PostItem)
    fun onPostDoubleClicked(currentPost: PostItem)
}