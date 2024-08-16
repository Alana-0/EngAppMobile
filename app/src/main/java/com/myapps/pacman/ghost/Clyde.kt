package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Position

class Clyde(
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
        val xRange =
            IntRange(pacman.currentPosition.positionX - 8, pacman.currentPosition.positionX + 8)
        val yRange =
            IntRange(pacman.currentPosition.positionY - 8, pacman.currentPosition.positionY + 8)

        target =
            if (xRange.contains(this.currentPosition.positionX) && yRange.contains(this.currentPosition.positionY)) {
                scatterTarget
            } else pacman.currentPosition
    }

    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: ()->GhostMode,
        onPacmanCollision: (Ghost, Pacman) -> Boolean,
        onUpdatingMoveAndDirection: () -> Unit
    ){
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.CLYDE_ENTITY_TYPE){
            return@controlTime updatePosition(
                currentMap(),
                pacman(),
                ghostMode(),
                onPacmanCollision,
                onUpdatingMoveAndDirection
            )
        }
    }
    private fun updatePosition(
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
        this.calculateDirections(currentMap)
        this.move(this.direction)
        var collisionProduced: Boolean = onPacmanCollision(this, pacman)
        updateSpeedDelay(pacman)
        onUpdatingMoveAndDirection()
        if (!collisionProduced && checkTransfer(currentPosition, direction, currentMap)) {
            collisionProduced = onPacmanCollision(this, pacman)
            updateSpeedDelay(pacman)
            onUpdatingMoveAndDirection()
        }
        return collisionProduced
    }

    private fun updateSpeedDelay(pacman: Pacman){
        when {
            !this.lifeStatement -> {
                if (actorsMovementsTimerController.getClydeSpeedDelay() != ActorsMovementsTimerController.DEATH_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.CLYDE_ENTITY_TYPE,
                        ActorsMovementsTimerController.DEATH_SPEED_DELAY
                    )
                }
            }

            pacman.energizerStatus -> {
                if (actorsMovementsTimerController.getClydeSpeedDelay() != ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.CLYDE_ENTITY_TYPE,
                        ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
                    )
                }
            }

            else -> {
                if (actorsMovementsTimerController.getClydeSpeedDelay() != ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.CLYDE_ENTITY_TYPE,
                        ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
                    )
                }
            }
        }
    }
}