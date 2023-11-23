package com.example.onroadvehiclebreakdown


import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.location.FusedLocationProviderClient


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlin.random.Random

class CustomerDashboardActivity : FragmentActivity(), OnMapReadyCallback {


    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val DEFAULT_ZOOM = 15.0f
    private val TAG = "CustomerDashboardActivity"
    private var map: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST = 1

    private lateinit var saveButton: Button
    private lateinit var sosButton: Button
    private lateinit var closestPersonTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var carEditText: EditText
    private lateinit var addressEditText: EditText

    private val defaultLocation = LatLng(33.4323, 73.02182)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)



        // Check and request location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        val myInfoButton = findViewById<Button>(R.id.myInfoButton)
        val myInfoSection = findViewById<LinearLayout>(R.id.myInfoSection)
        val CurrentLocation = findViewById<ImageButton>(R.id.currentLoc)

        // Initialize UI elements
        saveButton = findViewById(R.id.saveButton)
        sosButton = findViewById(R.id.sosButton)
        //closestPersonTextView = findViewById(R.id.closestPersonTextView)
        nameEditText = findViewById(R.id.nameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        carEditText = findViewById(R.id.carEditText)
        addressEditText = findViewById(R.id.addressEditText)

        CurrentLocation.setOnClickListener {
            moveToCurrentLocation()
        }
        // Set onClickListener for Save Button
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val carNumber = carEditText.text.toString()
            val address = addressEditText.text.toString()

            val userId = auth.currentUser?.uid // Get the current user's unique ID
            myInfoSection.visibility = View.INVISIBLE
            userId?.let { uid ->
                // Reference to the "users" node in your database
                val usersRef = databaseReference.child("users").child(uid)

                // Fetch the existing user data
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userData = dataSnapshot.getValue(User::class.java)
                        updateUserLocationOnMap()

                        userData?.let { user ->

                            val updatedUser = User(
                                email = user.email,
                                password = user.password,
                                selectedUserRole = user.selectedUserRole,
                                name = name,
                                phone = phone,
                                carNumber = carNumber,
                                address = address,
                                location = null
                            )

                            usersRef.setValue(updatedUser)
                                .addOnSuccessListener {
                                    Toast.makeText(this@CustomerDashboardActivity, "User information updated", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@CustomerDashboardActivity, "Failed to update user information", Toast.LENGTH_SHORT).show()
                                }

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database error
                        Toast.makeText(this@CustomerDashboardActivity, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                    }
                })
            }

        }

        myInfoButton.setOnClickListener {
            if (myInfoSection.visibility == View.VISIBLE) {
                myInfoSection.visibility = View.INVISIBLE
            } else {
                myInfoSection.visibility = View.VISIBLE
            }
        }
        sosButton.setOnClickListener {
            //closestPersonTextView.text = "SOS, help me"
        }

        // Calculate and display closest person's name
        calculateClosestPerson()
    }

    private fun moveToCurrentLocation() {
        updateUserLocationOnMap()
    }
    private fun calculateClosestPerson() {
        // This is where you implement the logic to find the closest person and update the UI
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isMyLocationButtonEnabled = true
        enableMyLocation()
        moveToCurrentLocation()


        val userLocation = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
        addRandomMarkersAroundUserLocation(userLocation)
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map?.isMyLocationEnabled = true
        }
    }


    fun updateUserLocationOnMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation

            locationResult.addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation: Location? = task.result
                    if (lastKnownLocation != null) {
                        val userLocation = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM))
                        updateLocationInFirebase(userLocation)

                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                } else {
                    Log.e(TAG, "Exception: ${task.exception}")
                    Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    private fun updateLocationInFirebase(location: LatLng) {
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            val usersRef = databaseReference.child("users").child(uid)
            val updateData = mapOf<String, Any>("location" to location)
            usersRef.updateChildren(updateData)

        }
    }

    private fun addRandomMarkersAroundUserLocation(userLocation: LatLng) {
        val numberOfMarkers = 20
        val maxDistanceInMeters = 5000 // Adjust this distance as needed

        for (i in 0 until numberOfMarkers) {
            val randomDistance = Random.nextDouble(0.0, maxDistanceInMeters.toDouble())
            val randomBearing = Random.nextDouble(0.0, 360.0)

            val newLatLng = calculateLatLng(userLocation, randomDistance, randomBearing)

            // Add a marker for the new coordinates
            map?.addMarker(MarkerOptions().position(newLatLng).title("Mechanic $i"))
        }
    }

    private fun calculateLatLng(startLatLng: LatLng, distance: Double, bearing: Double): LatLng {
        val earthRadius = 6371000.0 // Earth's radius in meters

        val lat1 = Math.toRadians(startLatLng.latitude)
        val lon1 = Math.toRadians(startLatLng.longitude)
        val angularDistance = distance / earthRadius

        val lat2 = Math.asin(
            Math.sin(lat1) * Math.cos(angularDistance) +
                    Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(Math.toRadians(bearing))
        )

        val lon2 = lon1 + Math.atan2(
            Math.sin(Math.toRadians(bearing)) * Math.sin(angularDistance) * Math.cos(lat1),
            Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2)
        )

        return LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }



}



