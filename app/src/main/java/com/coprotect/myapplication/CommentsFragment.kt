package com.coprotect.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getCommentReference
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.databinding.FragmentCommentsBinding
import com.coprotect.myapplication.databinding.FragmentProfileBinding
import com.coprotect.myapplication.firebaseClasses.CommentItem
import com.coprotect.myapplication.postTransactions.PostTasks.Companion.addCommentCount
import com.coprotect.myapplication.recyclerViewAdapters.CommentRVAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.*
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 * Use the [CommentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommentsFragment : Fragment() {

    private lateinit var binding : FragmentCommentsBinding
    private lateinit var adapter : CommentRVAdapter
    private lateinit var myUserId : String
    private lateinit var postId : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Retrieve the value
        myUserId = requireArguments().getString(IntentStrings.userId).toString()
        postId = requireArguments().getString(IntentStrings.postId).toString()

        // Inflate the layout for this fragment
        binding = FragmentCommentsBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // Hide Nav Bar
        val navbar = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavBar)
        navbar.visibility = View.GONE

        /**
         * Initialize the comments recyclerView Adapter
         */
        adapter = CommentRVAdapter((this.requireContext()))
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.commentsRecyclerView.adapter = this.adapter

        // Load All Comments
        loadComments(postId)

        /**
         * Post Comment
         */
        binding.commentPostBtn.setOnClickListener {
            if (binding.commentEditText.text.toString().trim() != ""){
                postComment(postId, binding.commentEditText.text.toString().trim())
                binding.commentEditText.text.clear()
            }else{
                Toast.makeText(this.requireContext(), "Comment is empty...", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    private fun postComment(postId: String, commentText: String) {
        val commentId = UUID.randomUUID().toString()
        val commentItem = CommentItem(FirebaseAuth.getInstance().uid.toString(),
            postId,
            commentId,
            commentText,
            0,
            System.currentTimeMillis())

        getCommentReference(postId).child(commentId).setValue(commentItem).addOnSuccessListener {
            addCommentCount(postId)
        }
    }

    val commentMap: HashMap<String, CommentItem> = HashMap()
    private fun loadComments(postId: String) {
        getCommentReference(postId).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val comment = snapshot.getValue(CommentItem::class.java)
                    if (comment != null){
                        commentMap[snapshot.key.toString()] = comment
                        adapter.updateComments(commentMap.values.toList())
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val comment = snapshot.getValue(CommentItem::class.java)
                    if (comment != null){
                        commentMap[snapshot.key.toString()] = comment
                        adapter.updateComments(commentMap.values.toList())
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (commentMap.containsKey(snapshot.key.toString())){
                    commentMap.remove(snapshot.key)
                    adapter.updateComments(commentMap.values.toList())
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Show Nav Bar
        val navbar = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavBar)
        navbar.visibility = View.VISIBLE
    }


}