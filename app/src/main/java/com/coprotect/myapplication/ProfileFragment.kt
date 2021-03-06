package com.coprotect.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getFollowingReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserPostReference
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.databinding.FragmentProfileBinding
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.coprotect.myapplication.followOperations.FollowTasks.Companion.addFollowing
import com.coprotect.myapplication.recyclerViewAdapters.ActivityPostListener
import com.coprotect.myapplication.recyclerViewAdapters.ActivityPostRVAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment(), ActivityPostListener {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var adapter: ActivityPostRVAdapter
    private lateinit var profileUserId: String
    var user: UserItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val rootView = binding.root

        //Retrieve the value
        try{
            profileUserId = requireArguments().getString(IntentStrings.userId).toString()
        }catch (e: Exception){
            profileUserId = FirebaseAuth.getInstance().uid.toString()
            binding.followButton.text = "Edit Profile"
        }

        /**
         * Initialize the recyclerView
         * With adapter
         */
        adapter = ActivityPostRVAdapter(this.requireContext(), this)
        binding.activityRecyclerView.layoutManager = GridLayoutManager(rootView.context, 3)
        binding.activityRecyclerView.adapter = this.adapter

        /**
         * Fetch User Details
         * And Activities
         * From Database
         */
        Thread{
            fetchUserDetails(profileUserId)
            fetchUserActivities(profileUserId)
            if (profileUserId != FirebaseAuth.getInstance().uid.toString()){
                checkFollowing(FirebaseAuth.getInstance().uid.toString(), profileUserId)
            }
        }.start()

        /**
         * Following Button Clicked
         */
        binding.followButton.setOnClickListener {
            if (user != null){
                if (profileUserId != FirebaseAuth.getInstance().uid.toString()){
                    addFollowing(FirebaseAuth.getInstance().uid.toString(), user!!)
                }else{
                    parentFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .addToBackStack("EditProfile")
                        .replace(R.id.fragmentContainerView, EditProfileFragment())
                        .commit()
                }
            }
        }

        /**
         * Followers and Following Users Count Clicked
         */
        binding.followingCountTextView.setOnClickListener {
            showFollowing()
        }
        binding.followingLabelTextView.setOnClickListener {
            showFollowing()
        }

        binding.followersCountTextView.setOnClickListener {
            showFollowers()
        }
        binding.followersLabelTextView.setOnClickListener {
            showFollowers()
        }

        return rootView
    }

    private fun showFollowing(){
        val followListFragment = FollowListFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, profileUserId)
        args.putString(IntentStrings.followType, "following")
        followListFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addToBackStack("showFollowing")
            .replace(R.id.fragmentContainerView, followListFragment)
            .commit()
    }

    private fun showFollowers(){
        val followListFragment = FollowListFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, profileUserId)
        args.putString(IntentStrings.followType, "followers")
        followListFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addToBackStack("showFollowers")
            .replace(R.id.fragmentContainerView, followListFragment)
            .commit()
    }


    private val activityPostMap = HashMap<String, PostItem>()

    private fun fetchUserDetails(userId: String){
        // Image Loading Config
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)

        // Fetch Details
        val ref = DatabaseLocations.getUserReference(userId)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.value != null) {
                        user = snapshot.getValue(UserItem::class.java)
                        if (user != null){
                            Glide.with(requireContext()).load(user!!.profilePictureUrl).apply(options).into(binding.userDpCircleImageView)
                            binding.userProfileName.text = "${user!!.firstName} ${user!!.lastName}"
                            binding.followersCountTextView.text = user!!.followers.toString()
                            binding.followingCountTextView.text = user!!.following.toString()
                            if (user!!.bio != "" || user!!.website != ""){
                                binding.bioTextView.visibility = View.VISIBLE
                                binding.bioTextView.text = "${user!!.bio}\n\nWebsite: ${user!!.website}"
                            }else{
                                binding.bioTextView.visibility = View.GONE
                            }
                            profileUserId = user!!.userId
                        }

                    }
                }catch (e: Exception){
                    e.stackTrace
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun fetchUserActivities(userId: String){
        getUserPostReference(userId).addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val activityPost = snapshot.getValue(PostItem::class.java)
                    if (activityPost != null){
                        updateActivity(snapshot.key.toString(), activityPost)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val activityPost = snapshot.getValue(PostItem::class.java)
                    if (activityPost != null){
                        updateActivity(snapshot.key.toString(), activityPost)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (activityPostMap.containsKey(snapshot.key)){
                    activityPostMap.remove(snapshot.key.toString())
                    adapter.updateActivityPosts(activityPostMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
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

    private fun updateActivity(key: String, post: PostItem){
        activityPostMap[key] = post
        adapter.updateActivityPosts(activityPostMap.values.sortedBy { it.postTimeInMillis }.reversed().toList())
    }

    private fun checkFollowing(myUserId: String, profileUserId: String) {
        getFollowingReference(myUserId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    if (snapshot.hasChild(profileUserId)){
                        binding.followButton.text = "Following"
                    }else{
                        binding.followButton.text = "Follow"
                    }
                }else{
                    binding.followButton.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }



    override fun onClickedActivityPost(activityPost: PostItem, position: Int) {
        //Put the value
        val homeFragment = HomeFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, profileUserId)
        args.putInt(IntentStrings.postPosition, position)
        homeFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addToBackStack("PostImages")
            .replace(R.id.fragmentContainerView, homeFragment)
            .commit()
    }

}