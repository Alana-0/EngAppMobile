package com.myapps.pacman.states

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position

data class PacmanData(
    val pacmanPosition: Position = Position(-1,-1),
    val pacmanDirection: Direction = Direction.RIGHT,
    val energizerStatus: Boolean = false,
    val speedDelay:Long = 0L,
    val lifeStatement:Boolean = true
)