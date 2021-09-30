package com.coprotect.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import com.coprotect.myapplication.R
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getAllPostReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getPostLikeReference
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.postTransactions.PostTasks.Companion.addLike
import com.coprotect.myapplication.postTransactions.PostTasks.Companion.removeLike
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), PostListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var adapter : PostRVAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val newPostBtn = rootView.findViewById<FloatingActionButton>(R.id.newPostButton)
        val homeRecyclerView = rootView.findViewById<RecyclerView>(R.id.homeRecyclerView)

        /**
         * Initialize RecyclerView Adapter
         */
        adapter = PostRVAdapter(this.requireContext(), this)
        homeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        homeRecyclerView.adapter = this.adapter

        /**
         * Handle Clicks of
         * Add Post Button
         */
        newPostBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack("Profile")
                .replace(R.id.fragmentContainerView, NewPostFragment())
                .commit()
        }

        /**
         * Handle New Post Button Visibility
         * With Scrolling
          */
        homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) newPostBtn.hide() else if (dy < 0) newPostBtn.show()
            }
        })

        /**
         * Fetch Posts
          */
        fetchPosts()

        return rootView
    }

    val postMap: HashMap<String, PostItem> = HashMap()

    private fun fetchPosts(){
        getAllPostReference().addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val post = snapshot.getValue(PostItem::class.java)
                    if (post!= null){
                        postMap[snapshot.key.toString()] = post
                        adapter.updatePosts(postMap.values.toList())
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val post = snapshot.getValue(PostItem::class.java)
                    if (post!= null){
                        postMap[snapshot.key.toString()] = post
                        adapter.updatePosts(postMap.values.toList())
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                postMap.remove(snapshot.key)
                adapter.updatePosts(postMap.values.toList())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onLikeButtonClicked(currentPost: PostItem) {
        Log.d("HomeFragment", "Clicked on Like")

        val ref = getPostLikeReference(currentPost.postId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild(FirebaseAuth.getInstance().uid.toString())){
                        snapshot.child(FirebaseAuth.getInstance().uid.toString()).ref.removeValue()
                        removeLike(currentPost.postId)
                    }else{
                        ref.child(FirebaseAuth.getInstance().uid.toString()).setValue("liked")
                        addLike(currentPost.postId)
                    }
                }else{
                    ref.child(FirebaseAuth.getInstance().uid.toString()).setValue("liked")
                    addLike(currentPost.postId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}