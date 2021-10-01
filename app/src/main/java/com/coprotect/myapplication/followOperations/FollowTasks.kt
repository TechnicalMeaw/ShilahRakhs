package com.coprotect.myapplication.followOperations

import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getFollowersReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getFollowingReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.firebaseClasses.FollowersItem
import com.coprotect.myapplication.firebaseClasses.FollowingItem
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FollowTasks {
    companion object{
        fun addFollowing(userId: String, followingUser: UserItem){
            val followingItem = FollowingItem(followingUser.userId, followingUser.profilePictureUrl, followingUser.firstName, followingUser.lastName)
            getFollowingReference(userId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        if (snapshot.hasChild(followingUser.userId)){
                            snapshot.child(followingUser.userId).ref.removeValue().addOnSuccessListener {
                                removeFollowingCount(userId)
                                removeFollower(followingUser.userId, userId)
                            }

                        }else{
                            snapshot.child(followingUser.userId).ref.setValue(followingItem).addOnSuccessListener {
                                addFollower(followingUser.userId, userId)
                                addFollowingCount(userId)
                            }
                        }
                    }else{
                        snapshot.child(followingUser.userId).ref.setValue(followingItem).addOnSuccessListener {
                            addFollower(followingUser.userId, userId)
                            addFollowingCount(userId)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        private fun addFollower(userId: String, followerUserId: String){
            getUserReference(followerUserId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(UserItem::class.java)
                        if (user!=null){
                            addFollowerToDatabase(userId, user)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        private fun addFollowerToDatabase(userId: String, followerUser: UserItem){
            val followerItem = FollowersItem(followerUser.userId, followerUser.profilePictureUrl, followerUser.firstName, followerUser.lastName)
            getFollowersReference(userId).child(followerUser.userId).setValue(followerItem).addOnSuccessListener {
                addFollowerCount(userId)
            }
        }

        private fun removeFollower(userId: String, followerUserId: String){
            getFollowersReference(userId).child(followerUserId).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        snapshot.ref.removeValue().addOnSuccessListener {
                            removeFollowerCount(userId)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }



        private fun addFollowingCount(userId: String) {
            getUserReference(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val followingCount = snapshot.child("following").value as Long
                        snapshot.child("following").ref.setValue(followingCount+1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        private fun removeFollowingCount(userId: String) {
            getUserReference(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val followingCount = snapshot.child("following").value as Long
                        snapshot.child("following").ref.setValue(followingCount-1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }


        private fun addFollowerCount(userId: String) {
            getUserReference(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val followingCount = snapshot.child("followers").value as Long
                        snapshot.child("followers").ref.setValue(followingCount+1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        private fun removeFollowerCount(userId: String) {
            getUserReference(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val followingCount = snapshot.child("followers").value as Long
                        snapshot.child("followers").ref.setValue(followingCount+1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }
}