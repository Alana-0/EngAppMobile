package com.myapps.pacman.flowData

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position

data class PacmanData(
    val position: Position,
    val direction: Direction,
    val isEnergizer: Boolean,
    val movementsDelay: Long = 200L
)
