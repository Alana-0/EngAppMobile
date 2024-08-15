package com.myapps.pacman.flowData

import com.myapps.pacman.utils.matrix.Matrix

data class BoardData(
    val charData:Matrix<Char>,
    val scorer:Int,
    val pacmanLives:Int,
    val isBell:Boolean,
    val currentLevel:Int = 0
)
