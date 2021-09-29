package com.coprotect.myapplication.constants

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CheckingPermission(var context: Context) {

    fun checkCameraPermission(): Boolean {
        val result = (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
        val result1 =
            (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        return result && result1
    }

    fun requestCameraPermission() {
        val cameraPermission: Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val CAMERA_REQUEST_CODE = 100
        ActivityCompat.requestPermissions(context as Activity, cameraPermission, CAMERA_REQUEST_CODE)
    }

    fun checkStoragePermission(): Boolean {
        return (
                ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    fun requestStoragePermission() {
        val storagePermission: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val STORAGE_REQUEST_CODE = 200
        ActivityCompat.requestPermissions(context as Activity, storagePermission, STORAGE_REQUEST_CODE)
    }
}



