package com.coprotect.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getAllPostReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getFollowingReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getPostLikeReference
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.databinding.FragmentHomeBinding
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.postTransactions.PostTasks.Companion.addLike
import com.coprotect.myapplication.postTransactions.PostTasks.Companion.removeLike
import com.coprotect.myapplication.recyclerViewAdapters.PostListener
import com.coprotect.myapplication.recyclerViewAdapters.PostRVAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), PostListener {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var adapter : PostRVAdapter
    var userIdToBeFetched = ""
    var postPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Retrieve the value
        try{
            userIdToBeFetched = requireArguments().getString(IntentStrings.userId).toString()
            postPosition = requireArguments().getInt(IntentStrings.postPosition)
        }catch (e: Exception){
            userIdToBeFetched = ""
        }

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root


        /**
         * Initialize RecyclerView Adapter
         */
        adapter = PostRVAdapter(this.requireContext(), this)
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.homeRecyclerView.adapter = this.adapter

//        // Thin Line Between Posts
//        binding.homeRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        /**
         * Handle Clicks of
         * Add Post Button
         */
        binding.newPostButton.setOnClickListener {
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
        binding.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.newPostButton.hide() else if (dy < 0) binding.newPostButton.show()
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
                        if (userIdToBeFetched != ""){
                            if (post.userId == userIdToBeFetched){
                                postMap[snapshot.key.toString()] = post
                                adapter.updatePosts(postMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
                            }
                        } else{
                            checkFollowing(snapshot.key.toString(), post)
                        }

                        /**
                         * Scroll to the specific post
                         */
                        if (postMap.size -1 == postPosition){
                            Handler().postDelayed(
                                Runnable {
                                    binding.homeRecyclerView.smoothScrollToPosition(postPosition)
                                }
                                , 100)
                        }
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

    private fun checkFollowing(postKey: String, post: PostItem) {
        getFollowingReference(FirebaseAuth.getInstance().uid.toString()).child(post.userId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    postMap[postKey] = post
                    adapter.updatePosts(postMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    override fun onLikeButtonClicked(currentPost: PostItem) {
        Log.d("HomeFragment", "Clicked on Like")
        val ref = getPostLikeReference(currentPost.postId)
        Thread{
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        if (snapshot.hasChild(FirebaseAuth.getInstance().uid.toString())){
                            snapshot.child(FirebaseAuth.getInstance().uid.toString()).ref.removeValue()
                            removeLike(currentPost.postId)
                        }else{
                            ref.child(FirebaseAuth.getInstance().uid.toString()).setValue(System.currentTimeMillis())
                            addLike(currentPost.postId)
                        }
                    }else{
                        ref.child(FirebaseAuth.getInstance().uid.toString()).setValue(System.currentTimeMillis())
                        addLike(currentPost.postId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }.start()
    }

    override fun onPostDoubleClicked(currentPost: PostItem) {
        Log.d("HomeFragment", "Double Clicked on Post")
        val ref = getPostLikeReference(currentPost.postId)
        Thread{
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        if (!snapshot.hasChild(FirebaseAuth.getInstance().uid.toString())){
                            ref.child(FirebaseAuth.getInstance().uid.toString()).setValue(System.currentTimeMillis())
                            addLike(currentPost.postId)
                        }
                    }else{
                        ref.child(FirebaseAuth.getInstance().uid.toString()).setValue(System.currentTimeMillis())
                        addLike(currentPost.postId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }.start()
    }

    override fun onCommentButtonClicked(currentPost: PostItem) {
        val commentFragment = CommentsFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, FirebaseAuth.getInstance().uid.toString())
        args.putString(IntentStrings.postId, currentPost.postId)
        commentFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addToBackStack("comments")
            .replace(R.id.fragmentContainerView, commentFragment)
            .commit()
    }

    override fun onClickedProfile(currentPost: PostItem) {
        val profileFragment = ProfileFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, currentPost.userId)
        profileFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addToBackStack("userProfile")
            .replace(R.id.fragmentContainerView, profileFragment)
            .commit()
    }

    override fun onShareButtonClicked(currentPost: PostItem) {
        Glide.with(this)
            .asBitmap()
            .load(currentPost.postMediaUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    shareImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // this is called when imageView is cleared on lifecycle call or for
                    // some other reason.
                    // if you are referencing the bitmap somewhere else too other than this imageView
                    // clear it here as you can no longer have the bitmap
                }
            })

    }


    fun shareImage(bitmap: Bitmap){
                // save bitmap to cache directory
        try {
            val cachePath: File = File(this.requireContext().cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val imagePath: File = File(this.requireContext().cacheDir, "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri =
            FileProvider.getUriForFile(this.requireContext(), "com.coprotect.myapplication.fileprovider", newFile)

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
        shareIntent.setDataAndType(contentUri, requireContext().contentResolver.getType(contentUri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)

        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey check out this awesome image I found on Shilah Raksh app")

        startActivity(Intent.createChooser(shareIntent, "Share image via..."))
    }

}