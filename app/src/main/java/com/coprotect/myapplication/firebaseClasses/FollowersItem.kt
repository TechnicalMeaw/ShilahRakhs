package com.coprotect.myapplication.firebaseClasses

class FollowersItem(val userId: String, val profilePictureUrl: String, val firstName: String, val lastName: String) {
    constructor() : this("", "", "", "")
}