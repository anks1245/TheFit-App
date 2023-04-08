package com.example.ajatusfit.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajatusfit.R
import com.example.ajatusfit.adapters.AllRanksAdapter
import com.example.ajatusfit.dataClass.UserActivityDataModel
import com.example.ajatusfit.dataClass.UsersActivityData
import com.example.ajatusfit.databinding.ActivityAllRanksBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllRanksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAllRanksBinding
    private lateinit var allRanksAdapter: AllRanksAdapter
    private var allRanksArrayList: ArrayList<UserActivityDataModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllRanksBinding.inflate(layoutInflater)
        val root = binding.root

        setSupportActionBar(binding.topAppToolbar)
        supportActionBar?.title = "All Ranks"
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.topAppToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24_dark)

        val arrayList: ArrayList<UsersActivityData> = ArrayList()

        val db = FirebaseDatabase.getInstance()
        val ref = db.getReference("users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children
                arrayList.clear()
                allRanksArrayList.clear()
                children.forEach {
                    val data = it.getValue(UsersActivityData::class.java)
                    arrayList.add(data!!)
//                    Log.d("RTD", "onSuccess:${ it.value }")
                }
                val sortedList = arrayList.sortedWith(compareBy { it.steps }).reversed()
                Log.d("RTD", "onSuccess:${ sortedList.reversed() }")

                sortedList.reversed()
                for(i in sortedList.indices){
                    val userActivityDataModel = UserActivityDataModel(i+1,sortedList[i].name,sortedList[i].steps,sortedList[i].timestamp,"rank",-1)
                    allRanksArrayList.add(userActivityDataModel)
                }
                allRanksAdapter = AllRanksAdapter(allRanksArrayList)
                binding.allranksRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@AllRanksActivity,LinearLayoutManager.VERTICAL,false)
                    adapter = allRanksAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RTD",error.message.toString())
            }

        })

        setContentView(root)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}