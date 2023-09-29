package com.example.onroadvehiclebreakdown

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.widget.Spinner
import android.widget.ArrayAdapter

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()


        val editTextUsername = findViewById<EditText>(R.id.signup_editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.signup_editTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.signup_editTextConfirmPassword)
        val spinnerUserRole = findViewById<Spinner>(R.id.signup_spinnerUserRole)
        val buttonRegister = findViewById<Button>(R.id.signup_buttonRegister)

        // Create an ArrayAdapter for the spinner using the user_roles array
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_roles,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUserRole.adapter = adapter

        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            val selectedUserRole = spinnerUserRole.selectedItem.toString()



            if (password == confirmPassword ) {
                // Create a new user with email and password using Firebase Authentication
                auth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Registration successful
                            val user = auth.currentUser
                            if (user != null) {
                                // Store user data in Firebase Realtime Database
                                val userId = user.uid
                                val email = user.email ?: "N/A"

                                // Reference to the "users" node in your database
                                val usersRef = database.reference.child("users")

                                // Create a user object with email and password
                                val userData = User(email, password , selectedUserRole)

                                // Set user data under the user's ID
                                usersRef.child(userId).setValue(userData)

                                // We can navigate to another activity or perform other actions here
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                                //to move it back to Main activity
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            // Handle registration failure
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // Password and confirm password do not match
                editTextConfirmPassword.error = "Passwords do not match"
            }
        }
    }
}
