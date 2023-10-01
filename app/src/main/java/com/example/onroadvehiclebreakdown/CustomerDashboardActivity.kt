package com.example.onroadvehiclebreakdown


import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CustomerDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private lateinit var saveButton: Button
    private lateinit var sosButton: Button
    private lateinit var closestPersonTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var carEditText: EditText
    private lateinit var addressEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

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

                // Create a user object with updated information
                val updatedUser = User(
                    email = null, // You can set this to null if it's not needed for this operation
                    password = null, // You can set this to null if it's not needed for this operation
                    selectedUserRole = null, // You can set this to null if it's not needed for this operation
                    name = name,
                    phone = phone,
                    carNumber = carNumber,
                    address = address
                )

                // Update the user's data in the database
                usersRef.setValue(updatedUser)
                    .addOnSuccessListener {
                        // Update successful
                        Toast.makeText(this, "User information updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // Handle update failure
                        Toast.makeText(this, "Failed to update user information", Toast.LENGTH_SHORT).show()
                    }
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

    // Implement your logic to calculate and display the closest person's name
    private fun calculateClosestPerson() {
        // This is where you implement the logic to find the closest person and update the UI
        // Example: closestPersonTextView.text = "Closest person: John Doe"
    }
}
