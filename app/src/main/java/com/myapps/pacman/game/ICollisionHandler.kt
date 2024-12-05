package com.myapps.pacman.game
//define o contrato para gerenciar as colisões no jogo Pacman.
// Ela atua como uma camada de abstração, especificando os métodos e variáveis
// necessários para lidar com as diferentes interações entre o Pacman, fantasmas, e
// itens no tabuleiro, mas sem implementar diretamente essas funcionalidades.

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