package com.coprotect.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coprotect.myapplication.R
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.databinding.FragmentExploreBinding
import com.coprotect.myapplication.databinding.FragmentHomeBinding
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.postTransactions.PostTasks
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
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExploreFragment : Fragment(), PostListener {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var adapter: PostRVAdapter

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
        adapter = PostRVAdapter(this.requireContext(), this)
        binding.exploreRecyclerView.layoutManager = LinearLayoutManager(requireContext())
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

    override fun onLikeButtonClicked(currentPost: PostItem) {
        val ref = DatabaseLocations.getPostLikeReference(currentPost.postId)
        Thread{
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        if (snapshot.hasChild(FirebaseAuth.getInstance().uid.toString())){
                            snapshot.child(FirebaseAuth.getInstance().uid.toString()).ref.removeValue()
                            PostTasks.removeLike(currentPost.postId)
                        }else{
                            ref.child(FirebaseAuth.getInstance().uid.toString()).setValue(System.currentTimeMillis())
                            PostTasks.addLike(currentPost.postId)
                        }
                    }else{
                        ref.child(FirebaseAuth.getInstance().uid.toString()).setValue(System.currentTimeMillis())
                        PostTasks.addLike(currentPost.postId)
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

//         parentFragmentManager.beginTransaction()
//             .replace(R.id.fragmentContainerView, commentFragment)
//             .addToBackStack("comments")
//             .commit()
        
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, commentFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onClickedProfile(currentPost: PostItem) {
        val profileFragment = ProfileFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, currentPost.userId)
        profileFragment.arguments = args

//         parentFragmentManager.beginTransaction()
//             .replace(R.id.fragmentContainerView, profileFragment)
//             .addToBackStack("userProfile")
//             .commit()
        
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, profileFragment)
            .addToBackStack(null)
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
