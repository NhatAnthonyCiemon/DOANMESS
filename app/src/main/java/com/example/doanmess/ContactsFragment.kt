package com.example.doanmess

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ContactsFragment : Fragment() {

    private var list: MutableList<Contact> = mutableListOf()
    lateinit var recyclerView: RecyclerView

    lateinit var searchBtn : ImageButton
    lateinit var searchFilter: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = mutableListOf(
            Contact(R.drawable.avatar_placeholder_allchat, "conchocuaduynhan", true),
            Contact(R.drawable.avatar_placeholder_allchat, "conglongcuaduylan", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn C", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn D", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn E", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn F", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn G", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn H", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn I", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn J", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn K", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn L", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn M", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn N", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn O", true),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view:View = inflater.inflate(R.layout.fragment_contacts, container, false)
        recyclerView=view.findViewById(R.id.recyclerViewContact)
        val adapter = ContactsAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        searchBtn = view.findViewById(R.id.search_btn)
        searchFilter = view.findViewById(R.id.filter_search)
        searchBtn.setOnClickListener({
            var filter = searchFilter.text.toString()
            if(filter.isEmpty()){
                adapter.changeList(list)

            }
            else{
                val filterLowerCase = filter.toLowerCase()
                val filteredList = list.filter { it.name.toLowerCase().contains(filterLowerCase) }
                adapter.changeList(filteredList)
            }

        })
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ContactsFragment().apply {
            }
    }
}