package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Position

class Pinky(
    currentPosition: Position,
    target: Position,
    scatterTarget: Position,
    doorTarget: Position,
    home: Position,
    homeXRange: IntRange,
    homeYRange: IntRange,
    direction: Direction,
    private val actorsMovementsTimerController: ActorsMovementsTimerController
) : Ghost(
    currentPosition = currentPosition,
    target = target,
    scatterTarget = scatterTarget,
    doorTarget = doorTarget,
    home = home,
    homeXRange = homeXRange,
    homeYRange = homeYRange,
    direction = direction
) {

    private fun calculateTarget(pacman: Pacman) {
        when (pacman.direction) {
            Direction.RIGHT -> {
                target =
                    pacman.currentPosition.copy(positionY = pacman.currentPosition.positionY + 4)
            }

            Direction.UP -> {
                target =
                    pacman.currentPosition.copy(positionX = pacman.currentPosition.positionX - 4)
            }

            Direction.DOWN -> {
                target =
                    pacman.currentPosition.copy(positionX = pacman.currentPosition.positionX + 4)
            }

            Direction.LEFT -> {
                target =
                    pacman.currentPosition.copy(positionY = pacman.currentPosition.positionY - 4)
            }

            Direction.NOWHERE -> {
                target = pacman.currentPosition
            }
        }
    }

    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: ()->GhostMode,
        onPacmanCollision: (Ghost, Pacman) -> Boolean,
        onUpdatingMoveAndDirection: () -> Unit
    ){
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.PINKY_ENTITY_TYPE){
            return@controlTime updatePosition(
                currentMap(),
                pacman(),
                ghostMode(),
                onPacmanCollision,
                onUpdatingMoveAndDirection
            )
        }
    }

    fun updatePosition(
        currentMap: Matrix<Char>,
        pacman: Pacman,
        ghostMode: GhostMode,
        onPacmanCollision: (Ghost, Pacman) -> Boolean,
        onUpdatingMoveAndDirection: () -> Unit
    ):Boolean {
        this.updateStatus(pacman, ghostMode)
        if (isTargetToCalculate(pacman)) {
            calculateTarget(pacman)
        }
        calculateDirections(currentMap)
        this.move(this.direction)
        var collisionIsProduced = onPacmanCollision(this, pacman)
        updateSpeedDelay(pacman)
        onUpdatingMoveAndDirection()
        if (!collisionIsProduced && this.checkTransfer(currentPosition, direction, currentMap)) {
            collisionIsProduced = onPacmanCollision(this, pacman)
            updateSpeedDelay(pacman)
            onUpdatingMoveAndDirection()
        }
        return collisionIsProduced
    }

    private fun updateSpeedDelay(pacman: Pacman){
        when {
            !this.lifeStatement -> {
                if (actorsMovementsTimerController.getPinkySpeedDelay() != ActorsMovementsTimerController.DEATH_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.PINKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.DEATH_SPEED_DELAY
                    )
                }
            }

            pacman.energizerStatus -> {
                if (actorsMovementsTimerController.getPinkySpeedDelay() != ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.PINKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
                    )
                }
            }

            else -> {
                if (actorsMovementsTimerController.getPinkySpeedDelay() != ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.PINKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
                    )
                }
            }
        }
    }

}