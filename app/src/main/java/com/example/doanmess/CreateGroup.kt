package com.example.doanmess

import android.os.Bundle
import android.view.View
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

    private val add = mutableListOf<GroupAdd>()
    private val added = mutableListOf<GroupAdded>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        enableEdgeToEdge()

        add.add(GroupAdd("conchocuaduynhan", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("conglonogcuaduylannnnnn", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 3", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 4", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 5", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 1", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 2", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 3", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 4", R.drawable.avatar_placeholder_allchat, false))
        add.add(GroupAdd("Group 5", R.drawable.avatar_placeholder_allchat, false))
        addRv = findViewById(R.id.rvAdd)
        addedRv = findViewById(R.id.rvAdded)

        adapterAdd = GroupAddAdapter(add) { position ->

            added.add(GroupAdded(add[position].name, position))
            adapterAdded.notifyItemInserted(added.size - 1)

        }

        adapterAdded = GroupAddedAdapter(added) { positon ->
            add[added[positon].positionAdded].added = false
            adapterAdd.notifyItemChanged(added[positon].positionAdded)
            added.removeAt(positon)

        }

        addRv.adapter = adapterAdd
        addRv.layoutManager= LinearLayoutManager(this)

        addedRv.adapter = adapterAdded
        addedRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

/*    private fun updateSpaceViewVisibility() {
        val spaceView = findViewById<View>(R.id.spaceView)
        spaceView.visibility = if (added.isEmpty()) View.GONE else View.VISIBLE
    }*/
}