package com.hiringtest.minesweeper.api.client.model

import kotlinx.serialization.Serializable

@Serializable
data class Square (
    val column : Int,
    val displayValue : String,
    val flag : Boolean,
    val revealed : Boolean,
    val row : Int
)