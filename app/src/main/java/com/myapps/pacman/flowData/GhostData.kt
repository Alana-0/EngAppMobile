package com.myapps.pacman.flowData

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.ghost.GhostMode

data class GhostData(
    val position: Position,
    val direction: Direction,
    val lifeStatement:Boolean = true,
    val speedDelay:Long = 200L
)