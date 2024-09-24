package com.myapps.pacman.game

import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.utils.Position
import kotlinx.coroutines.flow.StateFlow

interface ICollisionHandler {
    var handlePelletCollision: (Position) -> Unit
    var handleEnergizerCollision: (Position) -> Unit
    var handleBellCollision: (Position) -> Unit
    var handleGhostEaten: (GhostData) -> Unit
    var handlePacmanDeath: () -> Unit
    fun startObservingCollisions(
        pacmanState: StateFlow<PacmanData>,
        ghostStates: List<StateFlow<GhostData>>,
        mapState: StateFlow<BoardData>
    )
    fun cancelCollisionObservation()
    fun pauseCollisionObservation()
    fun resumeCollisionObservation()
}