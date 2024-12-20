package com.dicoding.view.maps

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.dicoding.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dicoding.databinding.ActivityMapsBinding
import com.dicoding.utils.getCircularBitmap
import com.dicoding.utils.showToast
import com.dicoding.view.ResultStories
import com.dicoding.view.ViewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.getStories()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->

                    if (task.isSuccessful && task.result != null) {
                        val location = task.result

                        val latitude = location.latitude
                        val longitude = location.longitude

                        Log.d("MyLocation", "Latitude: $latitude, Longitude: $longitude")
                        mMap.isMyLocationEnabled = true
                        val currentLocation = LatLng(latitude, longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12f))
                    } else {
                        Log.e("MyLocation", "Failed to get location")
                    }
                }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun addManyMarker() {
        viewModel.stories.observe(this) { result ->
            when (result) {
                is ResultStories.Loading -> {

                }

                is ResultStories.Success -> {

                    result.data?.listStory?.forEach { data ->
                        val latLng = data.lat?.let { data.lon?.let { it1 -> LatLng(it, it1) } }

                        val markerWidth = 150
                        val markerHeight = 150
                        Glide.with(this)
                            .asBitmap()
                            .load(data.photoUrl)
                            .override(markerWidth, markerHeight)
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    val circularBitmap = getCircularBitmap(resource)
                                    val imageMarkerOptions = latLng?.let {
                                        MarkerOptions()
                                            .position(it)
                                            .title(data.name)
                                            .snippet(data.description)
                                            .icon(BitmapDescriptorFactory.fromBitmap(circularBitmap))
                                    }
                                    val marker = imageMarkerOptions?.let { mMap.addMarker(it) }
                                    marker?.tag = data.photoUrl
                                }
                            })
                    }
                }

                is ResultStories.Error -> {

                    showToast(this, result.error)
                }
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        addManyMarker()
        getMyLocation()
    }
}