package com.example.ajatusfit.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.ajatusfit.R
import com.example.ajatusfit.constants.SHARED_PREF
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var logoutBtn: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var accnt: GoogleSignInAccount
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        logoutBtn = findViewById(R.id.logout)
        accnt= GoogleSignIn.getLastSignedInAccount(this)!!
        if(accnt != null){

        }
        sharedPreferences = getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

       val  mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

//        Toast.makeText(this, "", Toast.LENGTH_SHORT).show()

        logoutBtn.setOnClickListener {
            mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this) {
                    Toast.makeText(this, "Revoked All Access", Toast.LENGTH_SHORT).show()
                }
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, OnCompleteListener<Void?> {
                    Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
                })
            editor.clear()
            editor.commit()
            startActivity(Intent(this,LoginActivity::class.java))
            finishAffinity()
        }
    }
}