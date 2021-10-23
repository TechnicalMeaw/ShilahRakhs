package com.coprotect.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.databinding.FragmentExploreBinding
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.recyclerViewAdapters.ExploreRVAdapter
import com.coprotect.myapplication.recyclerViewAdapters.ImageExploreListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

/**
 * A simple [Fragment] subclass.
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExploreFragment : Fragment(), ImageExploreListener {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var adapter: ExploreRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        val rootView = binding.root

        /**
         * Initialize RecyclerView Adapter
         */
        adapter = ExploreRVAdapter(this.requireContext(), this)
        binding.exploreRecyclerView.setHasFixedSize(true)
        binding.exploreRecyclerView.layoutManager = GridLayoutManager(this.requireContext(),2,GridLayoutManager.VERTICAL,false)
        binding.exploreRecyclerView.adapter = this.adapter

        /**
         * Fetch All Posts
         */
        Thread{
            fetchAllPosts()
        }.start()

        return rootView
    }

    val postMap: HashMap<String, PostItem> = HashMap()
    private fun fetchAllPosts(){
        DatabaseLocations.getAllPostReference().addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val post = snapshot.getValue(PostItem::class.java)
                    if (post!= null && post.userId != FirebaseAuth.getInstance().uid.toString()){
                        postMap[snapshot.key.toString()] = post
                        adapter.updatePosts(postMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val post = snapshot.getValue(PostItem::class.java)
                    if (post!= null){
                        if (postMap.containsKey(snapshot.key.toString())){
                            postMap[snapshot.key.toString()] = post
                            adapter.updatePosts(postMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (postMap.containsKey(snapshot.key.toString())){
                    postMap.remove(snapshot.key.toString())
                    adapter.updatePosts(postMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
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

    override fun onClickedPost(currentPost: PostItem) {
        val profileFragment = ProfileFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, currentPost.userId)
        profileFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, profileFragment)
            .addToBackStack("userProfile")
            .commit()
    }

}