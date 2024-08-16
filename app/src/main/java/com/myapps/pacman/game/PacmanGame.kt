package com.myapps.pacman.game

import android.content.Context
import android.util.Log
import com.myapps.pacman.R
import com.myapps.pacman.game.coroutines.CoroutineSupervisor
import com.myapps.pacman.ghost.Blinky
import com.myapps.pacman.ghost.Clyde
import com.myapps.pacman.ghost.Ghost
import com.myapps.pacman.ghost.GhostMode
import com.myapps.pacman.ghost.Inky
import com.myapps.pacman.ghost.Pinky
import com.myapps.pacman.levels.LevelStartData
import com.myapps.pacman.levels.parse.loadAllMaps
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.sound.PacmanSoundService
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.timer.TimeFlow
import com.myapps.pacman.timer.Timer
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.TypeOfCollision
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.utils.transformIntoCharMatrix
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.internal.artificialFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class PacmanGame(context: Context) {

    //all this constants are in seconds
    companion object Constants {
        private const val scatterTime = 7
        private const val chaseTime = 20
        private const val energizerTime = 6
        private const val bellTime = 10
    }

    //game coroutines and main game controllers
    private val scope = CoroutineSupervisor()
    private var gameJob: Job? = null
    private var isGameStarted = false
    private var gameJobIsPaused = false
    private var pacmanMovementJob: Job? = null
    private var blinkyMovementJob: Job? = null
    private var inkyMovementJob: Job? = null
    private var pinkyMovementJob: Job? = null
    private var clydeMovementJob: Job? = null
    private var sirenSoundJob: Job? = null
    private var pauseController = PauseController()
    private var soundService = PacmanSoundService(context)

    private var levelsData: Map<Int, LevelStartData> = emptyMap()

    //game variables
    private var currentLevel = 0
    private var isGameLose = false
    private var isGameWin = false
    private var ghostTimerTarget = scatterTime
    private var ghostMode = GhostMode.SCATTER
    private var pacmanLives = 3
    private var bellsEaten = 0
    private var counterEatingGhost = 0
    private var scorer = 0
    private var pacmanSpeedDelay = 250
    private var dots = 0
    private var isBellAppear = false
    private var sirenSoundPause = PauseController()


    //game Timers (control game events)
    private var ghostTimer = Timer()
    private var energizerTimer = Timer()
    private var bellTimer = Timer()
    private var actorsMovementsTimerController = ActorsMovementsTimerController()

    // game map
    private var gameMap: Matrix<Char>

    //game Actors
    private var pacman: Pacman
    private var blinky: Blinky
    private var inky: Inky
    private var pinky: Pinky
    private var clyde: Clyde

    //these are the states to be collected
    val inkyState = MutableStateFlow(
        GhostData()
    )
    val pinkyState = MutableStateFlow(
        GhostData()
    )
    val blinkyState = MutableStateFlow(
        GhostData()
    )
    val clydeState = MutableStateFlow(
        GhostData()
    )

    val pacmanState = MutableStateFlow(
        PacmanData()
    )

    val mapBoardData = MutableStateFlow(
        BoardData()
    )



    init {
        levelsData  = loadAllMaps(context)
        dots = levelsData[0]?.amountOfFood ?: 0
        pacman = Pacman(
            currentPosition = levelsData[currentLevel]?.pacmanDefaultPosition ?: Position(
                -1,
                -1
            ),
            actorsMovementsTimerController = actorsMovementsTimerController
        )
        blinky = Blinky(
            currentPosition = levelsData[currentLevel]?.blinkyDefaultPosition ?: Position(
                -1,
                -1
            ),
            target = Position(0, 0),
            scatterTarget = levelsData[currentLevel]?.blinkyScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            levelsData[currentLevel]?.blinkySpeedDelay
                ?: ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY,
            actorsMovementsTimerController
        )

        inky = Inky(
            currentPosition = levelsData[currentLevel]?.inkyDefaultPosition ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[currentLevel]?.inkyScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )

        pinky = Pinky(
            currentPosition = levelsData[currentLevel]?.pinkyDefaultPosition ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[currentLevel]?.pinkyScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )

        clyde = Clyde(
            currentPosition = levelsData[currentLevel]?.clydeDefaultPosition ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[currentLevel]?.clydeScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )

        gameMap = transformIntoCharMatrix(
            levelsData[currentLevel]?.mapCharData ?: emptyList(),
            rows = levelsData[currentLevel]?.height ?: 0,
            columns = levelsData[currentLevel]?.width ?: 0
        )

        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap,
            scorer = scorer,
            pacmanLives = pacmanLives,
            currentLevel = currentLevel,
            isGameWin = isGameWin,
            isGameLose = isGameLose
        )

        pacmanState.value = pacmanState.value.copy(
            pacmanPosition = Pair(
                pacman.currentPosition.positionX.toFloat(),
                pacman.currentPosition.positionY.toFloat()
            ),
            speedDelay = pacmanSpeedDelay.toLong()
        )

        blinkyState.value = blinkyState.value.copy(
            ghostPosition = Pair(
                blinky.currentPosition.positionX.toFloat(),
                blinky.currentPosition.positionY.toFloat()
            ),
            ghostDelay = actorsMovementsTimerController.getBlinkySpeedDelay().toLong()
        )
        inkyState.value = inkyState.value.copy(
            ghostPosition = Pair(
                inky.currentPosition.positionX.toFloat(),
                inky.currentPosition.positionY.toFloat()
            ),
            ghostDelay = actorsMovementsTimerController.getBlinkySpeedDelay().toLong()
        )
        pinkyState.value = pinkyState.value.copy(
            ghostPosition = Pair(
                pinky.currentPosition.positionX.toFloat(),
                pinky.currentPosition.positionY.toFloat()
            ),
            ghostDelay = actorsMovementsTimerController.getBlinkySpeedDelay().toLong()
        )
        clydeState.value = clydeState.value.copy(
            ghostPosition = Pair(
                clyde.currentPosition.positionX.toFloat(),
                clyde.currentPosition.positionY.toFloat()
            ),
            ghostDelay = actorsMovementsTimerController.getBlinkySpeedDelay().toLong()
        )
    }


    private suspend fun awaitSirenSoundResume() =
        suspendCancellableCoroutine<Unit> { continuation ->
            val listener = object : PauseListener {
                override fun onPause() {}

                override fun onResume() {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
            sirenSoundPause.addListener(listener)
            continuation.invokeOnCancellation {
                sirenSoundPause.removeListener(listener)
            }
        }

    private suspend fun awaitGamePauseResume() = suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : PauseListener {
            override fun onPause() {}
            override fun onResume() {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
        pauseController.addListener(listener)
        continuation.invokeOnCancellation {
            pauseController.removeListener(listener)
        }
    }

    // call this method to start the game
    fun initGame(movements: MutableList<Direction>) {
        gameJob = scope.launch {
            try {
                TimeFlow.init()
                startGame(movements)
                while (isActive && !isGameLose && !isGameWin) {
                    val startMillis = System.currentTimeMillis()
                    if (!gameJobIsPaused) {
                        checkPacmanDeath(movements)
                        clockManagement()
                        checkBellAppear()
                        checkWin()
                        loadNextLevel(movements)
                    }
                    val frameTime = System.currentTimeMillis() - startMillis
                    val delayTime = 16 - frameTime
                    if (delayTime > 0) {
                        delay(delayTime)
                    }

                    if (gameJobIsPaused) {
                        awaitGamePauseResume()
                    }
                }
            } finally {
                ghostTimer.reset()
                energizerTimer.reset()
                bellTimer.reset()
                TimeFlow.stop()
            }

            if (isGameLose || isGameWin) {
                scope.cancelAll()
            }
        }
    }

    private suspend fun startGame(movements: MutableList<Direction>) {
        if (!isGameStarted) {
            delay(2000)
            soundService.playSound(R.raw.pacman_intro)
            delay(4000)
            ghostTimer.start()
            isGameStarted = true
            startActorsMovements(movements)
            sirenSoundStart()
        }
    }

    fun stopGame() {
        isGameStarted = false
        gameJobIsPaused = false
        actorsMovementsTimerController.resume()
        ghostTimer.reset()
        energizerTimer.reset()
        bellTimer.reset()
        TimeFlow.stop()
        scope.cancelAll()
        gameJob = null
        pacmanMovementJob = null

        resetGame()
    }

    private fun resetGame() {
        ghostTimerTarget = scatterTime
        pacman.lifeStatement = true
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
        dots = levelsData[currentLevel]?.amountOfFood ?: 0
        configureGhostAndPacmanLevelDefaults(currentLevel)
        gameMap = transformIntoCharMatrix(
            levelsData[currentLevel]?.mapCharData ?: emptyList(),
            rows = levelsData[currentLevel]?.height ?: 0,
            columns = levelsData[currentLevel]?.width ?: 0
        )

        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap,
            scorer = scorer,
            pacmanLives = pacmanLives,
            currentLevel = currentLevel,
            isGameWin = isGameWin,
            isGameLose = isGameLose
        )

        resetPositions(currentLevel)
    }

    private fun clearMovements(movements: MutableList<Direction>) {
        movements.clear()
        movements.add(Direction.RIGHT)
    }

    private suspend fun checkPacmanDeath(movements: MutableList<Direction>) {
        if (pacmanLives == 0) {
            isGameLose = true
            mapBoardData.value = mapBoardData.value.copy(
                isGameLose = isGameLose
            )
            return
        }
        if (pacmanLives > 0 && !pacman.lifeStatement) {
            sirenSoundPause.pause()
            delay(2000)
            soundService.playSound(R.raw.pacman_death)
            clearMovements(movements)
            pacman.direction = Direction.RIGHT
            pacman.lifeStatement = true
            pacmanState.value = pacmanState.value.copy(
                pacmanDirection = pacman.direction
            )
            resetGhostStatements()
            delay(3000)
            resetPositions(currentLevel)
            actorsMovementsTimerController.resume()
            sirenSoundPause.resume()
        }
    }

    private suspend fun loadNextLevel(movements: MutableList<Direction>) {
        if (dots == 0 && !isGameWin && !isGameLose) {
            actorsMovementsTimerController.pause()
            ghostTimer.reset()
            bellTimer.reset()
            sirenSoundPause.pause()
            delay(2000)
            ghostTimerTarget = scatterTime
            currentLevel += 1
            gameMap = transformIntoCharMatrix(
                levelsData[currentLevel]?.mapCharData ?: emptyList(),
                rows = levelsData[currentLevel]?.height ?: 0,
                columns = levelsData[currentLevel]?.width ?: 0
            )
            dots = levelsData[currentLevel]?.amountOfFood ?: 0
            isBellAppear = false
            configureGhostAndPacmanLevelDefaults(currentLevel)
            resetPositions(currentLevel)
            resetGhostStatements()
            clearMovements(movements)
            pacman.direction = Direction.RIGHT
            mapBoardData.value = mapBoardData.value.copy(
                gameBoardData = gameMap,
                currentLevel = currentLevel
            )
            soundService.playSound(R.raw.pacman_intro)
            delay(4000)
            ghostTimer.start()
            actorsMovementsTimerController.resume()
            sirenSoundPause.resume()
        }
    }

    private fun checkWin() {
        if (dots == 0 && currentLevel == levelsData.size - 1) {
            isGameWin = true
            mapBoardData.value = mapBoardData.value.copy(
                isGameWin = isGameWin
            )
        }
    }


    private fun startActorsMovements(movements: MutableList<Direction>) {
        pacmanMovementJob = scope.launch {
            pacman.startMoving(movements, { gameMap }, onFoodCollision = { pos, coll ->
                onPacmanFoodCollision(pos, coll)
            },
                onGhostCollision = {
                    onGhostCollision(it)
                },
                onUpdatingMoveAndDirection = {
                    pacmanState.value = pacmanState.value.copy(
                        pacmanPosition = Pair(
                            pacman.currentPosition.positionX.toFloat(),
                            pacman.currentPosition.positionY.toFloat()
                        ),
                        pacmanDirection = pacman.direction
                    )
                }
            )
        }

        blinkyMovementJob = scope.launch {
            blinky.startMoving(
                currentMap = { gameMap },
                { pacman },
                ghostMode = { ghostMode },
                onPacmanCollision = { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
            ) {
                blinkyState.value = blinkyState.value.copy(
                    ghostPosition = Pair(
                        blinky.currentPosition.positionX.toFloat(),
                        blinky.currentPosition.positionY.toFloat()
                    ),
                    ghostDirection = blinky.direction,
                    ghostLifeStatement = blinky.lifeStatement,
                    ghostDelay = actorsMovementsTimerController.getBlinkySpeedDelay().toLong()
                )
            }
        }

        inkyMovementJob = scope.launch {
            inky.startMoving(
                currentMap = { gameMap },
                { pacman },
                ghostMode = { ghostMode },
                blinkyPosition = { blinky.currentPosition },
                onPacmanCollision = { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
            ) {
                inkyState.value = inkyState.value.copy(
                    ghostPosition = Pair(
                        inky.currentPosition.positionX.toFloat(),
                        inky.currentPosition.positionY.toFloat()
                    ),
                    ghostDirection = inky.direction,
                    ghostLifeStatement = inky.lifeStatement,
                    ghostDelay = actorsMovementsTimerController.getInkySpeedDelay().toLong()
                )
            }
        }

        pinkyMovementJob = scope.launch {
            pinky.startMoving(
                currentMap = { gameMap },
                { pacman },
                ghostMode = { ghostMode },
                onPacmanCollision = { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
            ) {
                pinkyState.value = pinkyState.value.copy(
                    ghostPosition = Pair(
                        pinky.currentPosition.positionX.toFloat(),
                        pinky.currentPosition.positionY.toFloat()
                    ),
                    ghostDirection = pinky.direction,
                    ghostLifeStatement = pinky.lifeStatement,
                    ghostDelay = actorsMovementsTimerController.getPinkySpeedDelay().toLong()
                )
            }
        }

        clydeMovementJob = scope.launch {
            clyde.startMoving(
                currentMap = { gameMap },
                { pacman },
                ghostMode = { ghostMode },
                onPacmanCollision = { ghost, pacman ->
                    onPacmanCollision(ghost, pacman)
                }
            ) {
                clydeState.value = clydeState.value.copy(
                    ghostPosition = Pair(
                        clyde.currentPosition.positionX.toFloat(),
                        clyde.currentPosition.positionY.toFloat()
                    ),
                    ghostDirection = clyde.direction,
                    ghostLifeStatement = clyde.lifeStatement,
                    ghostDelay = actorsMovementsTimerController.getClydeSpeedDelay().toLong()
                )
            }
        }


    }

    private fun sirenSoundStart() {
        sirenSoundJob = scope.launch {
            while (isActive) {
                if (!sirenSoundPause.isPaused) {
                    soundService.playSound(R.raw.ghost_siren)
                }
                if (sirenSoundPause.isPaused) {
                    awaitSirenSoundResume()
                } else {
                    delay(330)
                }
            }
        }
    }

    private fun onPacmanFoodCollision(position: Position, typeOfCollision: TypeOfCollision) {
        when (typeOfCollision) {
            TypeOfCollision.PELLET -> handlePelletCollision(position)
            TypeOfCollision.ENERGIZER -> handleEnergizerCollision(position)
            TypeOfCollision.BELL -> handleBellCollision(position)
            TypeOfCollision.NONE -> {}
        }
    }

    private fun handlePelletCollision(position: Position) {
        gameMap.insertElement(' ', position.positionX, position.positionY)
        dots -= 1
        scorer += 10
        soundService.playSound(R.raw.pacman_eating_pellet)
        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap,
            scorer = scorer
        )
    }

    private fun handleEnergizerCollision(position: Position) {
        gameMap.insertElement(' ', position.positionX, position.positionY)
        pacman.energizerStatus = true
        energizerTimer.start()
        ghostTimer.pause()
        dots -= 1
        scorer += 50
        soundService.playSound(R.raw.pacman_energizer_mode)
        sirenSoundPause.pause { }
        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap,
            scorer = scorer
        )
        pacmanState.value = pacmanState.value.copy(
            energizerStatus = pacman.energizerStatus
        )
    }

    private fun handleBellCollision(position: Position) {
        gameMap.insertElement(' ', position.positionX, position.positionY)
        pacmanSpeedDelay -= 10
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.PACMAN_ENTITY_TYPE,
            pacmanSpeedDelay
        )
        soundService.playSound(R.raw.pacman_eating_fruit)
        scorer += 200
        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap,
            scorer = scorer
        )
        pacmanState.value = pacmanState.value.copy(
            speedDelay = pacmanSpeedDelay.toLong()
        )
    }

    private fun onGhostCollision(position: Position): Boolean =
        if (pacman.energizerStatus) {
            handleEnergizedPacmanCollision(position)
        } else {
            handleNormalPacmanCollision(position)
        }

    private fun handleEnergizedPacmanCollision(position: Position): Boolean {
        val ghosts = listOf(blinky, inky, pinky, clyde)
        for (ghost in ghosts) {
            if (ghost.lifeStatement && ghost.currentPosition == position) {
                ghost.lifeStatement = false
                soundService.playSound(R.raw.pacman_eatghost)
                counterEatingGhost++
                scorer += calculateEatGhostScorer()
                mapBoardData.value = mapBoardData.value.copy(
                    scorer = scorer
                )
                return false
            }
        }
        return false
    }

    private fun handleNormalPacmanCollision(position: Position): Boolean {
        val ghosts = listOf(blinky, inky, pinky, clyde)
        for (ghost in ghosts) {
            if (ghost.currentPosition == position && ghost.lifeStatement) {
                if (pacmanLives != 0) pacmanLives--
                pacman.lifeStatement = false
                mapBoardData.value = mapBoardData.value.copy(
                    pacmanLives = pacmanLives
                )
                return true
            }
        }
        return false
    }

    private fun onPacmanCollision(ghost: Ghost, pacman: Pacman): Boolean {
        if (ghost.currentPosition != pacman.currentPosition) {
            return false
        } else {
            if (!pacman.energizerStatus) {
                if (ghost.lifeStatement) {
                    if (pacmanLives != 0) pacmanLives--
                    mapBoardData.value = mapBoardData.value.copy(
                        pacmanLives = pacmanLives
                    )
                    pacman.lifeStatement = false
                    return true
                }
            } else {
                if (ghost.lifeStatement) {
                    ghost.lifeStatement = false
                    soundService.playSound(R.raw.pacman_eatghost)
                }
            }
        }
        return false
    }


    private fun resetGhostStatements() {
        blinky.direction = Direction.NOWHERE
        inky.direction = Direction.NOWHERE
        pinky.direction = Direction.NOWHERE
        clyde.direction = Direction.NOWHERE
        blinky.lifeStatement = true
        pinky.lifeStatement = true
        inky.lifeStatement = true
        clyde.lifeStatement = true
        blinkyState.value = blinkyState.value.copy(
            ghostDirection = blinky.direction,
            ghostLifeStatement = blinky.lifeStatement
        )
        pinkyState.value = pinkyState.value.copy(
            ghostDirection = pinky.direction,
            ghostLifeStatement = pinky.lifeStatement
        )
        inkyState.value = inkyState.value.copy(
            ghostDirection = inky.direction,
            ghostLifeStatement = inky.lifeStatement
        )
        clydeState.value = clydeState.value.copy(
            ghostDirection = clyde.direction,
            ghostLifeStatement = clyde.lifeStatement
        )
    }

    private fun resetPositions(currentLevel: Int) {
        pacman.currentPosition = levelsData[currentLevel]?.pacmanDefaultPosition ?: Position(-1, -1)
        blinky.currentPosition = levelsData[currentLevel]?.blinkyDefaultPosition ?: Position(-1, -1)
        inky.currentPosition = levelsData[currentLevel]?.inkyDefaultPosition ?: Position(-1, -1)
        pinky.currentPosition = levelsData[currentLevel]?.pinkyDefaultPosition ?: Position(-1, -1)
        clyde.currentPosition = levelsData[currentLevel]?.clydeDefaultPosition ?: Position(-1, -1)

        pacmanState.value = pacmanState.value.copy(
            pacmanPosition = Pair(
                pacman.currentPosition.positionX.toFloat(),
                pacman.currentPosition.positionY.toFloat()
            )
        )

        blinkyState.value = blinkyState.value.copy(
            ghostPosition = Pair(
                blinky.currentPosition.positionX.toFloat(),
                blinky.currentPosition.positionY.toFloat()
            ),
            ghostLifeStatement = true
        )
        inkyState.value = inkyState.value.copy(
            ghostPosition = Pair(
                inky.currentPosition.positionX.toFloat(),
                inky.currentPosition.positionY.toFloat()
            ),
            ghostLifeStatement = true
        )
        pinkyState.value = pinkyState.value.copy(
            ghostPosition = Pair(
                pinky.currentPosition.positionX.toFloat(),
                pinky.currentPosition.positionY.toFloat()
            ),
            ghostLifeStatement = true
        )
        clydeState.value = clydeState.value.copy(
            ghostPosition = Pair(
                clyde.currentPosition.positionX.toFloat(),
                clyde.currentPosition.positionY.toFloat()
            ),
            ghostLifeStatement = true
        )

    }

    private fun handleGhostTimer(){
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

    private fun handleEnergizerTimer(){
        pacman.energizerStatus = false
        soundService.stopSound(R.raw.pacman_energizer_mode)
        sirenSoundPause.resume()
        pacmanState.value = pacmanState.value.copy(
            energizerStatus = pacman.energizerStatus
        )
        counterEatingGhost = 0
        if (blinky.lifeStatement) {
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                levelsData[currentLevel]?.blinkySpeedDelay
                    ?: ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
            )
        }
        ghostTimer.unpause()
        energizerTimer.reset()
    }

    private fun handleBellTimer(){
        gameMap.insertElement(
            ' ',
            levelsData[currentLevel]?.pacmanDefaultPosition?.positionX ?: 0,
            levelsData[currentLevel]?.pacmanDefaultPosition?.positionY ?: 0
        )
        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap
        )
        bellTimer.reset()
    }
    private fun clockManagement() {
        if (ghostTimer.getTicks() > ghostTimerTarget) {
           handleGhostTimer()
        }
        if (energizerTimer.getTicks() > energizerTime) {
            handleEnergizerTimer()
        }
        if (bellTimer.getTicks() > bellTime) {
            handleBellTimer()
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
        pacmanSpeedDelay = pacmanSpeedDelay()
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.PACMAN_ENTITY_TYPE,
            pacmanSpeedDelay
        )
        blinky.scatterTarget = levelsData[currentLevel]?.blinkyScatterPosition ?: Position(-1, -1)
        inky.scatterTarget = levelsData[currentLevel]?.inkyScatterPosition ?: Position(-1, -1)
        pinky.scatterTarget = levelsData[currentLevel]?.pinkyScatterPosition ?: Position(-1, -1)
        clyde.scatterTarget = levelsData[currentLevel]?.clydeScatterPosition ?: Position(-1, -1)
        blinky.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        inky.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        pinky.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        clyde.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        blinky.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        blinky.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        blinky.blinkyStandardSpeedDelay = levelsData[currentLevel]?.blinkySpeedDelay ?: 0
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
            levelsData[currentLevel]?.blinkySpeedDelay ?: 0
        )
        inky.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        inky.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        pinky.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        pinky.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        clyde.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        clyde.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        pinky.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
        inky.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
        blinky.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
        clyde.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
    }

    private fun pacmanSpeedDelay(): Int {
        if (bellsEaten == 0) return 250
        if (bellsEaten == 1) return 240
        if (bellsEaten == 2) return 230
        if (bellsEaten == 3) return 220
        if (bellsEaten == 4) return 210
        if (bellsEaten == 5) return 200
        return 200
    }

    private fun checkBellAppear() {
        if (levelsData[currentLevel]?.isBell == false) return
        if (dots > 140) return
        if (isBellAppear) return
        if (pacman.currentPosition == levelsData[currentLevel]?.pacmanDefaultPosition) return
        if (blinky.currentPosition == levelsData[currentLevel]?.pacmanDefaultPosition) return
        if (pinky.currentPosition == levelsData[currentLevel]?.pacmanDefaultPosition) return
        if (inky.currentPosition == levelsData[currentLevel]?.pacmanDefaultPosition) return
        if (clyde.currentPosition == levelsData[currentLevel]?.pacmanDefaultPosition) return

        gameMap.insertElement(
            'b',
            levelsData[currentLevel]?.pacmanDefaultPosition?.positionX ?: 0,
            levelsData[currentLevel]?.pacmanDefaultPosition?.positionY ?: 0
        )
        mapBoardData.value = mapBoardData.value.copy(
            gameBoardData = gameMap
        )
        isBellAppear = true
        bellTimer.start()
    }

    fun onPause() {
        if (isGameLose) return
        if (isGameWin) return
        if (!isGameStarted) return
        pauseController.pause {
            gameJobIsPaused = true
            ghostTimer.pause()
            energizerTimer.pause()
            bellTimer.pause()
            actorsMovementsTimerController.pause()
            if (pacman.energizerStatus) soundService.pauseSound(R.raw.pacman_energizer_mode)
            if (!sirenSoundPause.isPaused) sirenSoundPause.pause()
        }
    }

    fun onResume() {
        if (!gameJobIsPaused) return
        pauseController.resume {
            gameJobIsPaused = false
            ghostTimer.unpause()
            energizerTimer.unpause()
            bellTimer.unpause()
            actorsMovementsTimerController.resume()
            if (pacman.energizerStatus) soundService.playSound(R.raw.pacman_energizer_mode)
            if (sirenSoundPause.isPaused) sirenSoundPause.resume()
        }
    }

    fun muteSounds() {
        soundService.muteSounds()
    }

    fun recoverSounds() {
        soundService.recoverSound()
    }
}


