package com.example.onroadvehiclebreakdown

class User {
    var email: String? = null
    var password: String? = null
    var selectedUserRole: String? = null
    var name: String? = null
    var phone: String? = null
    var carNumber: String? = null
    var address: String? = null

    constructor() // Default constructor required for Firebase

    constructor(
        email: String?,
        password: String?,
        selectedUserRole: String?,
        name: String?,
        phone: String?,
        carNumber: String?,
        address: String?
    ) {
        this.email = email
        this.password = password
        this.selectedUserRole = selectedUserRole
        this.name = name
        this.phone = phone
        this.carNumber = carNumber
        this.address = address
    }
    constructor(
        email: String?,
        password: String?,
        selectedUserRole: String?,
    ) {
        this.email = email
        this.password = password
        this.selectedUserRole = selectedUserRole
    }
}