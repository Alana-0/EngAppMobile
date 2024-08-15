package com.myapps.pacman

import com.myapps.pacman.flowData.BoardData
import com.myapps.pacman.ghost.Blinky
import com.myapps.pacman.ghost.Clyde
import com.myapps.pacman.ghost.Ghost
import com.myapps.pacman.ghost.GhostMode
import com.myapps.pacman.ghost.Inky
import com.myapps.pacman.ghost.Pinky
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.sound.SoundService
import com.myapps.pacman.timer.TimeFlow
import com.myapps.pacman.timer.Timer
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.LevelStartData
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.TypeOfCollision
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.utils.transformIntoCharMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class Game(private var gameData: List<LevelStartData>) {

    //game coroutines
    private val scope = CoroutineScope(Dispatchers.Default)
    private var gameJob: Job? = null
    private var sirenSound: Job? = null
    private var pacmanMovementJob: Job? = null
    private var blinkyMovementsJob: Job? = null
    private var inkyMovementsJob: Job? = null
    private var pinkyMovementsJob: Job? = null
    private var clydeMovementsJob: Job? = null

    //game variables
    private var currentLevel = 0
    private var isGameLose = false
    private var isGameWin = false
    private var standardGhostDelay = 200L
    private var scatterTime = 7
    private var chaseTime = 20
    private var energizerTime = 6
    private var fruitTime = 10
    private var ghostTimerTarget = scatterTime
    private var ghostMode = GhostMode.SCATTER
    private var pacmanLives = 3
    private var bellsEaten = 0
    private var counterEatingGhost = 0
    private var scorer = 0
    private var pacmanSpeedDelay = 200L
    private var dots = gameData[currentLevel].amountOfFood
    private var isBellAppear = false

    //game Timers
    private var ghostTimer = Timer()
    private var energizerTimer = Timer()
    private var bellTimer = Timer()

    // game map
    private var gameMap: Matrix<Char> =
        transformIntoCharMatrix(gameData[0].mapCharData, gameData[0].height, gameData[0].width)

    //game Actors
    private val pacman =
        Pacman(currentPosition = gameData[0].pacmanDefaultPosition, movementsDelay = 200L)
    private val blinky = Blinky(
        currentPosition = gameData[0].blinkyDefaultPosition,
        target = Position(0, 0),
        scatterTarget = gameData[0].blinkyScatterPosition,
        doorTarget = gameData[0].doorTarget,
        home = gameData[0].homeTargetPosition,
        homeXRange = gameData[0].ghostHomeXRange,
        homeYRange = gameData[0].ghostHomeYRange,
        direction = Direction.NOWHERE,
        gameData[0].blinkySpeedDelay,
        gameData[0].blinkySpeedDelay
    )
    private val inky = Inky(
        currentPosition = gameData[0].inkyDefaultPosition,
        target = Position(0, 0),
        scatterTarget = gameData[0].inkyScatterPosition,
        doorTarget = gameData[0].doorTarget,
        home = gameData[0].homeTargetPosition,
        homeXRange = gameData[0].ghostHomeXRange,
        homeYRange = gameData[0].ghostHomeYRange,
        direction = Direction.NOWHERE,
        200L,
        standardGhostDelay
    )
    private val pinky = Pinky(
        currentPosition = gameData[0].pinkyDefaultPosition,
        target = Position(0, 0),
        scatterTarget = gameData[0].pinkyScatterPosition,
        doorTarget = gameData[0].doorTarget,
        home = gameData[0].homeTargetPosition,
        homeXRange = gameData[0].ghostHomeXRange,
        homeYRange = gameData[0].ghostHomeYRange,
        direction = Direction.NOWHERE,
        200L,
        standardGhostDelay
    )
    private val clyde = Clyde(
        currentPosition = gameData[0].clydeDefaultPosition,
        target = Position(0, 0),
        scatterTarget = gameData[0].clydeScatterPosition,
        doorTarget = gameData[0].doorTarget,
        home = gameData[0].homeTargetPosition,
        homeXRange = gameData[0].ghostHomeXRange,
        homeYRange = gameData[0].ghostHomeYRange,
        direction = Direction.NOWHERE,
        200L,
        standardGhostDelay
    )


    // flows to be attached
    val mapFlow = flow {
        emit(
            BoardData(
                gameMap,
                scorer,
                pacmanLives,
                isBell = if (isBellAppear) gameData[currentLevel].isBell else false,
                currentLevel = currentLevel
            )
        )
        delay(6000)
        while (true) {
            emit(
                BoardData(
                    gameMap,
                    scorer,
                    pacmanLives,
                    isBell = if (isBellAppear) gameData[currentLevel].isBell else false,
                    currentLevel = currentLevel
                )
            )
            delay(16)
        }
    }.flowOn(Dispatchers.Default)
    val blinkyPosition = flow {
        emit(blinky)
        delay(6000)
        while (true) {
            emit(blinky)
            delay(16)
        }
    }.flowOn(Dispatchers.Default)
    val pinkyPosition = flow {
        emit(pinky)
        delay(6000)
        while (true) {
            emit(pinky)
            delay(16)
        }
    }.flowOn(Dispatchers.Default)
    val inkyPosition = flow {
        emit(inky)
        delay(6000)
        while (true) {
            emit(inky)
            delay(16)
        }
    }.flowOn(Dispatchers.Default)
    val clydePosition = flow {
        emit(clyde)
        delay(6000)
        while (true) {
            emit(clyde)
            delay(16)
        }
    }.flowOn(Dispatchers.Default)
    val pacmanPosition = flow {
        emit(pacman)
        delay(6000)
        while (true) {
            emit(pacman)
            delay(16)
        }
    }.flowOn(Dispatchers.Default)


    fun initGame(movements: MutableList<Direction>) {
        gameJob = CoroutineScope(Dispatchers.Default).launch {
            TimeFlow.init()
            configureStartGame()
            delay(2000)
            SoundService.playSound(R.raw.pacman_intro, false, 1f, 0.5f, 0.5f)
            delay(4000)
            reproduceSirenSound()
            ghostTimer.start()
            startActorsMovements(movements)
            while (isActive && !isGameLose && !isGameWin) {
                clockManagement()
                checkPacmanDeath(movements)
                checkBellAppear()
                isGameWin = checkWin()
                loadNextLevel(movements)
                delay(16)
            }
            stopActorsMovements()
            pauseSirenSound()
            SoundService.stopSound(R.raw.pacman_intro)
            SoundService.stopSound(R.raw.pacman_energizer_mode)
            TimeFlow.stop()
        }
    }

    fun stopGame() {
        pauseSirenSound()
        SoundService.stopSound(R.raw.pacman_intro)
        SoundService.stopSound(R.raw.pacman_energizer_mode)
        ghostTimer.reset()
        energizerTimer.reset()
        bellTimer.reset()
        stopActorsMovements()
        TimeFlow.stop()
        gameJob?.cancel()
        gameJob = null
    }

    private fun configureStartGame() {
        ghostTimerTarget = scatterTime
        pacman.lifeStatement = true
        pacman.direction = Direction.RIGHT
        isBellAppear = false
        isGameLose = false
        isGameWin = false
        currentLevel = 0
        pacmanLives = 3
        bellsEaten = 0
        scorer = 0
        dots = gameData[currentLevel].amountOfFood
        configureGhostAndPacmanLevelDefaults(currentLevel)
        gameMap =
            transformIntoCharMatrix(gameData[0].mapCharData, gameData[0].height, gameData[0].width)
        resetPositions(currentLevel)
    }

    private fun clearMovements(movements: MutableList<Direction>) {
        movements.clear()
        movements.add(Direction.RIGHT)
    }


    private suspend fun checkPacmanDeath(movements: MutableList<Direction>) {
        if (pacmanLives == 0) {
            isGameLose = true
            return
        }
        if (pacmanLives > 0 && !pacman.lifeStatement) {
            pauseSirenSound()
            delay(500)
            SoundService.playSound(
                R.raw.pacman_death,
                false,
                1f,
                leftVolume = 0.5f,
                rightVolume = 0.5f
            )
            delay(2000)
            resetPositions(currentLevel)
            resetGhostFacing()
            clearMovements(movements)
            pacman.direction = Direction.RIGHT
            pacman.lifeStatement = true
            delay(4000)
            startActorsMovements(movements)
            reproduceSirenSound()
        }
    }

    private fun stopActorsMovements() {
        pacmanMovementJob?.cancel()
        blinkyMovementsJob?.cancel()
        inkyMovementsJob?.cancel()
        pinkyMovementsJob?.cancel()
        clydeMovementsJob?.cancel()
        pacmanMovementJob = null
        blinkyMovementsJob = null
        inkyMovementsJob = null
        pinkyMovementsJob = null
        clydeMovementsJob = null
    }

    private fun startActorsMovements(movements: MutableList<Direction>) {
        blinkyMovementsJob = scope.launch {
            while (isActive) {
                blinky.updatePosition(gameMap, pacman, ghostMode) { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
                delay(blinky.movementsDelay)
            }
        }
        inkyMovementsJob = scope.launch {
            while (isActive) {
                inky.updatePosition(
                    gameMap,
                    pacman,
                    ghostMode,
                    blinky.currentPosition
                ) { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
                delay(inky.movementsDelay)
            }
        }
        pinkyMovementsJob = scope.launch {
            while (isActive) {
                pinky.updatePosition(gameMap, pacman, ghostMode) { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
                delay(pinky.movementsDelay)
            }
        }
        clydeMovementsJob = scope.launch {
            while (isActive) {
                clyde.updatePosition(gameMap, pacman, ghostMode) { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
                delay(clyde.movementsDelay)
            }
        }
        pacmanMovementJob = scope.launch {
            while (isActive) {
                updatePacmanPosition(movements)
                delay(pacman.movementsDelay)
            }
        }
    }

    private suspend fun loadNextLevel(movements: MutableList<Direction>) {
        if (dots == 0 && !isGameWin && !isGameLose) {
            stopActorsMovements()
            pauseSirenSound()
            ghostTimer.reset()
            ghostTimerTarget = scatterTime
            SoundService.stopSound(R.raw.pacman_energizer_mode)
            delay(2000)
            currentLevel += 1
            gameMap = transformIntoCharMatrix(
                gameData[currentLevel].mapCharData,
                gameData[currentLevel].height,
                gameData[currentLevel].width
            )
            dots = gameData[currentLevel].amountOfFood
            isBellAppear = false
            resetPositions(currentLevel)
            configureGhostAndPacmanLevelDefaults(currentLevel)
            clearMovements(movements)
            pacman.direction = Direction.RIGHT
            SoundService.playSound(R.raw.pacman_intro, false, 1f, 0.5f, 0.5f)
            delay(4000)
            reproduceSirenSound()
            ghostTimer.start()
            startActorsMovements(movements)
        }
    }

    private fun checkWin(): Boolean {
        if (dots == 0 && currentLevel == 9) return true
        return false
    }

    private fun updatePacmanPosition(movements: MutableList<Direction>) {
        pacman.updatePosition(
            movements,
            gameMap,
            onGhostCollision = { pacman -> onGhostCollision(pacman) },
            onFoodCollision = { position, typeOfCollision ->
                onPacmanFoodCollision(position, typeOfCollision)
            }
        )
    }

    private fun onPacmanFoodCollision(position: Position, typeOfCollision: TypeOfCollision) {
        when (typeOfCollision) {
            TypeOfCollision.PELLET -> {
                gameMap.insertElement(' ', position.positionX, position.positionY)
                SoundService.playSound(
                    R.raw.pacman_eating_pellet,
                    false,
                    0.9f,
                    leftVolume = 0.05f,
                    rightVolume = 0.05f
                )
                dots -= 1
                scorer += 10
            }

            TypeOfCollision.ENERGIZER -> {
                gameMap.insertElement(' ', position.positionX, position.positionY)
                pacman.energizerStatus = true
                SoundService.playSound(R.raw.pacman_energizer_mode, false, 1.0f, 0.1f, 0.1f)
                pauseSirenSound()
                energizerTimer.start()
                blinky.movementsDelay = 500L
                inky.movementsDelay = 500L
                pinky.movementsDelay = 500L
                clyde.movementsDelay = 500L
                ghostTimer.pause()
                dots -= 1
                scorer += 50
            }

            TypeOfCollision.BELL -> {
                gameMap.insertElement(' ', position.positionX, position.positionY)
                SoundService.playSound(R.raw.pacman_eating_fruit, false, 1.0f, 0.5f, 0.5f)
                pacmanSpeedDelay -= 10
                scorer += 200
                pacman.movementsDelay = pacmanSpeedDelay
            }

            TypeOfCollision.NONE -> {}
        }
    }

    private fun reproduceSirenSound() {
        sirenSound = scope.launch {
            while (isActive) {
                SoundService.playSound(R.raw.ghost_siren, false, 0.8f, 0.05f, 0.05f)
                delay(300)
            }
        }
    }

    private fun pauseSirenSound() {
        sirenSound?.cancel()
        sirenSound = null
    }

    private fun onGhostCollision(
        pacman: Pacman
    ) {
        if (pacman.energizerStatus) {
            if (blinky.lifeStatement && blinky.currentPosition == pacman.currentPosition) {
                blinky.lifeStatement = false
                blinky.movementsDelay = 100L
                SoundService.playSound(R.raw.pacman_eatghost, false, 1.0f, 0.1f, 0.1f)
                counterEatingGhost++
                scorer += calculateEatGhostScorer()
                return
            }
            if (inky.lifeStatement && inky.currentPosition == pacman.currentPosition) {
                inky.lifeStatement = false
                inky.movementsDelay = 100L
                SoundService.playSound(R.raw.pacman_eatghost, false, 1.0f, 0.1f, 0.1f)
                counterEatingGhost++
                scorer += calculateEatGhostScorer()
                return
            }
            if (pinky.lifeStatement && pinky.currentPosition == pacman.currentPosition) {
                pinky.lifeStatement = false
                pinky.movementsDelay = 100L
                SoundService.playSound(R.raw.pacman_eatghost, false, 1.0f, 0.1f, 0.1f)
                counterEatingGhost++
                scorer += calculateEatGhostScorer()
                return
            }
            if (clyde.lifeStatement && clyde.currentPosition == pacman.currentPosition) {
                clyde.lifeStatement = false
                clyde.movementsDelay = 100L
                SoundService.playSound(R.raw.pacman_eatghost, false, 1.0f, 0.1f, 0.1f)
                counterEatingGhost++
                scorer += calculateEatGhostScorer()
                return
            }
        } else {
            if (blinky.currentPosition == pacman.currentPosition && blinky.lifeStatement) {
                if (pacmanLives != 0) pacmanLives--
                pacman.lifeStatement = false
                stopActorsMovements()
                return
            }
            if (inky.currentPosition == pacman.currentPosition && inky.lifeStatement) {
                if (pacmanLives != 0) pacmanLives--
                pacman.lifeStatement = false
                stopActorsMovements()
                return
            }
            if (pinky.currentPosition == pacman.currentPosition && pinky.lifeStatement) {
                if (pacmanLives != 0) pacmanLives--
                pacman.lifeStatement = false
                stopActorsMovements()
                return
            }
            if (clyde.currentPosition == pacman.currentPosition && clyde.lifeStatement) {
                if (pacmanLives != 0) pacmanLives--
                pacman.lifeStatement = false
                stopActorsMovements()
                return
            }
        }
    }

    private fun onPacmanCollision(ghost: Ghost, pacman: Pacman) {
        if (ghost.currentPosition != pacman.currentPosition) {
            return
        } else {
            if (!pacman.energizerStatus) {
                if (ghost.lifeStatement) {
                    if (pacmanLives != 0) pacmanLives--
                    pacman.lifeStatement = false
                    stopActorsMovements()
                }
            } else {
                if (ghost.lifeStatement) {
                    ghost.lifeStatement = false
                    ghost.movementsDelay = 100L
                    SoundService.playSound(R.raw.pacman_eatghost, false, 1.0f, 0.1f, 0.1f)
                }
            }
        }
    }

    private fun resetGhostFacing() {
        blinky.direction = Direction.NOWHERE
        inky.direction = Direction.NOWHERE
        pinky.direction = Direction.NOWHERE
        clyde.direction = Direction.NOWHERE
    }

    private fun resetPositions(currentLevel: Int) {
        pacman.currentPosition = gameData[currentLevel].pacmanDefaultPosition
        blinky.currentPosition = gameData[currentLevel].blinkyDefaultPosition
        inky.currentPosition = gameData[currentLevel].inkyDefaultPosition
        pinky.currentPosition = gameData[currentLevel].pinkyDefaultPosition
        clyde.currentPosition = gameData[currentLevel].clydeDefaultPosition
    }

    private fun clockManagement() {
        if (ghostTimer.getTicks() > ghostTimerTarget) {
            if (ghostTimerTarget == scatterTime) {
                ghostMode = GhostMode.CHASE
                ghostTimerTarget = chaseTime
                ghostTimer.restart()
            } else {
                if (ghostTimerTarget == chaseTime) {
                    ghostMode = GhostMode.SCATTER
                    ghostTimerTarget = scatterTime
                    ghostTimer.restart()
                }
            }
        }
        if (energizerTimer.getTicks() > energizerTime) {
            pacman.energizerStatus = false
            counterEatingGhost = 0
            if (blinky.lifeStatement) blinky.movementsDelay = blinky.standardBaseDelay
            if (inky.lifeStatement) inky.movementsDelay = inky.standardBaseDelay
            if (pinky.lifeStatement) pinky.movementsDelay = pinky.standardBaseDelay
            if (clyde.lifeStatement) clyde.movementsDelay = clyde.standardBaseDelay
            SoundService.stopSound(R.raw.pacman_energizer_mode)
            reproduceSirenSound()
            ghostTimer.unpause()
            energizerTimer.reset()
        }
        if (bellTimer.getTicks() > fruitTime) {
            gameMap.insertElement(
                ' ',
                gameData[currentLevel].pacmanDefaultPosition.positionX,
                gameData[currentLevel].pacmanDefaultPosition.positionY
            )
            bellTimer.reset()
        }
    }

    private fun calculateEatGhostScorer(): Int =
        when (counterEatingGhost) {
            1 -> 200
            2 -> 400
            3 -> 800
            4 -> 1600
            else -> 0
        }

    private fun configureGhostAndPacmanLevelDefaults(currentLevel: Int) {
        ghostMode = GhostMode.SCATTER
        pacman.movementsDelay = pacmanSpeedDelay()
        blinky.scatterTarget = gameData[currentLevel].blinkyScatterPosition
        inky.scatterTarget = gameData[currentLevel].inkyScatterPosition
        pinky.scatterTarget = gameData[currentLevel].pinkyScatterPosition
        clyde.scatterTarget = gameData[currentLevel].clydeScatterPosition
        blinky.home = gameData[currentLevel].homeTargetPosition
        inky.home = gameData[currentLevel].homeTargetPosition
        pinky.home = gameData[currentLevel].homeTargetPosition
        clyde.home = gameData[currentLevel].homeTargetPosition
        blinky.homeXRange = gameData[currentLevel].ghostHomeXRange
        blinky.homeYRange = gameData[currentLevel].ghostHomeYRange
        blinky.movementsDelay = gameData[currentLevel].blinkySpeedDelay
        blinky.standardBaseDelay = gameData[currentLevel].blinkySpeedDelay
        inky.homeXRange = gameData[currentLevel].ghostHomeXRange
        inky.homeYRange = gameData[currentLevel].ghostHomeYRange
        pinky.homeXRange = gameData[currentLevel].ghostHomeXRange
        pinky.homeYRange = gameData[currentLevel].ghostHomeYRange
        clyde.homeXRange = gameData[currentLevel].ghostHomeXRange
        clyde.homeYRange = gameData[currentLevel].ghostHomeYRange
        pinky.doorTarget = gameData[currentLevel].doorTarget
        inky.doorTarget = gameData[currentLevel].doorTarget
        blinky.doorTarget = gameData[currentLevel].doorTarget
        clyde.doorTarget = gameData[currentLevel].doorTarget
    }

    private fun pacmanSpeedDelay(): Long {
        if (bellsEaten == 0) return 200L
        if (bellsEaten == 1) return 190L
        if (bellsEaten == 2) return 180L
        if (bellsEaten == 3) return 170L
        if (bellsEaten == 4) return 160L
        if (bellsEaten == 5) return 150L
        return 200L
    }

    private fun checkBellAppear() {
        if (!gameData[currentLevel].isBell) return
        if (dots > 140) return
        if (isBellAppear) return
        if (pacman.currentPosition == gameData[currentLevel].pacmanDefaultPosition) return
        if (blinky.currentPosition == gameData[currentLevel].pacmanDefaultPosition) return
        if (pinky.currentPosition == gameData[currentLevel].pacmanDefaultPosition) return
        if (inky.currentPosition == gameData[currentLevel].pacmanDefaultPosition) return
        if (clyde.currentPosition == gameData[currentLevel].pacmanDefaultPosition) return

        gameMap.insertElement(
            'b',
            gameData[currentLevel].pacmanDefaultPosition.positionX,
            gameData[currentLevel].pacmanDefaultPosition.positionY
        )
        isBellAppear = true
        bellTimer.start()
    }
}
