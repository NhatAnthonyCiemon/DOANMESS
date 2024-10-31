package com.example.doanmess

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ContactsFragment : Fragment() {

    private var list: MutableList<Contact> = mutableListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var searchBtn: ImageButton
    lateinit var searchFilter: EditText
    lateinit var toAddFriendBtn: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var userStatusListener: ValueEventListener
    val firestore = Firebase.firestore
    lateinit var adapter : ContactsAdapter
    val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()

    }
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_contacts, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewContact)
        adapter = ContactsAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        searchBtn = view.findViewById(R.id.search_btn)
        searchFilter = view.findViewById(R.id.filter_search)
        toAddFriendBtn = view.findViewById(R.id.toAddFriendBtn)
        progressBar = view.findViewById(R.id.progressBar)
        toAddFriendBtn.setOnClickListener {
            val intent = Intent(activity, AddFriend::class.java)
            startActivity(intent)
        }



        searchBtn.setOnClickListener {

        }
        searchFilter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filter = s.toString()
                if (filter.isEmpty()) {
                    adapter.changeList(list)
                } else {
                    val filterLowerCase = filter.toLowerCase()
                    val filteredList = list.filter { it.name.toLowerCase().contains(filterLowerCase) }
                    adapter.changeList(filteredList)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        showLoading(true)
        fetchFriendsAndGroups()
        return view
    }

    override fun onResume() {
        super.onResume()
        addRealtimeListeners()
    }
    override fun onPause() {
        super.onPause()
        // Remove the listener to avoid memory leaks
        if (::userStatusListener.isInitialized) {
            list.forEach { contact ->
                val userStatusRef = database.getReference("users/${contact.id}/online")
                userStatusRef.removeEventListener(userStatusListener)
            }
        }
    }

    fun focusSearch() {
        if (::searchFilter.isInitialized) {
            searchFilter.requestFocus()
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchFilter, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContactsFragment().apply {
            }
    }

    // Launch a coroutine to fetch friends and groups


    private fun fetchFriendsAndGroups() {
        val userRef = firestore.collection("users").document(currentUser!!.uid).get().addOnSuccessListener {
            val friends = it.get("Friends") as List<String>
            val groups = it.get("Groups") as List<String>
            val tasks = mutableListOf<Task<DocumentSnapshot>>()
            friends.forEach { friendId ->
                val task = fetchUserDetails(friendId)
                tasks.add(task)
            }
            groups.forEach { groupId ->
                val task = fetchGroupDetails(groupId)
                tasks.add(task)
            }
            Tasks.whenAllComplete(tasks).addOnCompleteListener {
                addRealtimeListeners()
            }
        }

    }

    private fun fetchUserDetails(userId: String) : Task<DocumentSnapshot>{
        val userRef = firestore.collection("users").document(userId).get().addOnSuccessListener {
            val avatarUrl = it.getString("Avatar") ?: ""
            val name = it.getString("Name") ?: ""
            val contact = Contact(userId, avatarUrl, name, false)
            list.add(contact)
            adapter.notifyDataSetChanged()
        }
        return userRef
    }

    private fun fetchGroupDetails(groupId: String): Task<DocumentSnapshot> {
        val groupRef = firestore.collection("groups").document(groupId).get().addOnSuccessListener {
            val avatarUrl = it.getString("Avatar") ?: ""
            val name = it.getString("Name") ?: ""
            val contact = Contact(groupId, avatarUrl, name, false)
            list.add(contact)
            adapter.notifyDataSetChanged()
        }
        return groupRef

    }
    private fun addRealtimeListeners() {
        if (list.isEmpty()) {
            showLoading(false)
            return
        }
        list.forEach { contact ->
            val userStatusRef = database.getReference("users/${contact.id}/online")
            userStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                    if (!isOnline) {
                        // Wait for 5 seconds and check again
                        Handler(Looper.getMainLooper()).postDelayed({
                            userStatusRef.get().addOnSuccessListener { delayedSnapshot ->
                                val delayedIsOnline = delayedSnapshot.getValue(Boolean::class.java) ?: false
                                if (!delayedIsOnline) {
                                    contact.online = false
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }, 2500)
                    } else {
                        contact.online = true
                        adapter?.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            }
            userStatusRef.addValueEventListener(userStatusListener)
        }
        showLoading(false)
    }
}