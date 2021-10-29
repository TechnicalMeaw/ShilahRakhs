package com.coprotect.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getFollowersReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getFollowingReference
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.databinding.FragmentFollowListBinding
import com.coprotect.myapplication.databinding.FragmentProfileBinding
import com.coprotect.myapplication.firebaseClasses.FollowingItem
import com.coprotect.myapplication.listeners.FollowItemListener
import com.coprotect.myapplication.recyclerViewAdapters.FollowRVAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

/**
 * A simple [Fragment] subclass.
 * Use the [FollowListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowListFragment : Fragment(), FollowItemListener {

    private lateinit var binding : FragmentFollowListBinding
    private lateinit var adapter: FollowRVAdapter
    private lateinit var profileUserId: String
    private lateinit var followRefType: String
    val followMap = HashMap<String, FollowingItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFollowListBinding.inflate(inflater, container, false)

        //Retrieve the value
        try{
            profileUserId = requireArguments().getString(IntentStrings.userId).toString()
            followRefType = requireArguments().getString(IntentStrings.followType).toString()
        }catch (e: Exception){
            profileUserId = FirebaseAuth.getInstance().uid.toString()
        }

        //Initialize the recyclerView
        adapter = FollowRVAdapter(this.requireContext(), this)
        binding.followRecyclerView.adapter = this.adapter
        binding.followRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        // Thin Line Between Posts
        binding.followRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        fetchFollowItems(profileUserId, followRefType)

        return binding.root
    }

    lateinit var ref: DatabaseReference
    private fun fetchFollowItems(id: String, type: String){
        if (type == "followers"){
            ref = getFollowersReference(id)
        }else{
            ref = getFollowingReference(id)
        }

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val item = snapshot.getValue(FollowingItem::class.java)
                    if (item != null){
                        followMap[snapshot.key.toString()] = item
                        adapter.updateFollowUsers(followMap.values.toList())
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    val item = snapshot.getValue(FollowingItem::class.java)
                    if (item != null){
                        followMap[snapshot.key.toString()] = item
                        adapter.updateFollowUsers(followMap.values.toList())
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (followMap.containsKey(snapshot.key.toString())){
                    followMap.remove(snapshot.key.toString())
                    adapter.updateFollowUsers(followMap.values.toList())
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


    override fun onFollowUserClicked(currentFollowItem: FollowingItem) {
        val profileFragment = ProfileFragment()
        val args = Bundle()
        args.putString(IntentStrings.userId, currentFollowItem.userId)
        profileFragment.arguments = args

        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addToBackStack("userProfile")
            .replace(R.id.fragmentContainerView, profileFragment)
            .commit()
    }

}