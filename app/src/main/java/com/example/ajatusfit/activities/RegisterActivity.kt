package com.example.ajatusfit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.ajatusfit.R
import com.example.ajatusfit.dataClass.Users
import com.example.ajatusfit.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var gso:GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var signInAccount: GoogleSignInAccount
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val root = binding.root

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

        signInAccount = GoogleSignIn.getLastSignedInAccount(this)!!

        if(signInAccount!= null){
            binding.name.setText(signInAccount.displayName.toString())
            binding.name.isEnabled = false

            binding.email.setText(signInAccount.email.toString())
            binding.email.isEnabled = false

        }

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
        binding.dateOfBirth.setOnClickListener {
            datePicker.show(supportFragmentManager,"TAG")
            datePicker.addOnPositiveButtonClickListener {
//                Toast.makeText(this@RegisterActivity, datePicker.headerText, Toast.LENGTH_SHORT).show()
                binding.dateOfBirth.setText(datePicker.headerText)
            }
        }

        val genderList = listOf<String>("Male","Female","Others")
        val arrayAdapter = ArrayAdapter(this,R.layout.list_item,genderList)
        (binding.gender as? AutoCompleteTextView)?.setAdapter(arrayAdapter)

        binding.registerBtn.setOnClickListener {
//            if(binding.password.text.toString() != binding.passwordConfirm.text.toString()){
//                Toast.makeText(this, "Password Doesn't Match", Toast.LENGTH_SHORT).show()
//                if(!binding.name.text.isNullOrEmpty() && !binding.email.text.isNullOrEmpty() && !binding.dateOfBirth.text.isNullOrEmpty() &&
//                    !binding.gender.text.isNullOrEmpty() && !binding.password.text.isNullOrEmpty() && !binding.passwordConfirm.text.isNullOrEmpty() ){
                    val db = Firebase.firestore
                    val user = hashMapOf(
                        "name" to binding.name.text.toString(),
                        "email" to binding.email.text.toString(),
                        "password" to binding.password.text.toString(),
                        "gender" to binding.gender.text.toString(),
                        "date_of_birth" to binding.dateOfBirth.text.toString(),
                        "profileImg_url" to signInAccount.photoUrl,
                        "token" to signInAccount.id,
                        "is_complete" to true
                    )

                    db.collection("users").document(binding.email.text.toString()).set(user)
                        .addOnSuccessListener {
                            val database = FirebaseDatabase.getInstance()
                            val ref = database.getReference("users")

                            val user_rtd = Users(id=signInAccount.id.toString(), name = signInAccount.displayName.toString(), steps = 0, timestamp = System.currentTimeMillis().toString())

                            ref.child(signInAccount.id.toString()).setValue(user_rtd)

                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this,MainActivity::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "ERROR: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
        }

        setContentView(root)
    }

    private fun anyActivity(){
        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
//                Toast.makeText(this, "Revoked All Access", Toast.LENGTH_SHORT).show()
            }
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this, OnCompleteListener<Void?> {
//                Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        anyActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        anyActivity()
    }
}