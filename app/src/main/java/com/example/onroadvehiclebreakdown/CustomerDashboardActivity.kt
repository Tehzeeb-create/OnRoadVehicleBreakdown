package com.example.onroadvehiclebreakdown

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CustomerDashboardActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        logoutButton.setOnClickListener {
            // Sign the user out
            FirebaseAuth.getInstance().signOut()

            // Navigate back to the login screen (MainActivity)
            val intent = Intent(this@CustomerDashboardActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}
