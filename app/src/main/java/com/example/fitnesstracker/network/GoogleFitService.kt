package com.example.fitnesstracker.network

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleFitService(private val context: Context) {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    suspend fun getGoogleFitData(dataType: DataType, startTime: Long, endTime: Long) = suspendCoroutine { continuation ->
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            continuation.resumeWithException(Exception("Google Fit permissions not granted"))
            return@suspendCoroutine
        }

        val readRequest = DataReadRequest.Builder()
            .read(dataType)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                continuation.resume(response)
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    fun hasPermissions(): Boolean {
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun getFitnessOptions() = fitnessOptions
}
