package com.hiringtest.minesweeper.api.client.model

import kotlinx.serialization.Serializable

@Serializable
data class Game (
    val id : Int,
    val settings : Settings,
    val squares : List<Square>,
    val status : String
)
