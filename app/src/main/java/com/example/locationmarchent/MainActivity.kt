package com.example.locationmarchent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.location.LocationCallback
import com.example.location.LocationInfo
import com.example.location.LocationPermissionCallback
import com.example.location.data.models.LocationData
import com.example.location.utils.Constants
import com.example.locationmarchent.base.BasePermissionScreen
import com.example.locationmarchent.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : BasePermissionScreen(), LocationCallback, LocationPermissionCallback {
    lateinit var binding: ActivityMainBinding

    private lateinit var locationInfo: LocationInfo

    override fun onPermissionGranted(permission: String) {
        locationInfo.onPermissionGranted()
    }

    override fun onNeverAskAgainChecked(permission: String) {
        // TODO:: SHOW ALERT FOR onNeverAskAgainChecked
    }

    override fun onPermissionDenied(permission: String) {
        // TODO:: SHOW ALERT FOR onPermissionDenied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationInfo = LocationInfo(this, this, this)
        locationInfo.init()

        binding.btnRequest.setOnClickListener {
            locationInfo.getCurrentLocation()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.RequestCodes.REQUEST_CHECK_SETTINGS) {
            when (resultCode) {
                RESULT_OK -> locationInfo.onLocationServiceEnabledGranted()
                RESULT_CANCELED -> Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLocationSuccessListener(locationData: LocationData) {
        binding.txtView.text = locationData.toString()
    }

    override fun onLocationFailureListener(exception: Exception) {
        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
    }

    override fun onCheckLocationPermissions(permissions: ArrayList<String>) {
        checkPermissions(
            this,
            Constants.PermissionTags.LOCATION_TAG,
            "It seems that the location permission not granted\n you want to granted?",
            permissions
        )
    }
}