package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.utils.Position
import kotlinx.coroutines.Delay

class Blinky(
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
        this.target = pacman.currentPosition
    }

    fun updatePosition(currentMap: Matrix<Char>, pacman: Pacman, ghostMode: GhostMode, onPacmanCollision:(Ghost,Pacman)->Unit){
        this.updateStatus(pacman,ghostMode)
        if(isTargetToCalculate(pacman)){
            this.calculateTarget(pacman)
        }
        calculateDirections(currentMap)
        this.move(direction)
        onPacmanCollision(this,pacman)
        if(this.checkTransfer(currentPosition,direction,currentMap)){
            onPacmanCollision(this,pacman)
        }
    }

}
