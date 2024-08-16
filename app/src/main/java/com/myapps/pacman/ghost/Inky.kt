package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Position

class Inky(
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

    private fun calculateTarget(pacman: Pacman, blinkyPosition: Position) {
        var posX = pacman.currentPosition.positionX
        var posy = pacman.currentPosition.positionY
        when (pacman.direction) {
            Direction.RIGHT -> {
                posy += 2
            }

            Direction.LEFT -> {
                posy -= 2
            }

            Direction.UP -> {
                posX -= 2
            }

            Direction.DOWN -> {
                posX += 2
            }

            Direction.NOWHERE -> {}
        }

        val posX1 = posX - blinkyPosition.positionX
        val posY1 = posy - blinkyPosition.positionY

        target = target.copy(positionX = posX1, positionY = posY1)
    }


    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: () -> GhostMode,
        blinkyPosition: () -> Position,
        onPacmanCollision: (Ghost, Pacman) -> Boolean,
        onUpdatingMoveAndDirection: () -> Unit
    ) {
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.INKY_ENTITY_TYPE) {
           return@controlTime updatePosition(
                currentMap(),
                pacman(),
                ghostMode(),
                blinkyPosition(),
                onPacmanCollision,
                onUpdatingMoveAndDirection
            )
        }
    }

    fun updatePosition(
        currentMap: Matrix<Char>,
        pacman: Pacman,
        ghostMode: GhostMode,
        blinkyPosition: Position,
        onPacmanCollision: (Ghost, Pacman) -> Boolean,
        onUpdatingMoveAndDirection: () -> Unit
    ):Boolean {
        this.updateStatus(pacman, ghostMode)
        if (isTargetToCalculate(pacman)) {
            calculateTarget(pacman, blinkyPosition)
        }
        this.calculateDirections(currentMap)
        this.move(this.direction)
        var collisionProduced = onPacmanCollision(this,pacman)
        updateSpeedDelay(pacman)
        onUpdatingMoveAndDirection()
        if (!collisionProduced && this.checkTransfer(currentPosition, direction, currentMap)) {
            collisionProduced = onPacmanCollision(this, pacman)
            updateSpeedDelay(pacman)
            onUpdatingMoveAndDirection()
        }
        return collisionProduced
    }

    private fun updateSpeedDelay(pacman: Pacman) {
        when {
            !this.lifeStatement -> {
                if (actorsMovementsTimerController.getInkySpeedDelay() != ActorsMovementsTimerController.DEATH_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.INKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.DEATH_SPEED_DELAY
                    )
                }
            }

            pacman.energizerStatus -> {
                if (actorsMovementsTimerController.getInkySpeedDelay() != ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.INKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
                    )
                }
            }

            else -> {
                if (actorsMovementsTimerController.getInkySpeedDelay() != ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.INKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
                    )
                }
            }
        }
    }

}