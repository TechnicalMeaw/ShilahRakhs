package com.coprotect.myapplication.constants

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DatabaseLocations {
    companion object{
        private const val userLocation = "/user"
        private const val PostLocation = "/allPost"
        private const val userPostLocation = "/userPost"
        private const val commentLocation = "/comments"
        private const val followingLocation = "/following"
        private const val followersLocation = "/followers"

        fun getUserReference(userId: String): DatabaseReference{
            return FirebaseDatabase.getInstance("https://shilah-raksh-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("$userLocation/$userId/")
        }
        fun getAllPostReference(): DatabaseReference {
            return FirebaseDatabase.getInstance().getReference("$PostLocation/")
        }
        fun getSpecificPostReference(postId: String): DatabaseReference {
            return FirebaseDatabase.getInstance().getReference("$PostLocation/$postId/")
        }
        fun getUserPostReference(userId: String): DatabaseReference{
            return FirebaseDatabase.getInstance().getReference("$userPostLocation/$userId/")
        }
        fun getCommentReference(postId: String): DatabaseReference {
            return FirebaseDatabase.getInstance().getReference("$commentLocation/$postId/")
        }
        fun getFollowingReference(userId: String): DatabaseReference{
            return FirebaseDatabase.getInstance().getReference("$followingLocation/$userId/")
        }
        fun getFollowersReference(userId: String): DatabaseReference{
            return FirebaseDatabase.getInstance().getReference("$followersLocation/$userId/")
        }
    }
}