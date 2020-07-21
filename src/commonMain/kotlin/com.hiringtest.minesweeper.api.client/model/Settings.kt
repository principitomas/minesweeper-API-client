package com.hiringtest.minesweeper.api.client.model

import kotlinx.serialization.Serializable

@Serializable
data class Settings (
    val columns : Int,
    val mines : Int,
    val rows : Int
)