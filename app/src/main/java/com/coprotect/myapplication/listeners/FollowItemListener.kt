package com.coprotect.myapplication.listeners

import com.coprotect.myapplication.firebaseClasses.FollowingItem

interface FollowItemListener {
    fun onFollowUserClicked(currentFollowItem: FollowingItem)
}