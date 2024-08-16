package com.myapps.pacman.states

import com.myapps.pacman.utils.Direction

data class PacmanData(
    val pacmanPosition: Pair<Float,Float> = Pair(-1f,-1f),
    val pacmanDirection: Direction = Direction.RIGHT,
    val energizerStatus: Boolean = false,
    val speedDelay:Long = 0L
)