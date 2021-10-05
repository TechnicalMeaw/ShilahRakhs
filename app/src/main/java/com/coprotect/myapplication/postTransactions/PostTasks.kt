package com.coprotect.myapplication.postTransactions

import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getSpecificPostReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PostTasks {
    companion object{
        fun addLike(postId: String){
            getSpecificPostReference(postId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val like = snapshot.child("likeCount").value as Long
                        snapshot.child("likeCount").ref.setValue(like+1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        fun removeLike(postId: String){
            getSpecificPostReference(postId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val like = snapshot.child("likeCount").value as Long
                        snapshot.child("likeCount").ref.setValue(like-1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        fun addCommentCount(postId: String) {
            getSpecificPostReference(postId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val comment = snapshot.child("commentCount").value as Long
                        snapshot.child("commentCount").ref.setValue(comment+1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        fun removeCommentCount(postId: String) {
            getSpecificPostReference(postId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val comment = snapshot.child("commentCount").value as Long
                        snapshot.child("commentCount").ref.setValue(comment-1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
}