package com.harjot.currentlocation

import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.harjot.currentlocation.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var fusedLocationClient: FusedLocationProviderClient //for accessing all location methods
    val permissionRequestCode = 1000
    private val TAG = "address"
    var pgBar:ProgressBar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        pgBar = binding.pgBar
        if (checkPermission()){
            getLastLocation()
        }else{
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            permissionRequestCode->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLastLocation()
                }else{
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    fun checkPermission():Boolean{
        return ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
    }
    fun requestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION),
            permissionRequestCode
        )
    }
    fun getLastLocation(){
        pgBar?.visibility = View.VISIBLE
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED){
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                pgBar?.visibility = View.GONE
                if (location!=null){
                    var userLong = location.longitude
                    var userLat = location.latitude

                    Log.e(TAG, "address: $userLat $userLong", )
                    var address = getCompleteAddressString(userLat,userLong)
                    binding.tvLocation.text=address.toString()
                    binding.tvLatitude.text=userLat.toString()
                    binding.tvLongitude.text=userLong.toString()

                }
            }
    }
    fun getCompleteAddressString(latitude:Double,longitde:Double):String{
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude,longitde,1)
            if (addresses!=null && addresses.isNotEmpty()){
                val address = addresses[0]

                val addressString = address.getAddressLine(0)

                val placeIdIndex = addressString.indexOf(" ")
                if (placeIdIndex!=-1){
                    return addressString.substring(placeIdIndex+1)
                }else{
                    return addressString
                }
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
        return "No Address Found"
    }
}