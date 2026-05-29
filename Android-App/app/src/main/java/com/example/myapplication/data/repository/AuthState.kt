package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.AuthResponseDto

// Holds the logged-in user for the lifetime of the process. Replace with
// DataStore/persistent storage (and real auth) when JWT is introduced.
object AuthState {
    var userId: Int = 0
        private set
    var fullName: String = ""
        private set
    var email: String = ""
        private set
    var role: String = ""
        private set
    var municipalityId: Int = 0
        private set

    val isAdmin: Boolean get() = role == "admin"

    fun set(auth: AuthResponseDto) {
        userId = auth.userId
        fullName = auth.fullName
        email = auth.email
        role = auth.role
        municipalityId = auth.municipalityId ?: 0
    }

    fun clear() {
        userId = 0
        fullName = ""
        email = ""
        role = ""
        municipalityId = 0
    }
}
