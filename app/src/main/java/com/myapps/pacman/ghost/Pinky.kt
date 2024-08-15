package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
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
    movementsDelay: Long,
    standardBaseDelay: Long
) : Ghost(
    currentPosition = currentPosition,
    target = target,
    scatterTarget = scatterTarget,
    doorTarget = doorTarget,
    home = home,
    homeXRange = homeXRange,
    homeYRange = homeYRange,
    direction = direction,
    movementsDelay = movementsDelay,
    standardBaseDelay = standardBaseDelay
) {

    private fun calculateTarget(pacman: Pacman) {
        when (pacman.direction) {
            Direction.RIGHT -> {
                target = pacman.currentPosition.copy(positionY = pacman.currentPosition.positionY + 4)
            }

            Direction.UP -> {
                target = pacman.currentPosition.copy(positionX = pacman.currentPosition.positionX - 4)
            }

            Direction.DOWN -> {
                target = pacman.currentPosition.copy(positionX = pacman.currentPosition.positionX + 4)
            }

            Direction.LEFT -> {
                target = pacman.currentPosition.copy(positionY = pacman.currentPosition.positionY - 4)
            }

            Direction.NOWHERE -> {
                target = pacman.currentPosition
            }
        }
    }

    fun updatePosition(currentMap: Matrix<Char>, pacman: Pacman, ghostMode: GhostMode,onPacmanCollision:(Ghost,Pacman)->Unit){
        this.updateStatus(pacman,ghostMode)
        if(isTargetToCalculate(pacman)){
            calculateTarget(pacman)
        }
        calculateDirections(currentMap)
        this.move(this.direction)
        onPacmanCollision(this,pacman)
        if(this.checkTransfer(currentPosition,direction,currentMap)){
            onPacmanCollision(this,pacman)
        }
    }


}