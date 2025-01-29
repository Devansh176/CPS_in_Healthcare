package com.example.fitnesstracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var heartRateUpdateJob: Job? = null
    private var trackingNumber = 0
    
    private val _heartRateData = MutableStateFlow<List<TrackingData>>(emptyList())
    private val heartRateData: StateFlow<List<TrackingData>> = _heartRateData
    
    private val _statusMessage = MutableStateFlow<String>("Initializing...")
    private val statusMessage: StateFlow<String> = _statusMessage

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var googleFitLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupPermissionLaunchers()
        
        setContent {
            FitnessTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentStatus by statusMessage.collectAsState()
                    val currentData by heartRateData.collectAsState()
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentStatus,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        LazyColumn {
                            items(currentData) { data ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text("Heart Rate: ${data.heartRate} BPM")
                                        Text("Time: ${data.startTime}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        checkAndRequestPermissions()
    }

    private fun setupPermissionLaunchers() {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Log.d(TAG, "All permissions granted")
                _statusMessage.value = "Permissions granted, checking Google Fit..."
                checkGoogleFitPermissions()
            } else {
                Log.e(TAG, "Some permissions were denied")
                _statusMessage.value = "Error: Required permissions were denied"
            }
        }

        googleFitLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Google Fit permissions granted")
                lifecycleScope.launch {
                    subscribeToHeartRateData()
                }
            } else {
                Log.e(TAG, "Google Fit permissions denied")
                _statusMessage.value = "Error: Google Fit permissions denied"
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            Log.d(TAG, "All permissions already granted")
            checkGoogleFitPermissions()
        }
    }

    private fun checkGoogleFitPermissions() {
        val fitnessOptions = getFitnessOptions()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        
        if (account == null) {
            _statusMessage.value = "Error: Not signed in to Google account"
            try {
                val signInIntent = GoogleSignIn.getClient(this, 
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
                ).signInIntent
                googleFitLauncher.launch(signInIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Google Sign-In", e)
                _statusMessage.value = "Error: Failed to start Google Sign-In"
            }
            return
        }

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            try {
                GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting Google Fit permissions", e)
                _statusMessage.value = "Error: Failed to request Google Fit permissions"
            }
        } else {
            lifecycleScope.launch {
                subscribeToHeartRateData()
            }
        }
    }

    private suspend fun subscribeToHeartRateData() {
        try {
            _statusMessage.value = "Starting heart rate recording..."
            val account = GoogleSignIn.getLastSignedInAccount(this)
                ?: throw Exception("Not signed in to Google account")

            // First, check if Google Fit is available
            val fitnessAppIntent = packageManager.getLaunchIntentForPackage("com.google.android.apps.fitness")
            if (fitnessAppIntent == null) {
                throw Exception("Google Fit app is not installed")
            }

            // Subscribe to heart rate data
            Tasks.await(
                Fitness.getRecordingClient(this, account)
                    .subscribe(DataType.TYPE_HEART_RATE_BPM)
            )

            Log.i(TAG, "Successfully subscribed to heart rate data!")
            _statusMessage.value = "Subscribed to heart rate updates"
            startHeartRateUpdates()

        } catch (e: Exception) {
            Log.e(TAG, "Error in subscribeToHeartRateData", e)
            _statusMessage.value = "Error: ${e.message}"
        }
    }

    private fun startHeartRateUpdates() {
        heartRateUpdateJob?.cancel()
        heartRateUpdateJob = scope.launch {
            try {
                while (isActive) {
                    readLatestHeartRateData()
                    delay(10000) // Check every 10 seconds
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in heart rate updates loop", e)
                _statusMessage.value = "Error in updates: ${e.message}"
            }
        }
    }

    private suspend fun readLatestHeartRateData() {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(this)
                ?: throw Exception("Not signed in to Google account")

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.MINUTES.toMillis(1) // Last minute

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(this, account)
                    .readData(readRequest)
            )

            var foundData = false
            response.dataSets.forEach { dataSet ->
                dataSet.dataPoints.forEach { dataPoint ->
                    foundData = true
                    trackingNumber++
                    val heartRate = dataPoint.getValue(Field.FIELD_BPM)
                    
                    val trackingData = TrackingData(
                        trackingNumber = trackingNumber,
                        startTime = formatTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS)),
                        endTime = formatTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS)),
                        heartRate = heartRate.asFloat().toString()
                    )
                    
                    val currentList = _heartRateData.value.toMutableList()
                    currentList.add(0, trackingData)
                    if (currentList.size > 10) {
                        currentList.removeAt(currentList.size - 1)
                    }
                    _heartRateData.value = currentList
                }
            }

            if (foundData) {
                _statusMessage.value = "Last update: ${formatTime(System.currentTimeMillis())}"
            } else {
                _statusMessage.value = "No heart rate data found. Please check:\n" +
                    "1. Is your watch connected?\n" +
                    "2. Is it measuring heart rate?\n" +
                    "3. Is Google Fit installed and set up?"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error reading heart rate data", e)
            _statusMessage.value = "Error reading data: ${e.message}"
        }
    }

    private fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    private fun getFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
            .build()
    }

    override fun onResume() {
        super.onResume()
        if (::googleFitLauncher.isInitialized && GoogleSignIn.getLastSignedInAccount(this) != null) {
            lifecycleScope.launch {
                subscribeToHeartRateData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heartRateUpdateJob?.cancel()
        scope.cancel()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001
    }
}

data class TrackingData(
    val trackingNumber: Int,
    val startTime: String,
    val endTime: String,
    val heartRate: String
)
