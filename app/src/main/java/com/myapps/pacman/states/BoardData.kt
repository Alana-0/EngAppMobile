package com.myapps.pacman.states

import com.myapps.pacman.utils.matrix.Matrix

data class BoardData(
    val gameBoardData: Matrix<Char> = Matrix(0,0),
    val scorer:Int = 0,
    val pacmanLives:Int = 0,
    val currentLevel:Int = 0,
    val isGameWin:Boolean = false,
    val isGameLose:Boolean = false
)
