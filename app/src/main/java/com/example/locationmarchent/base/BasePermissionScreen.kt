package com.example.locationmarchent.base

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.location.utils.Constants
import com.example.locationmarchent.R
import com.example.locationmarchent.utils.UIUtils

abstract class BasePermissionScreen : AppCompatActivity() {
    private var mPermissionsTag: String? = null

    companion object {

        private const val MY_PERMISSIONS_REQUEST = 1
    }

    private var notGrantedPermissions = ArrayList<String>()

    abstract fun onPermissionGranted(permission: String)

    /**
     * Called right after the system permissions dialogs if the user checks "never ask again"
     *     on any of the requested permissions.
     */
    abstract fun onNeverAskAgainChecked(permission: String)

    abstract fun onPermissionDenied(permission: String)

    protected fun checkPermissions(
        context: Context, permissionTag: String? = null, rationaleDialogeMessage: String = "",
        permissions: ArrayList<String>
    ) {
        if (permissions.isEmpty())
            throw Exception("Check permission called without any permissions")
        mPermissionsTag = permissionTag
        notGrantedPermissions = ArrayList() // reset

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPermissions.add(permission)
            }
        }

        if (notGrantedPermissions.isEmpty()) {
            onPermissionGranted(permissionTag ?: permissions[0])
        } else {
            if (shouldShowRationale(notGrantedPermissions)) {
                showCustomDialog(rationaleDialogeMessage)
            } else {
                requestPermissions(notGrantedPermissions.toTypedArray(), MY_PERMISSIONS_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.RequestCodes.REQUEST_APP_SETTINGS) {
            var allPermissionsGranted = true

            for (permission in notGrantedPermissions) {
                if (!isPermissionGranted(permission)) {
                    allPermissionsGranted = false
                }
            }

            when {
                allPermissionsGranted -> onPermissionGranted(
                    mPermissionsTag
                        ?: notGrantedPermissions[0]
                )
                else -> onPermissionDenied(
                    mPermissionsTag
                        ?: notGrantedPermissions[0]
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Handle permissions requested by looping on the permissions and
     * setting two flags one for when all permissions are granted and one for when never ask again checked in any of the permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    var allPermissionsGranted = true
                    var shouldShowRationale = true

                    for (grantResult in grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            allPermissionsGranted = false
                            val showRationale = shouldShowRequestPermissionRationale(
                                permissions[grantResults.indexOf(grantResult)]
                            )
                            if (!showRationale) {
                                shouldShowRationale = false
                                break
                            }
                        }
                    }

                    when {
                        !shouldShowRationale -> onNeverAskAgainChecked(
                            mPermissionsTag
                                ?: permissions[0]
                        )
                        allPermissionsGranted -> onPermissionGranted(
                            mPermissionsTag
                                ?: permissions[0]
                        )
                        else -> onPermissionDenied(
                            mPermissionsTag
                                ?: permissions[0]
                        )
                    }
                }
            }
        }
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this.applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRationale(permissions: ArrayList<String>): Boolean {
        var isNeed = false
        for (i in permissions.indices) {
            if (shouldShowRequestPermissionRationale(permissions[i])) {
                isNeed = true
                break
            }
        }
        return isNeed
    }

    private fun showCustomDialog(message: String) {
        UIUtils.showBasicDialog(this.applicationContext, null, message,
            getString(R.string.cont), getString(R.string.cancel),
            { _, _ ->
                requestPermissions(notGrantedPermissions.toTypedArray(), MY_PERMISSIONS_REQUEST)
            },
            { _, _ ->
                onPermissionDenied(mPermissionsTag ?: notGrantedPermissions[0])
            })
    }
}