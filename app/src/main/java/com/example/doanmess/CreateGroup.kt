package com.example.doanmess

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateGroup : AppCompatActivity() {
    private lateinit var addRv: RecyclerView
    private lateinit var addedRv: RecyclerView
    private lateinit var adapterAdd: GroupAddAdapter
    private lateinit var adapterAdded: GroupAddedAdapter
    lateinit var filterSearch : EditText
    lateinit var searchBtn : ImageButton
    private var originAdd =  mutableListOf<GroupAdd>()
    private var add = mutableListOf<GroupAdd>()
    private val added = mutableListOf<GroupAdded>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        enableEdgeToEdge()

        originAdd.add(GroupAdd("1","conchocuaduynhan", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("2","conglonogcuaduylannnnnn", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("3","Group 3", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("4","Group 4", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("5","Group 5", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("6","Group 1", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("7","Group 2", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("8","Group 3", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("9","Group 4", R.drawable.avatar_placeholder_allchat, false))
        originAdd.add(GroupAdd("10","Group 5", R.drawable.avatar_placeholder_allchat, false))
        add = originAdd.toMutableList()

        addRv = findViewById(R.id.rvAdd)
        addedRv = findViewById(R.id.rvAdded)

        adapterAdd = GroupAddAdapter(add) { id, name ->
            added.add(GroupAdded(name, id))
            adapterAdded.notifyItemInserted(added.size - 1)
        }

        adapterAdded = GroupAddedAdapter(added) { id ->
            originAdd[originAdd.indexOfFirst { it.id == id }].added = false
            val positon = add.indexOfFirst { it.id == id }
            if(positon != -1){
                add[positon].added = false
                adapterAdd.notifyItemChanged(positon)
            }
            added.removeAt(added.indexOfFirst { it.id == id })

        }

        addRv.adapter = adapterAdd
        addRv.layoutManager= LinearLayoutManager(this)

        addedRv.adapter = adapterAdded
        addedRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        filterSearch = findViewById(R.id.filter_search)
        searchBtn = findViewById(R.id.search_btn)
        searchBtn.setOnClickListener({
            if(filterSearch.text.isEmpty()) {
                add= originAdd.toMutableList()
            }
            else{
                val filterLowerCase = filterSearch.text.toString().toLowerCase()
                add= originAdd.filter { it.name.toLowerCase().contains(filterLowerCase) }.toMutableList()
            }
            adapterAdd.changeList(add)

        })
    }

/*    private fun updateSpaceViewVisibility() {
        val spaceView = findViewById<View>(R.id.spaceView)
        spaceView.visibility = if (added.isEmpty()) View.GONE else View.VISIBLE
    }*/
}