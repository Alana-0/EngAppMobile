package com.myapps.pacman.game

import com.myapps.pacman.board.BoardController
import com.myapps.pacman.modules.qualifiers.DispatcherDefault
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.Quadruple
import com.myapps.pacman.utils.TypeOfCollision
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


class CollisionHandler(
    private val coroutineDispatcher: CoroutineDispatcher
):ICollisionHandler{
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> get() = _isPaused


    override var handlePelletCollision: (Position) -> Unit = {}
    override var handleEnergizerCollision: (Position) -> Unit = {}
    override var handleBellCollision: (Position) -> Unit = {}
    override var handleGhostEaten: (GhostData) -> Unit = {}
    override var handlePacmanDeath: () -> Unit = {}

    private var job: Job? = null

    override fun startObservingCollisions(
        pacmanState: StateFlow<PacmanData>,
        ghostStates: List<StateFlow<GhostData>>,
        mapState: StateFlow<BoardData>
    ) {
        _isPaused.value = false
        job?.cancel()
        job = CoroutineScope(coroutineDispatcher).launch {
            combine(
                pacmanState,
                combineGhostsStateFlows(ghostStates),
                mapState,
                isPaused
            ) { pacman, ghostsList, map, paused ->
                Quadruple(pacman, ghostsList, map, paused)
            }.distinctUntilChanged()
                .collect { (pacman, ghostsList, map, paused) ->
                    if (!paused) {
                        handlePacmanCollisions(pacman, ghostsList)
                        handlePacmanFoodCollisions(pacman, map)
                    }
                }
        }
    }

    private fun handlePacmanFoodCollisions(pacman: PacmanData, boardData: BoardData) {
        when (checkCollisionWithFood(boardData.gameBoardData, pacman.pacmanPosition)) {
            TypeOfCollision.PELLET -> handlePelletCollision(pacman.pacmanPosition)
            TypeOfCollision.ENERGIZER -> handleEnergizerCollision(pacman.pacmanPosition)
            TypeOfCollision.BELL -> handleBellCollision(pacman.pacmanPosition)
            else -> {}
        }
    }

    private fun handlePacmanCollisions(pacman: PacmanData, ghosts: List<GhostData>) {
        ghosts.forEach { ghost ->
            if (checkCollision(ghost, pacman.pacmanPosition)) {
                if (pacman.energizerStatus) {
                    handleGhostEaten(ghost)
                } else {
                    handlePacmanDeath()
                }
            }
        }
    }


    private fun checkCollision(ghost: GhostData, position: Position): Boolean {
        return ghost.ghostLifeStatement && ghost.ghostPosition == position
    }

    private fun checkCollisionWithFood(map: Matrix<Char>, position: Position): TypeOfCollision {
        return when (map.getElementByPosition(position.positionX, position.positionY)) {
            BoardController.PELLET_CHAR -> TypeOfCollision.PELLET
            BoardController.ENERGIZER_CHAR -> TypeOfCollision.ENERGIZER
            BoardController.BELL_CHAR -> TypeOfCollision.BELL
            else -> TypeOfCollision.NONE
        }
    }

    override fun cancelCollisionObservation(){
        job?.cancel()
        job = null
    }

    private fun combineGhostsStateFlows(ghostsStateFlows: List<StateFlow<GhostData>>): StateFlow<List<GhostData>> {
        return combine(ghostsStateFlows) { ghostArray ->
            ghostArray.toList()
        }.stateIn(CoroutineScope(coroutineDispatcher), SharingStarted.Lazily, emptyList())
    }

    override fun pauseCollisionObservation(){
        _isPaused.value = true
    }

    override fun resumeCollisionObservation(){
        _isPaused.value = false
    }
}