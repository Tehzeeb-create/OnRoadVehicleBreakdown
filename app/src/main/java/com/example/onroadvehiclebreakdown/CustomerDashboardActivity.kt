package com.example.onroadvehiclebreakdown


import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.location.FusedLocationProviderClient


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class CustomerDashboardActivity : FragmentActivity(), OnMapReadyCallback {


    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val DEFAULT_ZOOM = 15.0f
    private val TAG = "CustomerDashboardActivity"
    private lateinit var map: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1

    private lateinit var saveButton: Button
    private lateinit var sosButton: Button
    private lateinit var closestPersonTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var carEditText: EditText
    private lateinit var addressEditText: EditText

    private val defaultLocation = LatLng(37.7749, -122.4194)




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



        // Initialize UI elements
        saveButton = findViewById(R.id.saveButton)
        sosButton = findViewById(R.id.sosButton)
        closestPersonTextView = findViewById(R.id.closestPersonTextView)
        nameEditText = findViewById(R.id.nameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        carEditText = findViewById(R.id.carEditText)
        addressEditText = findViewById(R.id.addressEditText)

        // Set onClickListener for Save Button
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val carNumber = carEditText.text.toString()
            val address = addressEditText.text.toString()

            val userId = auth.currentUser?.uid // Get the current user's unique ID

            userId?.let { uid ->
                // Reference to the "users" node in your database
                val usersRef = databaseReference.child("users").child(uid)

                // Fetch the existing user data
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userData = dataSnapshot.getValue(User::class.java)

                        userData?.let { user ->
                            val updatedUser = User(
                                email = user.email,
                                password = user.password,
                                selectedUserRole = user.selectedUserRole,
                                name = name,
                                phone = phone,
                                carNumber = carNumber,
                                address = address
                            )

                            // Update the user's data in the database
                            usersRef.setValue(updatedUser)
                                .addOnSuccessListener {
                                    // Update successful
                                    Toast.makeText(this@CustomerDashboardActivity, "User information updated", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    // Handle update failure
                                    Toast.makeText(this@CustomerDashboardActivity, "Failed to update user information", Toast.LENGTH_SHORT).show()
                                }
                            updateUserLocationOnMap()

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database error
                        Toast.makeText(this@CustomerDashboardActivity, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                    }
                })
            }

        }

        // Set onClickListener for SOS Button
        sosButton.setOnClickListener {
            // Display SOS message or perform SOS action
            closestPersonTextView.text = "SOS, help me"
        }

        // Calculate and display closest person's name (implement this logic)
        calculateClosestPerson()
    }

    private fun calculateClosestPerson() {
        // This is where you implement the logic to find the closest person and update the UI
        // Example: closestPersonTextView.text = "Closest person: John Doe"
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
        map.uiSettings.isMyLocationButtonEnabled = true
        updateUserLocationOnMap()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }
    }

    private fun updateUserLocationOnMap() {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation

            locationResult.addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val lastKnownLocation: Location? = task.result
                    if (lastKnownLocation != null) {
                        val userLocation =
                            LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                userLocation,
                                DEFAULT_ZOOM
                            )
                        )
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        map?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                defaultLocation,
                                DEFAULT_ZOOM
                            )
                        )
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


}

