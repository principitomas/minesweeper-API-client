package com.hiringtest.minesweeper.api.client.model

import kotlinx.serialization.Serializable

@Serializable
data class Credentials (
    val user : String,
    val password : String
)
