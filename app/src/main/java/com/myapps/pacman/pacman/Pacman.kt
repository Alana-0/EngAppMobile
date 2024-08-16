package com.myapps.pacman.pacman

import com.myapps.pacman.ghost.Ghost
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.TypeOfCollision
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.Delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Pacman(
    var direction: Direction = Direction.RIGHT,
    var lifeStatement: Boolean = true,
    var energizerStatus: Boolean = false,
    var currentPosition: Position,
    var movementsDelay: Long
) {

    fun updatePosition(
        movements: MutableList<Direction>,
        currentMap: Matrix<Char>,
        onFoodCollision: (Position, TypeOfCollision) -> Unit,
        onGhostCollision: (Pacman) -> Unit
    ) {
        var position = getPacmanPossiblePosition(currentPosition, movements[0])

        if (!isWallCollision(position, currentMap)) {
            this.move(movements[0])
            this.direction = movements[0]
            onFoodCollision(
                this.currentPosition,
                checkFoodCollisions(this.currentPosition, currentMap)
            )
            onGhostCollision(this)
            if(this.checkTransfer(this.currentPosition, this.direction, currentMap)){
                onFoodCollision(
                    this.currentPosition,
                    checkFoodCollisions(this.currentPosition, currentMap)
                )
                onGhostCollision(this)
            }
        }

        if (movements.size > 1 && movements[0] != movements[1]) {
            position = getPacmanPossiblePosition(currentPosition, movements[1])
            if (!isWallCollision(position, currentMap)) {
                this.move(movements[1])
                this.direction = movements[1]
                onFoodCollision(
                    this.currentPosition,
                    checkFoodCollisions(this.currentPosition, currentMap)
                )
                onGhostCollision(this)
                if(this.checkTransfer(this.currentPosition, this.direction, currentMap)){
                    onFoodCollision(
                        this.currentPosition,
                        checkFoodCollisions(this.currentPosition, currentMap)
                    )
                    onGhostCollision(this)
                }
                movements.removeAt(0)
            }
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
        return elementPosition == '|' || elementPosition == '='
    }

    private fun checkTransfer(
        position: Position,
        direction: Direction,
        currentMap: Matrix<Char>
    ): Boolean = when (direction) {
            Direction.RIGHT -> {
                if (position.positionY == currentMap.getColumns()) {
                    currentPosition = currentPosition.copy(positionY = 0)
                    true
                } else false
            }

            Direction.LEFT -> {
                if (position.positionY == -1) {
                    currentPosition = currentPosition.copy(positionY = currentMap.getColumns() - 1)
                    true
                } else false
            }

            Direction.UP -> {
                false
            }

            Direction.DOWN -> {
                false
            }

            Direction.NOWHERE -> {
                false
            }
        }

    fun move(direction: Direction) {
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


    private fun checkFoodCollisions(
        pacmanPosition: Position,
        gameMap: Matrix<Char>
    ): TypeOfCollision {
        val element = gameMap.getElementByPosition(
            pacmanPosition.positionX,
            pacmanPosition.positionY
        )
        return when (element) {
            '.' -> TypeOfCollision.PELLET
            'o' -> TypeOfCollision.ENERGIZER
            'b' -> TypeOfCollision.BELL
            else -> TypeOfCollision.NONE
        }
    }


}