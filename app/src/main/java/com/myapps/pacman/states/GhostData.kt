package com.myapps.pacman.states

import com.myapps.pacman.utils.Direction
import kotlinx.coroutines.Delay

data class GhostData(
    val ghostPosition: Pair<Float, Float> = Pair(-1f,-1f),
    val ghostDirection: Direction = Direction.NOWHERE,
    val ghostLifeStatement: Boolean = true,
    val ghostDelay: Long = 0L
)