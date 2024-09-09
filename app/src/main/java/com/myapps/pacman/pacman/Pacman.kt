package com.myapps.pacman.pacman


import com.myapps.pacman.board.BoardController
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Pacman(
    initialPosition: Position,
    initialDirection: Direction = Direction.RIGHT,
    initialEnergizerStatus: Boolean = false,
    private val actorsMovementsTimerController: ActorsMovementsTimerController
) {

    private var direction: Direction = initialDirection
    private var lifeStatement: Boolean = true
    private var energizerStatus: Boolean = initialEnergizerStatus
    private var currentPosition: Position = initialPosition

    private val _pacmanState = MutableStateFlow(
        PacmanData(
            currentPosition,
            direction,
            energizerStatus,
            actorsMovementsTimerController.getPacmanSpeedDelay().toLong(),
            lifeStatement
        )
    )
    val pacmanState: StateFlow<PacmanData> get() = _pacmanState

    suspend fun startMoving(
        movements: MutableList<Direction>,
        currentMap: () -> Matrix<Char>
    ){
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.PACMAN_ENTITY_TYPE){
            updatePosition(
                movements,
                currentMap()
            )
        }
    }

    private fun updatePosition(
        movements: MutableList<Direction>,
        currentMap: Matrix<Char>
    ) {
        val primaryDirection = movements.getOrNull(0) ?: return
        val secondaryDirection = movements.getOrNull(1)


        val newPosition = getPacmanPossiblePosition(currentPosition, primaryDirection)

        if (checkTransfer(newPosition, primaryDirection, currentMap)) {
            updatePacmanState(currentPosition, direction)
            return
        }

        if (secondaryDirection != null && primaryDirection != secondaryDirection) {
           val newSecondPosition = getPacmanPossiblePosition(currentPosition, secondaryDirection)
            if (!isWallCollision(newSecondPosition, currentMap)) {
                move(secondaryDirection)
                this.direction = secondaryDirection
                updatePacmanState(currentPosition, secondaryDirection)
                movements.removeAt(0)
                return
            }
        }

        if (!isWallCollision(newPosition, currentMap)) {
            move(primaryDirection)
            this.direction = primaryDirection
            updatePacmanState(currentPosition, primaryDirection)
            return
        }
    }

    private fun getPacmanPossiblePosition(position: Position, direction: Direction): Position =
        when (direction) {
            Direction.RIGHT -> position.copy(positionY = position.positionY + 1)
            Direction.LEFT -> position.copy(positionY = position.positionY - 1)
            Direction.DOWN -> position.copy(positionX = position.positionX + 1)
            Direction.UP -> position.copy(positionX = position.positionX - 1)
            Direction.NOWHERE -> position
        }

    private fun isWallCollision(position: Position, currentMap: Matrix<Char>): Boolean {
        val elementPosition =
            currentMap.getElementByPosition(position.positionX, position.positionY)
        return elementPosition == BoardController.WALL_CHAR || elementPosition == BoardController.GHOST_DOOR_CHAR
    }

    private fun checkTransfer(
        position: Position,
        direction: Direction,
        currentMap: Matrix<Char>
    ): Boolean = when (direction) {
        Direction.RIGHT -> {
            if (position.positionY >= currentMap.getColumns()) {
                // Transferir al borde izquierdo
                currentPosition = currentPosition.copy(positionY = 0)
                true
            } else false
        }
        Direction.LEFT -> {
            if (position.positionY < 0) {
                // Transferir al borde derecho
                currentPosition = currentPosition.copy(positionY = currentMap.getColumns() - 1)
                true
            } else false
        }
        else -> false
    }

    private fun move(direction: Direction) {
        when (direction) {
            Direction.RIGHT -> currentPosition =
                currentPosition.copy(positionY = currentPosition.positionY + 1)

            Direction.LEFT -> currentPosition =
                currentPosition.copy(positionY = currentPosition.positionY - 1)

            Direction.UP -> currentPosition =
                currentPosition.copy(positionX = currentPosition.positionX - 1)

            Direction.DOWN -> currentPosition =
                currentPosition.copy(positionX = currentPosition.positionX + 1)

            Direction.NOWHERE -> {}
        }
    }

    private fun updatePacmanState(newPosition: Position, newDirection: Direction) {
        if (_pacmanState.value.pacmanPosition != newPosition || _pacmanState.value.pacmanDirection != newDirection) {
            _pacmanState.value = _pacmanState.value.copy(
                pacmanPosition = newPosition,
                pacmanDirection = newDirection
            )
        }
    }
    fun updateDirection(direction: Direction){
        this.direction = direction
        _pacmanState.value = _pacmanState.value.copy(
            pacmanDirection = this.direction
        )
    }

    fun updatePosition(position: Position){
        this.currentPosition = position
        _pacmanState.value = _pacmanState.value.copy(
            pacmanPosition = currentPosition
        )
    }

    fun updateLifeStatement(lifeStatement:Boolean){
        this.lifeStatement = lifeStatement
        _pacmanState.value = _pacmanState.value.copy(
            lifeStatement = this.lifeStatement
        )
    }

    fun updateEnergizerStatus(energizerStatus: Boolean){
        this.energizerStatus = energizerStatus
        _pacmanState.value = _pacmanState.value.copy(
            energizerStatus = this.energizerStatus
        )
    }

    fun updateSpeedDelay(speedDelay: Int){
        actorsMovementsTimerController.setActorSpeedFactor(ActorsMovementsTimerController.PACMAN_ENTITY_TYPE,speedDelay)
        _pacmanState.value = _pacmanState.value.copy(
            speedDelay = actorsMovementsTimerController.getPacmanSpeedDelay().toLong()
        )
    }
}