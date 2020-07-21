package com.hiringtest.minesweeper.api.client.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount (

        val email : String,
        val firstName : String,
        val lastName : String,
        val password : String
)