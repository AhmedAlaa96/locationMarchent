package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.example.location.data.models.LocationData
import com.example.location.utils.Constants.RequestCodes.REQUEST_CHECK_SETTINGS
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.tasks.Task


interface LocationCallback {
    fun onLocationSuccessListener(locationData: LocationData)
    fun onLocationFailureListener(exception: java.lang.Exception)
}

interface LocationPermissionCallback {
    fun onCheckLocationPermissions(permissions: ArrayList<String>)
}

class LocationInfo(
    private val mContext: Context,
    private val mLocationCallback: LocationCallback,
    private val mLocationPermissionCallback: LocationPermissionCallback
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(mContext)

    fun init() {
        if (!needLocationPermissions()) {
            mLocationPermissionCallback.onCheckLocationPermissions(
                arrayListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            onPermissionGranted()
        }
    }

    private fun needLocationPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationServiceEnabled(): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    fun onPermissionGranted() {
        if (isLocationServiceEnabled()) {
            requestLocation()
        } else {
            enableLocationService()
        }
    }

    fun onLocationServiceEnabledGranted() {
        requestLocation()
    }

    private fun enableLocationService() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000 / 2

        val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()

        locationSettingsRequestBuilder.addLocationRequest(locationRequest)
        locationSettingsRequestBuilder.setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(mContext)
        val task: Task<LocationSettingsResponse> =
            settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())
        task.addOnSuccessListener {
            requestLocation()
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        mContext as Activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendIntentException: SendIntentException) {
                    sendIntentException.printStackTrace()
                }
            }
        }
    }

    fun getCurrentLocation() {
        init()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            mCancellationToken
        ).addOnSuccessListener {
            mLocationCallback.onLocationSuccessListener(
                LocationData(
                    it.longitude,
                    it.latitude
                )
            )
        }.addOnFailureListener { exception ->
            mLocationCallback.onLocationFailureListener(exception)
            Toast.makeText(mContext, "NO Availability", Toast.LENGTH_SHORT).show()
        }
    }

    private val mCancellationToken: CancellationToken = object : CancellationToken() {
        override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken =
            CancellationTokenSource().token

        override fun isCancellationRequested(): Boolean = false

    }
}