package com.example.onroadvehiclebreakdown

class User {
    var email: String? = null
    var password: String? = null
    var  selectedUserRole: String? = null

    constructor() // Default constructor required for Firebase

    constructor(email: String?, password: String?,  selectedUserRole: String?) {
        this.email = email
        this.password = password
        this.selectedUserRole = selectedUserRole
    }
}