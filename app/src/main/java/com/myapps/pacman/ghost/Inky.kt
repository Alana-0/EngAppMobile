package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
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

    private fun calculateTarget(pacman: Pacman, blinkyPosition: Position){
        var posX = pacman.currentPosition.positionX
        var posy = pacman.currentPosition.positionY
        when(pacman.direction){
            Direction.RIGHT->{
                posy+=2
            }
            Direction.LEFT->{
                posy-=2
            }
            Direction.UP->{
                posX-=2
            }
            Direction.DOWN->{
                posX+=2
            }
            Direction.NOWHERE->{}
        }

        val posX1 = posX - blinkyPosition.positionX
        val posY1 = posy - blinkyPosition.positionY

        target = target.copy(positionX = posX1, positionY = posY1)
    }


    fun updatePosition(currentMap: Matrix<Char>, pacman: Pacman, ghostMode: GhostMode, blinkyPosition: Position,onPacmanCollision:(Ghost,Pacman)->Unit){
        this.updateStatus(pacman,ghostMode)
        if(isTargetToCalculate(pacman)){
            calculateTarget(pacman,blinkyPosition)
        }
        this.calculateDirections(currentMap)
        this.move(this.direction)
        onPacmanCollision(this,pacman)
        if(this.checkTransfer(currentPosition,direction,currentMap)){
            onPacmanCollision(this,pacman)
        }
    }
}