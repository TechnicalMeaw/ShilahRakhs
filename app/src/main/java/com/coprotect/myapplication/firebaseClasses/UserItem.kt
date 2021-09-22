package com.coprotect.myapplication.firebaseClasses

class UserItem(val firstName: String,
               val lastName: String,
               val userId: String,
               val phoneNumber: String,
               val country: String,
               val profilePictureUrl: String,
               val notificationToken: String,
               val bio: String,
               val createdAccountTime: Long,
               val following: Long,
               val followers: Long) {
    constructor() : this("", "", "", "", "", "", "", "", -1, 0, 0)
}