package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
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

    private fun calculateTarget(pacman: Pacman){
        val xRange = IntRange(pacman.currentPosition.positionX-8,pacman.currentPosition.positionX+8)
        val yRange = IntRange(pacman.currentPosition.positionY-8,pacman.currentPosition.positionY+8)

        target = if(xRange.contains(this.currentPosition.positionX) && yRange.contains(this.currentPosition.positionY)){
            scatterTarget
        } else pacman.currentPosition
    }

    fun updatePosition(currentMap: Matrix<Char>, pacman: Pacman, ghostMode: GhostMode,onPacmanCollision:(Ghost,Pacman)->Unit){
        this.updateStatus(pacman,ghostMode)
        if(isTargetToCalculate(pacman)){
            calculateTarget(pacman)
        }
        this.calculateDirections(currentMap)
        this.move(this.direction)
        onPacmanCollision(this,pacman)
        if(this.checkTransfer(currentPosition,direction,currentMap)){
            onPacmanCollision(this,pacman)
        }
    }
}