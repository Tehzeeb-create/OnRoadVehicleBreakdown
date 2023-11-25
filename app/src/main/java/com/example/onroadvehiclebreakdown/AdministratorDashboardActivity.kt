package com.example.onroadvehiclebreakdown

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.widget.TableLayout
import android.widget.TableRow

class AdministratorDashboardActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_administrator_dashboard)

//        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference


        val tableLayout: TableLayout = findViewById(R.id.tableLayout)

        // Read data from the "users" node
        databaseReference.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    // Assuming your User class has email and selectedUserRole properties
                    val email = userSnapshot.child("email").getValue(String::class.java)
                    val selectedUserRole = userSnapshot.child("selectedUserRole").getValue(String::class.java)

                    // Display data in the table
                    displayDataInTable(email, selectedUserRole, tableLayout)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun displayDataInTable(email: String?, selectedUserRole: String?, tableLayout: TableLayout) {
        // Create a new table row
        val tableRow = TableRow(this)

        // Create TextViews to display email and selectedUserRole
        val emailTextView = TextView(this)
        val roleTextView = TextView(this)

        // Set text for TextViews
        emailTextView.text = email
        roleTextView.text = selectedUserRole

        // Add TextViews to the table row
        tableRow.addView(emailTextView)
        tableRow.addView(roleTextView)

        // Add the table row to the TableLayout
        tableLayout.addView(tableRow)
    }
}
