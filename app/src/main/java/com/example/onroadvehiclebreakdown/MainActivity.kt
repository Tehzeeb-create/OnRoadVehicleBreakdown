package com.example.onroadvehiclebreakdown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null // Change to nullable to avoid nullexception
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference

        val editTextEmail = findViewById<EditText>(R.id.main_editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.main_editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.main_buttonLogin)
        val textViewResult = findViewById<TextView>(R.id.main_textViewResult)
        val buttonRegister = findViewById<Button>(R.id.main_buttonRegister)
        val forgotPasswordText = findViewById<TextView>(R.id.forgot_password_text)


        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                // Display an error message if either email or password is empty
                Toast.makeText(this, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (email == "administrator" && password == "Tehzeeb@123") {
                // Navigate to the administrator panel
                val intent = Intent(this@MainActivity, AdministratorDashboardActivity::class.java)
                startActivity(intent)
            } else {
                // Check if auth is not null before using it
                auth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Login successful
                            // Now, retrieve the selectedUserRole from Firebase Database
                            val currentUser = auth?.currentUser
                            currentUser?.uid?.let { userId ->
                                val userRef = databaseReference.child("users").child(userId)
                                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val selectedUserRole =
                                            dataSnapshot.child("selectedUserRole").value as? String
                                        if (selectedUserRole == "Customer") {
                                            val intent = Intent(
                                                this@MainActivity,
                                                CustomerDashboardActivity::class.java
                                            )
                                            startActivity(intent)
                                        } else if (selectedUserRole == "Mechanic") {
                                            val intent = Intent(
                                                this@MainActivity,
                                                MechanicDashboardActivity::class.java
                                            )
                                            startActivity(intent)
                                        } else {
                                            textViewResult.text = "Unknown User Role"
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        textViewResult.text = "Login Unsuccessful"
                                    }
                                })
                            }
                        } else {
                            // Login unsuccessful
                            textViewResult.text = "Login Unsuccessful"
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()



                        }
                    }
            }
        }

        buttonRegister.setOnClickListener {
            val intent = Intent(this@MainActivity, SignupActivity::class.java)
            startActivity(intent)
        }
        forgotPasswordText.setOnClickListener {
            // Handle the "Forgot Password?" click here
            // You can start a new activity or show a password reset dialog
            // For example, you can start a PasswordResetActivity
            val intent = Intent(this@MainActivity, PasswordResetActivity::class.java)
            startActivity(intent)
        }
    }
}
