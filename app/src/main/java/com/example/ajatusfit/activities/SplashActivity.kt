package com.example.ajatusfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.ajatusfit.R
import com.example.ajatusfit.constants.LOGIN
import com.example.ajatusfit.constants.SHARED_PREF
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SplashActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedPreferences = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val loginCheck = sharedPreferences.getBoolean(LOGIN,false)

//        val db = FirebaseDatabase.getInstance()
//        val myRef: DatabaseReference = db.getReference("message")
//        myRef.setValue("Hello, World!")

        Handler().postDelayed({
            if(loginCheck){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        },3000)
    }
}