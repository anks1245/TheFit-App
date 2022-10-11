package com.example.ajatusfit.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.ajatusfit.R
import com.example.ajatusfit.databinding.ActivityMainBinding
import com.example.ajatusfit.services.MyServices
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE=1
    private val REQUEST_CODE_PERMISSIONS = 101
    private lateinit var fitnessOptions:FitnessOptions
    private lateinit var googleSignInAccount: GoogleSignInAccount
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        //date
        val week = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.DAY_OF_WEEK)
        val date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.DAY_OF_MONTH)
        val day = getDays(week)

//        val profilePic = binding.profileImg as ImageView

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)!!

        Glide.with(this).load(googleSignInAccount.photoUrl.toString()).placeholder(R.drawable.ic_baseline_person_pin_24).into(binding.profileImg)

//        Toast.makeText(this, googleSignInAccount.photoUrl.toString(), Toast.LENGTH_SHORT).show()

        binding.date.text = "$day, $date"
        //date-end
        //navigation-start
        binding.profileImg.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }
        binding.viewAll.setOnClickListener{
            startActivity(Intent(this,AllRanksActivity::class.java))
        }
        //navigation-end
        startService(Intent(applicationContext,MyServices::class.java))
        checkGooglePermission()
//        var inc = 1;

//        Handler().postDelayed({
//                              GlobalScope.launch {
//                                    fetchDataHelper()
//                              }
//                              accessGoogleFit()
//            Toast.makeText(this, inc.toString(), Toast.LENGTH_SHORT).show()
//            inc++
//        },10000)
        //doSomething
        CoroutineScope(Dispatchers.Default).launch {
            while (isActive){
                fetchDataHelper()
                delay(3000)
            }
        }
        setContentView(root)
    }

    fun checkGooglePermission(){
        //fitnessApis-Start
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account, fitnessOptions)
        } else {
            checkActivityRecognition()
            checkLocationPermission()
            subscribeFitness()
            activeSubscription()
            accessGoogleFit()
        }
        //fitnessApi-End
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                    checkActivityRecognition()
                    checkLocationPermission()
                    subscribeFitness()
                    activeSubscription()
                    accessGoogleFit()
                }
                else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                // Permission not granted
            }
        }
    }

    private fun checkActivityRecognition() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),REQUEST_CODE_PERMISSIONS)
        }else{
            // permission granted
        }
    }
    private fun checkBodySensors() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS),REQUEST_CODE_PERMISSIONS)
        }else{
            // permission granted
        }
    }

    private fun checkLocationPermission(){
        val foreground = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (foreground) {
            val background = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (background) {
                handleLocationUpdates()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_PERMISSIONS)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ), REQUEST_CODE_PERMISSIONS
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            var foreground = false
            var background = false
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.ACCESS_COARSE_LOCATION) {
                    //foreground permission allowed
                    if (grantResults[i] >= 0) {
                        foreground = true
                        Toast.makeText(applicationContext, "Foreground location permission allowed", Toast.LENGTH_SHORT).show()
                        continue
                    } else { Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
                        break
                    }
                }
                if (permissions[i] == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
                    if (grantResults[i] >= 0) {
                        foreground = true
                        background = true
                        Toast.makeText(
                            this,
                            "Background location location permission allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                        continue
                    } else {
                        Toast.makeText(this, "Background location location permission denied", Toast.LENGTH_SHORT).show()
                        break
                    }
                }
                //continue
                if(permissions[i] == Manifest.permission.ACTIVITY_RECOGNITION){
                    if(grantResults[i]>=0){
                        Toast.makeText(this, "Activity Recognition permission granted", Toast.LENGTH_SHORT).show()
                        checkBodySensors()
                    }else{
                        Toast.makeText(this, "Activity Recognition permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
                if(permissions[i] == Manifest.permission.BODY_SENSORS){
                    if(grantResults[i]>=0){
                        Toast.makeText(this, "Body Sensors permission granted", Toast.LENGTH_SHORT).show()
                        checkLocationPermission()
                    }else{
                        Toast.makeText(this, "Body Sensors permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (foreground) {
                if (background) {
                    handleLocationUpdates()
                } else {
                    handleForegroundLocationUpdates()
                }
            }
        }
    }

    private fun handleLocationUpdates() {
        //foreground and background
        Toast.makeText(
            applicationContext,
            "Start Foreground and Background Location Updates",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleForegroundLocationUpdates() {
        //handleForeground Location Updates
        Toast.makeText(applicationContext, "Start foreground location updates", Toast.LENGTH_SHORT)
            .show()
    }


    private fun accessGoogleFit() {
            val end = LocalDateTime.now()
            val start = end.minusDays(1)
            val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
            val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

            val readRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build()
            val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
            Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener { response ->
//                    Toast.makeText(this, "Fitness API", Toast.LENGTH_SHORT).show()
                    Log.i("TAG", "OnSuccess: ${response.buckets}")
                    for (dataSet in response.buckets.flatMap { it.dataSets }) {
                        dumpDataSet(dataSet)
                        Log.i("TAG", "OnSuccess: ${dataSet}")
                    }
                }
                .addOnFailureListener { e -> Log.d("TAG", "OnFailure()", e) }


    }

    private fun getTodaysData() {
        Fitness.getHistoryClient(this,googleSignInAccount).readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
//                binding.stepsCount.setText()
                Log.d("StepsHistory",it.toString())
            }

    }

    private fun dumpDataSet(dataSet: DataSet?) {
            Log.i(TAG, "Data returned for Data type: ${dataSet!!.dataType.name}")
            var steps = ""
            for (dp in dataSet.dataPoints) {
                Log.i(TAG,"Data point:")
                Log.i(TAG,"\tType: ${dp.dataType.name}")
                Log.i(TAG,"\tStart: ${dp.getStartTimeString()}")
                Log.i(TAG,"\tEnd: ${dp.getEndTimeString()}")
                for (field in dp.dataType.fields) {
                    Log.i("DATA_FIELD","\tField: ${field.name.toString()} Value: ${dp.getValue(field)}")
                    if(field.name.toString()=="steps")
                        steps = dp.getValue(field).toString()

                }
            }
        binding.stepsCount.text = steps
        binding.rankSteps.text = steps
    }

    fun DataPoint.getStartTimeString() = Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()


    fun DataPoint.getEndTimeString() = Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    private fun subscribeFitness(){
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.i("SUBSCRIPTION", "STEPS:Successfully subscribed!")
            }
            .addOnFailureListener { e ->
                Log.w("SUBSCRIPTION", "STEPS:There was a problem subscribing.", e)
            }
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .subscribe(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener {
                Log.i("SUBSCRIPTION", "CALORIES:Successfully subscribed!")
            }
            .addOnFailureListener { e ->
                Log.w("SUBSCRIPTION", "CALORIES:There was a problem subscribing.", e)
            }
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .subscribe(DataType.TYPE_HEART_POINTS)
            .addOnSuccessListener {
                Log.i("SUBSCRIPTION", "HEART POINTS:Successfully subscribed!")
            }
            .addOnFailureListener { e ->
                Log.w("SUBSCRIPTION", "HEART POINTS:There was a problem subscribing.", e)
            }
    }
    private fun activeSubscription(){
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .listSubscriptions()
            .addOnSuccessListener { subscriptions ->
                Log.i("Active", subscriptions.size.toString())
                for (sc in subscriptions) {
                    val dt = sc.dataType
                    Log.i("Active", "Active subscription for data type: ${dt?.name}")
                }
            }
    }

    private suspend fun fetchDataHelper(){
        withContext(Dispatchers.Main){
            accessGoogleFit()
        }
    }

    private fun getDays(day_of_week: Int): String {
        return when(day_of_week){
            1->{
                "Sun"
            }
            2->{
                "Mon"
            }
            3->{
                "Tue"
            }
            4->{
                "Wed"
            }
            5-> {
                "Thu"
            }
            6->{
                "Fri"
            }
            7->{
                "Sat"
            }
            else->{
                "..."
            }
        }
    }
    companion object{
        val TAG = "FIELDS"
    }

}