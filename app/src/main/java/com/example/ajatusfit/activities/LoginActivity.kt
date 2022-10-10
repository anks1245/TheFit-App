package com.example.ajatusfit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ajatusfit.R
import com.example.ajatusfit.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val root = binding.root
//        setContentView(R.layout.activity_login)
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }
        setContentView(root)
    }
}