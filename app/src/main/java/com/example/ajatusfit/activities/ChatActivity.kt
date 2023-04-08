package com.example.ajatusfit.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ajatusfit.R
import com.example.ajatusfit.adapters.MessageAdapter
import com.example.ajatusfit.constants.EMAIL
import com.example.ajatusfit.constants.G_TOKEN
import com.example.ajatusfit.constants.SHARED_PREF
import com.example.ajatusfit.dataClass.MessageRoomModel
import com.example.ajatusfit.databinding.ActivityChatBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Date


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var sharedPreferences: SharedPreferences
    private var messageArrayList: ArrayList<MessageRoomModel> = ArrayList()
    private lateinit var messageAdapter: MessageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        val root = binding.root

        setSupportActionBar(binding.topAppToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Ajatus Chat Room"
        supportActionBar?.subtitle = "members"

        binding.topAppToolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)

        sharedPreferences = getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE)
        db = FirebaseDatabase.getInstance()
        val ref = db.getReference("chat_room")
        ref.keepSynced(true)

        val email = sharedPreferences.getString(EMAIL,"")


        var name = ""
        val token = sharedPreferences.getString(G_TOKEN,"")
        Log.d("CHATS-",token.toString())
        var check = false

        val firestoreDB = Firebase.firestore
        firestoreDB.collection("users").document(email!!)
            .get().addOnSuccessListener{
//                Log.d("CHATS-",it.data?.get("token").toString())
                name = it.data?.get("name").toString()
//                token = it.data?.get("token").toString()
                check =true
            }
            .addOnFailureListener {
                Log.d("CHATS-","onError:${it.message}")
            }

        val linearLayoutManager = LinearLayoutManager(this@ChatActivity,LinearLayoutManager.VERTICAL,false)
        linearLayoutManager.stackFromEnd = true
        binding.allChatsRecyclerView.apply {
            layoutManager = linearLayoutManager
        }


            ref.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("CHATS",snapshot.value.toString())
                    messageArrayList.clear()
                    if(snapshot.childrenCount>0){
                        var children = snapshot.children
                        children.forEach {
//                            Log.d("CHATS","")
                            val data = it.getValue(MessageRoomModel::class.java)
                            messageArrayList.add(data!!)
                            Log.d("CHATS",Date(data.timestamp!!.toLong()).toString())
                        }
                        messageAdapter = MessageAdapter(messageArrayList,token!!)
                        binding.allChatsRecyclerView.adapter = messageAdapter
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("CHATS","onError:${error.message}")
                }

            })

        binding.sendMsg.setOnClickListener {
//            fetchUserData(email.toString())
            if(check){
                val msg = MessageRoomModel(name = name,token=token, message = binding.message.text.toString(),timestamp = System.currentTimeMillis().toString())
                ref.push().setValue(msg)
                binding.message.setText("")
//                messageAdapter.notifyDataSetChanged()
            }else{
                Toast.makeText(this, "Wait for few mins", Toast.LENGTH_SHORT).show()

            }
        }

        setContentView(root)
    }

    private fun fetchUserData(email:String){

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
    inner class ChatViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val name1: TextView = itemView.findViewById(R.id.nameOthers)
        val msg1: TextView = itemView.findViewById(R.id.msgOthers)
        val name2: TextView = itemView.findViewById(R.id.nameOthers)
        val msg2: TextView = itemView.findViewById(R.id.msgMe)
        val chat1: ConstraintLayout = itemView.findViewById(R.id.chatOthers)
        val chat2: ConstraintLayout = itemView.findViewById(R.id.chatMe)
    }
}