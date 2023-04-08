package com.example.ajatusfit.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ajatusfit.R
import com.example.ajatusfit.adapters.AllRanksAdapter
import com.example.ajatusfit.dataClass.UserActivityDataModel
import com.example.ajatusfit.dataClass.UsersActivityData
import com.example.ajatusfit.databinding.ActivityMainBinding
import com.example.ajatusfit.services.MyServices
import com.github.mikephil.charting.data.BarEntry
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Comparator
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE=1
    private val REQUEST_CODE_PERMISSIONS = 101
    private lateinit var fitnessOptions:FitnessOptions
    private lateinit var googleSignInAccount: GoogleSignInAccount
    private lateinit var db:FirebaseDatabase
    private lateinit var allRanksAdapter: AllRanksAdapter
    private var allRanksArrayList: ArrayList<UserActivityDataModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root

        db = FirebaseDatabase.getInstance()

//        val profilePic = binding.profileImg as ImageView

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)!!
        binding.userNameTextView.text = googleSignInAccount.displayName.toString()
        Glide.with(this).load(googleSignInAccount.photoUrl.toString()).placeholder(R.drawable.ic_baseline_person_pin_24).into(binding.profileImg)

//        Toast.makeText(this, googleSignInAccount.photoUrl.toString(), Toast.LENGTH_SHORT).show()

        binding.date.text = "${LocalDate.now().dayOfWeek}, ${LocalDate.now().dayOfMonth}"
        //date-end
        //navigation-start
        binding.profileImg.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }

        binding.rank1Card.visibility = View.GONE
        binding.rank2Card.visibility = View.GONE

        binding.viewAll.setOnClickListener{
            startActivity(Intent(this,AllRanksActivity::class.java))
        }
        binding.viewAll2.setOnClickListener {
            startActivity(Intent(this,AllRanksActivity::class.java))
        }
        binding.clicktoChat.setOnClickListener {
            startActivity(Intent(this,ChatActivity::class.java))
        }
        //navigation-end

        Log.i("DATE-DAY",LocalDate.now().dayOfWeek.toString())
        Log.i("DATE-DAY",LocalDate.now().minusDays(5).toString())
        val sunday = LocalDateTime.now().minusDays(5).toString()
        val date = SimpleDateFormat("yyyy-mm-dd").parse(sunday)
        Log.i("DATE-DAY",date.toString())

        checkGooglePermission()

        fetchData()
        //doSomething
        fetchWeeklyData()
        startService(Intent(applicationContext, MyServices::class.java))

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
//                if (permissions[i] == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
//                    if (grantResults[i] >= 0) {
//                        foreground = true
//                        background = true
//                        Toast.makeText(
//                            this,
//                            "Background location location permission allowed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        continue
//                    } else {
//                        Toast.makeText(this, "Background location location permission denied", Toast.LENGTH_SHORT).show()
//                        break
//                    }
//                }
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
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm]")
            val end = LocalDateTime.now()
            val start = LocalDateTime.parse("${ LocalDate.now() }T00:00",formatter)
