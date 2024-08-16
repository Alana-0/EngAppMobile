package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Position

class Blinky(
    currentPosition: Position,
    target: Position,
    scatterTarget: Position,
    doorTarget: Position,
    home: Position,
    homeXRange: IntRange,
    homeYRange: IntRange,
    direction: Direction,
    var blinkyStandardSpeedDelay: Int,
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
        this.target = pacman.currentPosition
    }

    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: () -> GhostMode,
        onPacmanCollision: (Ghost, Pacman) -> Boolean,
        onUpdatingMoveAndDirection: () -> Unit
    ) {
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.BLINKY_ENTITY_TYPE) {
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
    ):Boolean{
        this.updateStatus(pacman, ghostMode)
        if (isTargetToCalculate(pacman)) {
            this.calculateTarget(pacman)
        }
        calculateDirections(currentMap)
        this.move(this.direction)
        var collisionProduced: Boolean = onPacmanCollision(this, pacman)
        updateSpeedDelay(pacman)
        onUpdatingMoveAndDirection()
        if (!collisionProduced && this.checkTransfer(currentPosition, direction, currentMap)){
            collisionProduced = onPacmanCollision(this, pacman)
            updateSpeedDelay(pacman)
            onUpdatingMoveAndDirection()
        }
        return collisionProduced
    }

    private fun updateSpeedDelay(pacman: Pacman) {
        when {
            !this.lifeStatement -> {
                if (actorsMovementsTimerController.getBlinkySpeedDelay() != ActorsMovementsTimerController.DEATH_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.DEATH_SPEED_DELAY
                    )
                }
            }

            pacman.energizerStatus -> {
                if (actorsMovementsTimerController.getBlinkySpeedDelay() != ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                        ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
                    )
                }
            }

            else -> {
                if (actorsMovementsTimerController.getBlinkySpeedDelay() != blinkyStandardSpeedDelay) {
                    actorsMovementsTimerController.setActorSpeedFactor(
                        ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                        blinkyStandardSpeedDelay
                    )
                }
            }
        }
    }
}
