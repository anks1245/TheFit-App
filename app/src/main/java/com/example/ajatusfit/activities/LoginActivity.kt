package com.example.ajatusfit.activities

import android.R.attr
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ajatusfit.constants.*
import com.example.ajatusfit.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val REQUEST_CODE_GOOGLE_SIGN_IN = 1
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val root = binding.root

        sharedPreferences = getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        binding.googleLogin.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this,gso)
            signIn(mGoogleSignInClient)
        }

        binding.loginBtn.setOnClickListener {
            mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this) {
                    Toast.makeText(this, "Revoked All Access", Toast.LENGTH_SHORT).show()
                }
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, OnCompleteListener<Void?> {
                    Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
                })
        }
        setContentView(root)
    }

    private fun signIn(gsc: GoogleSignInClient) {
        val intent = gsc.signInIntent
        startActivityIfNeeded(intent,REQUEST_CODE_GOOGLE_SIGN_IN)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
                Log.d("CODE","$RESULT_OK $requestCode")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handlingSignInResult(task)
            }
        }
    }

    private fun handlingSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val credential = task.getResult(ApiException::class.java)
            updateUI(credential)
        }catch (e: ApiException){
            Toast.makeText(this, "Error ${ e.status }", Toast.LENGTH_SHORT).show()
            Log.e("API",e.status.toString())
        }
    }


    private fun updateUI(credential: GoogleSignInAccount) {
        Log.d("Credential",credential.displayName.toString())
        checkGoogleAccount(credential.id.toString(),credential.email.toString(),credential.displayName.toString())
    }

    private fun checkGoogleAccount(token: String,email:String,name:String){
        val db = Firebase.firestore
        val account = db.collection("users").document(email)
        account.get().addOnSuccessListener {
//                Log.d("LOGIN", it.data!!.get("is_complete").toString())
                if(it.data != null){
                    if(it.data!!.get("is_complete") as Boolean){
                        editor.putBoolean(LOGIN,true)
                        editor.putString(EMAIL,email)
                        editor.putString(G_TOKEN,token)
                        editor.commit()
                        Toast.makeText(this,"Logged in Successfully" , Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,MainActivity::class.java))
                        finishAffinity()
                    }else{
                        val user = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "password" to "",
                            "gender" to "",
                            "date_of_birth" to "",
                            "token" to token,
                            "is_complete" to false
                        )

                        db.collection("users").document(email).set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Complete Your Profile First", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this,RegisterActivity::class.java))
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "ERROR: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }else{
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "password" to "",
                        "gender" to "",
                        "date_of_birth" to "",
                        "is_complete" to false
                    )

                    db.collection("users").document(email).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Complete Your Profile First", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,RegisterActivity::class.java))
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "ERROR: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
//                    startActivity(Intent(this,RegisterActivity::class.java))
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "ERROR: $it", Toast.LENGTH_SHORT).show()
                Log.d("LOGIN","FAILED ${it}")
            }
    }
}