//            val start = end.minusWeeks(1)
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
//                    Log.i("BUCKET", "OnSuccess: ${response.buckets}")
                    for (dataSet in (response.buckets.flatMap { it.dataSets })) {
                        dumpDataSet(dataSet)
//                        Log.i("TAG", "OnSuccess: ${dataSet}")
                    }
                }
                .addOnFailureListener { e -> Log.d("TAG", "OnFailure()", e) }


    }

    private fun getTodaysData() {
        Fitness.getHistoryClient(this,googleSignInAccount).readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
//                binding.stepsCount.setText()
//                Log.d("StepsHistory",it.toString())
            }

    }

    private fun dumpDataSet(dataSet: DataSet?) {
//            Log.i(TAG, "Data returned for Data type: ${dataSet!!.dataType.name}")
            var steps = "0"
            for (dp in dataSet!!.dataPoints) {
//                Log.i(TAG,"Data point:")
//                Log.i(TAG,"\tType: ${dp.dataType.name}")
//                Log.i(TAG,"\tStart: ${dp.getStartTimeString()}")
//                Log.i(TAG,"\tEnd: ${dp.getEndTimeString()}")
                for (field in dp.dataType.fields) {
//                    Log.i("DATA_FIELD","\tField: ${field.name.toString()} Value: ${dp.getValue(field)}")
                    if(field.name.toString()=="steps")
                        steps = dp.getValue(field).toString()

                }
            }
        binding.rankSteps.text = steps

        updateRealtimeDatabase(steps.toInt())


    }

    private fun updateRealtimeDatabase(steps:Int) {

        val ref = db.getReference("users" )

//        Log.d("REF", LocalDateTime.now().toString())
//        ref.child(googleSignInAccount.id.toString()).child("steps").setValue(steps)
        ref.child(googleSignInAccount.id.toString()).updateChildren(mapOf("steps" to steps,"timestamp" to System.currentTimeMillis().toString()))
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

    fun fetchData(){
        CoroutineScope(Dispatchers.IO).launch {
            while (isActive){
                fetchDataHelper()
                delay(3000)
            }
        }
    }
    private fun fetchWeeklyData() {
        val days = listOf<Int>(1,2,3,4,5,6,7)
        val daywiseData: ArrayList<Int> = ArrayList()
        val entries: ArrayList<BarEntry> = ArrayList()

        val end = LocalDateTime.now()

        Log.i("WEEKLY","today ${LocalDate.now().dayOfWeek.toString()}")

        val day = getDays(LocalDateTime.now().dayOfWeek.toString())

        val start = LocalDateTime.now().minusDays(day)
        Log.i("WEEKLY","1 week before ${start}")
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
                    Log.i("WEEKLY", "OnSuccess: ${response.buckets.size}")

//                var datasetArr:MutableList<DataSet> = mutableListOf<DataSet>()
                var stepsArr = ArrayList<Int>()
                var index = 0;
                for (i in 0 until (response.buckets.size)){
                    Log.i("WEEKLY", "OnSuccess $i: ${response.buckets[i].dataSets[0].dataPoints}")
                    for (f in response.buckets[i].dataSets[0].dataType.fields){
                        if (f.name.toString()=="steps"){

                            if(response.buckets[i].dataSets[0].dataPoints.size == 0){
                                stepsArr.add(0)
                            }else{
                                Log.i("WEEKLY", "have datapoints $i: ${response.buckets[i].dataSets[0].dataPoints[0].getValue(f)}")
                                stepsArr.add(response.buckets[i].dataSets[0].dataPoints[0].getValue(f).asInt())
                            }
                        }
                    }
//                    Log.i("WEEKLY", "OnSuccess $i: ${response.buckets[i].dataSets[0].dataPoints.size}")

                    index++

                }
                Log.i("WEEKLY","total$day ${stepsArr.size}")



            }
            .addOnFailureListener { e -> Log.d("TAG", "OnFailure()", e) }


        days.forEach {

        }
    }

    private suspend fun fetchDataHelper(){
        withContext(Dispatchers.IO){
            accessGoogleFit()
            fetchDataFromRealtimeDatabase()
        }
    }

    private fun fetchDataFromRealtimeDatabase() {
        val ref = db.getReference("users")
        val arrayList: ArrayList<UsersActivityData> = ArrayList()
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("RTD", "onSuccess:${ snapshot.childrenCount }")
                val children = snapshot.children
                arrayList.clear()
                children.forEach {
                    Log.d("RTD_in",it.toString())
                    val data = it.getValue(UsersActivityData::class.java)
                    arrayList.add(data!!)
                    Log.d("RTDS", "onSuccess:${ it.value }")
                }
                allRanksArrayList.clear()
                if(arrayList.size > 0){
                    val sortedList = arrayList.sortedWith(compareBy { it.steps }).reversed()
//                    Log.d("RTD", "onSuccess:${ sortedList[1].id }")
                    if(sortedList[0].id == googleSignInAccount.id){
                        binding.rank1Card.visibility = View.VISIBLE
                        binding.rank2Card.visibility = View.GONE
                    }else{
                        binding.rank2Card.visibility = View.VISIBLE
                        binding.rank1Card.visibility = View.GONE
                        for (i in sortedList.indices){
                            if(sortedList[i].id == googleSignInAccount.id){
                                if(sortedList.size<=2){
                                    allRanksArrayList.clear()
                                    allRanksArrayList.add(UserActivityDataModel(i,sortedList[i-1].name,sortedList[i-1].steps,sortedList[i-1].timestamp,"main",-1))
                                    allRanksArrayList.add(UserActivityDataModel(i+1,sortedList[i].name,sortedList[i].steps,sortedList[i].timestamp,"main",i))
                                }else if(sortedList.size-1 == i){
                                    allRanksArrayList.clear()
                                    allRanksArrayList.add(UserActivityDataModel(i,sortedList[i-2].name,sortedList[i-2].steps,sortedList[i-2].timestamp,"main",-1))
                                    allRanksArrayList.add(UserActivityDataModel(i,sortedList[i-1].name,sortedList[i-1].steps,sortedList[i-1].timestamp,"main",-1))
                                    allRanksArrayList.add(UserActivityDataModel(i+1,sortedList[i].name,sortedList[i].steps,sortedList[i].timestamp,"main",i))
                                }else{
                                    allRanksArrayList.clear()
                                    allRanksArrayList.add(UserActivityDataModel(i,sortedList[i-1].name,sortedList[i-1].steps,sortedList[i-1].timestamp,"main",-1))
                                    allRanksArrayList.add(UserActivityDataModel(i+1,sortedList[i].name,sortedList[i].steps,sortedList[i].timestamp,"main",i))
                                    allRanksArrayList.add(UserActivityDataModel(i+2,sortedList[i+1].name,sortedList[i+1].steps,sortedList[i+1].timestamp,"main",-1))
                                }
                                break
                            }
                        }
                        allRanksAdapter = AllRanksAdapter(allRanksArrayList)
                        binding.rankCardRecyclerView.apply {
                            layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.VERTICAL,false)
                            adapter = allRanksAdapter
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("RTD","onError:${ error.message.toString() }")
            }

        })


    }

    private fun getDays(day_of_week: String): Long {
        return when(day_of_week){
            "SUNDAY"->{
                1
            }
            "MONDAY"->{
                2
            }
            "TUESDAY"->{
                3
            }
            "WEDNESDAY"->{
                4
            }
            "THURSDAY"-> {
                5
            }
            "FRIDAY"->{
                6
            }
            "SATURDAY"->{
                7
            }
            else->{
                -1
            }
        }
    }
    companion object{
        val TAG = "FIELDS"
    }

    override fun onPause() {
        Log.d("STATE","onPauseState")
        fetchData()
        super.onPause()
    }

    override fun onResume() {
        Log.d("STATE","onResumeState")
        super.onResume()
    }

}


