package ru.telematica.casco2go.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat;
import android.os.Build
import ru.telematica.casco2go.App
import java.util.jar.Manifest

/**
 * Created by m.sidorov on 02.05.2018.
 */
object Permissions {

    val REQUEST_ACCESS_FINE_LOCATION: Int = 1294

    fun checkLocation(): Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(App.instance, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            return true
        }
    }

    fun requestLocation(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
    }
}