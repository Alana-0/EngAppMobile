package com.myapps.pacman.game

import com.myapps.pacman.board.BoardController
import com.myapps.pacman.R
import com.myapps.pacman.game.coroutines.CoroutineSupervisor
import com.myapps.pacman.ghost.Blinky
import com.myapps.pacman.ghost.Clyde
import com.myapps.pacman.ghost.GhostMode
import com.myapps.pacman.ghost.Inky
import com.myapps.pacman.ghost.Pinky
import com.myapps.pacman.levels.LevelStartData
import com.myapps.pacman.levels.MapProvider
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.sound.GameSoundService
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GameStatus
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.GhostsIdentifiers
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.timer.CentralTimerController
import com.myapps.pacman.timer.ICentralTimerController
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.transformLevelsDataIntoListsOfDots
import com.myapps.pacman.utils.transformLevelsDataIntoMaps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class PacmanGame @Inject constructor(
    private val centralTimerController: ICentralTimerController,
    private val gameSoundService: GameSoundService,
    mapProvider: MapProvider,
    private val collisionHandler: ICollisionHandler,
    private val coroutineSupervisor: CoroutineSupervisor
) {
    //game coroutines and main game controllers
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

    private var levelsData: Map<Int, LevelStartData> = emptyMap()

    //game variables
    private var ghostTimerTarget = GameConstants.SCATTER_TIME
    private var ghostMode = GhostMode.SCATTER
    private var bellsEaten = 0
    private var counterEatingGhost = 0
    private var pacmanSpeedDelay = 250
    private var isBellAppear = false
    private var sirenSoundPause = PauseController()

    private var actorsMovementsTimerController = ActorsMovementsTimerController()


    //game Actors
     private var pacman: Pacman
     private var blinky: Blinky
     private var inky: Inky
     private var pinky: Pinky
     private var clyde: Clyde
     private var boardController: BoardController

    val pacmanState:StateFlow<PacmanData> get() = pacman.pacmanState
    val blinkyState:StateFlow<GhostData> get() = blinky.blinkyState
    val inkyState:StateFlow<GhostData> get() = inky.inkyState
    val pinkyState:StateFlow<GhostData> get() = pinky.pinkyState
    val clydeState:StateFlow<GhostData> get() = clyde.clydeState
    val boardState:StateFlow<BoardData> get() = boardController.boardState

    init {
        levelsData = mapProvider.getMaps()
        boardController = BoardController(
            maps = transformLevelsDataIntoMaps(levelsData),
            dots = transformLevelsDataIntoListsOfDots(levelsData)
        )
        pacman = Pacman(
            initialPosition = levelsData[boardController.boardState.value.currentLevel]?.pacmanDefaultPosition ?: Position(
                -1,
                -1
            ),
            actorsMovementsTimerController = actorsMovementsTimerController
        )
        blinky = Blinky(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.blinkyDefaultPosition ?: Position(
                -1,
                -1
            ),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.blinkyScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            levelsData[boardController.boardState.value.currentLevel]?.blinkySpeedDelay
                ?: ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY,
            actorsMovementsTimerController
        )

        inky = Inky(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.inkyDefaultPosition ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.inkyScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )

        pinky = Pinky(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.pinkyDefaultPosition ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.pinkyScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )

        clyde = Clyde(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.clydeDefaultPosition ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.clydeScatterPosition ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )


        pacman.updateSpeedDelay(pacmanSpeedDelay)
        collisionHandler.handleBellCollision = {handleBellCollision(it)}
        collisionHandler.handlePelletCollision = {handlePelletCollision(it)}
        collisionHandler.handleEnergizerCollision = {handleEnergizerCollision(it)}
        collisionHandler.handlePacmanDeath = {handlePacmanHit()}
        collisionHandler.handleGhostEaten = {handleGhostEaten(it)}

        centralTimerController.addNewTimerController(CentralTimerController.GHOST_TIMER)
        centralTimerController.addNewTimerController(CentralTimerController.ENERGIZER_TIMER)
        centralTimerController.addNewTimerController(CentralTimerController.BELL_TIMER)
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
        coroutineSupervisor.restartJob()
        gameJob = coroutineSupervisor.launch {
            try {
                centralTimerController.initTimerFunction()
                startGame(movements)
                while (isActive && boardController.boardState.value.gameStatus == GameStatus.ONGOING) {
                    val startMillis = System.currentTimeMillis()
                    if (!gameJobIsPaused) {
                        checkPacmanDeath(movements)
                        clockManagement()
                        checkBellAppear()
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
                centralTimerController.stopAllTimersController()
                centralTimerController.stopTimerFunction()
                collisionHandler.cancelCollisionObservation()
            }

            if (boardController.boardState.value.gameStatus == GameStatus.WON || boardController.boardState.value.gameStatus ==  GameStatus.LOSE) {
                coroutineSupervisor.cancelAll()
            }
        }
    }

    private suspend fun startGame(movements: MutableList<Direction>) {
        if (!isGameStarted) {
            delay(2000)
            gameSoundService.playSound(R.raw.pacman_intro)
            delay(4000)
            centralTimerController.startTimerController(CentralTimerController.GHOST_TIMER)
            isGameStarted = true
            startActorsMovements(movements)
            collisionHandler.startObservingCollisions(
                pacman.pacmanState,
                listOf(
                    blinky.blinkyState,
                    pinky.pinkyState,
                    inky.inkyState,
                    clyde.clydeState
                ),
                boardController.boardState
            )
            sirenSoundStart()
        }
    }

    fun stopGame() {
        isGameStarted = false
        gameJobIsPaused = false
        actorsMovementsTimerController.resume()
        collisionHandler.cancelCollisionObservation()
        centralTimerController.stopAllTimersController()
        coroutineSupervisor.cancelAll()
        coroutineSupervisor.onDestroy()
        gameJob = null
        pacmanMovementJob = null
        resetGame()
    }

    private fun resetGame() {
        ghostTimerTarget = GameConstants.SCATTER_TIME
        pacman.updateLifeStatement(true)
        pacman.updateDirection(Direction.RIGHT)
        isBellAppear = false
        boardController.resetBoardData()
        bellsEaten = 0
        configureGhostAndPacmanLevelDefaults(boardController.boardState.value.currentLevel)
        resetPositions(boardController.boardState.value.currentLevel)
    }

    private fun clearMovements(movements: MutableList<Direction>) {
        movements.clear()
        movements.add(Direction.RIGHT)
    }

    private suspend fun checkPacmanDeath(movements: MutableList<Direction>) {
        if (boardController.boardState.value.pacmanLives > 0 && !pacman.pacmanState.value.lifeStatement) {
            sirenSoundPause.pause()
            delay(2000)
            gameSoundService.playSound(R.raw.pacman_death)
            clearMovements(movements)
            pacman.updateDirection(Direction.RIGHT)
            pacman.updateLifeStatement(true)
            resetGhostStatements()
            delay(3000)
            resetPositions(boardController.boardState.value.currentLevel)
            actorsMovementsTimerController.resume()
            collisionHandler.resumeCollisionObservation()
            sirenSoundPause.resume()
        }
    }

    private suspend fun loadNextLevel(movements: MutableList<Direction>) {
        if (boardController.boardState.value.remainFood == 0 && boardController.boardState.value.gameStatus == GameStatus.ONGOING) {
            actorsMovementsTimerController.pause()
            centralTimerController.stopAllTimersController()
            sirenSoundPause.pause()
            delay(2000)
            ghostTimerTarget = GameConstants.SCATTER_TIME
            boardController.updateCurrentLevel()
            isBellAppear = false
            configureGhostAndPacmanLevelDefaults(boardController.boardState.value.currentLevel)
            resetPositions(boardController.boardState.value.currentLevel)
            resetGhostStatements()
            clearMovements(movements)
            pacman.updateDirection(Direction.RIGHT)
            gameSoundService.playSound(R.raw.pacman_intro)
            delay(4000)
            centralTimerController.startTimerController(CentralTimerController.GHOST_TIMER)
            actorsMovementsTimerController.resume()
            sirenSoundPause.resume()
        }
    }



    private fun startActorsMovements(movements: MutableList<Direction>) {
        pacmanMovementJob = coroutineSupervisor.launch {
            pacman.startMoving(movements) { boardController.boardState.value.gameBoardData }
        }

        blinkyMovementJob = coroutineSupervisor.launch {
            blinky.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode },
            )
        }

        inkyMovementJob = coroutineSupervisor.launch {
            inky.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode },
                blinkyPosition = { blinky.currentPosition },
            )
        }

        pinkyMovementJob = coroutineSupervisor.launch {
            pinky.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode }
            )
        }

        clydeMovementJob = coroutineSupervisor.launch {
            clyde.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode },
            )
        }
    }

    private fun sirenSoundStart() {
        sirenSoundJob = coroutineSupervisor.launch {
            while (isActive) {
                if (!sirenSoundPause.isPaused) {
                    gameSoundService.playSound(R.raw.ghost_siren)
                }
                if (sirenSoundPause.isPaused) {
                    awaitSirenSoundResume()
                } else {
                    delay(GameConstants.SIREN_DELAY)
                }
            }
        }
    }
    private fun handlePelletCollision(position: Position) {
        boardController.entityGetsEat(position,GameConstants.PELLET_POINTS)
        gameSoundService.playSound(R.raw.pacman_eating_pellet)
    }

    private fun handleEnergizerCollision(position: Position) {
        boardController.entityGetsEat(position,GameConstants.ENERGIZER_POINTS)
        pacman.updateEnergizerStatus(true)
        centralTimerController.startTimerController(CentralTimerController.ENERGIZER_TIMER)
        centralTimerController.pauseTimerController(CentralTimerController.GHOST_TIMER)
        gameSoundService.playSound(R.raw.pacman_energizer_mode)
        sirenSoundPause.pause { }
    }

    private fun handleBellCollision(position: Position) {
        boardController.entityGetsEat(position,GameConstants.BELL_POINTS)
        pacmanSpeedDelay -= GameConstants.BELL_REDUCTION_TIME
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.PACMAN_ENTITY_TYPE,
            pacmanSpeedDelay
        )
        pacman.updateSpeedDelay(actorsMovementsTimerController.getPacmanSpeedDelay())
        gameSoundService.playSound(R.raw.pacman_eating_fruit)
    }


    private fun handleGhostEaten(ghost: GhostData) {
        when(ghost.identifier){
            GhostsIdentifiers.BLINKY -> blinky.updateLifeStatement(false)
            GhostsIdentifiers.INKY -> inky.updateLifeStatement(false)
            GhostsIdentifiers.PINKY -> pinky.updateLifeStatement(false)
            GhostsIdentifiers.CLYDE -> clyde.updateLifeStatement(false)
        }
        gameSoundService.playSound(R.raw.pacman_eatghost)
        counterEatingGhost++
        boardController.updateScorer(calculateEatGhostScorer())
    }


    private fun handlePacmanHit() {
        actorsMovementsTimerController.pause()
        collisionHandler.pauseCollisionObservation()
        pacman.updateLifeStatement(false)
        boardController.decreasePacmanLives()
    }

    private fun resetGhostStatements() {
        blinky.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
        inky.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
        pinky.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
        clyde.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
    }

    private fun resetPositions(currentLevel: Int) {
        pacman.updatePosition(
            levelsData[currentLevel]?.pacmanDefaultPosition ?: Position(-1, -1)
        )
        blinky.updatePosition(
            levelsData[currentLevel]?.blinkyDefaultPosition ?: Position(-1, -1)
        )
        inky.updatePosition(
            levelsData[currentLevel]?.inkyDefaultPosition ?: Position(-1, -1)
        )
        pinky.updatePosition(
            levelsData[currentLevel]?.pinkyDefaultPosition ?: Position(-1, -1)
        )
        clyde.updatePosition(
            levelsData[currentLevel]?.clydeDefaultPosition ?: Position(-1, -1)
        )
    }

    private fun handleGhostTimer() {
        if (ghostTimerTarget == GameConstants.SCATTER_TIME) {
            ghostMode = GhostMode.CHASE
            ghostTimerTarget = GameConstants.CHASE_TIME
            centralTimerController.restartTimerController(CentralTimerController.GHOST_TIMER)
        } else {
            if (ghostTimerTarget == GameConstants.CHASE_TIME) {
                ghostMode = GhostMode.SCATTER
                ghostTimerTarget = GameConstants.SCATTER_TIME
                centralTimerController.restartTimerController(CentralTimerController.GHOST_TIMER)
            }
        }
    }

    private fun handleEnergizerTimer() {
        pacman.updateEnergizerStatus(false)
        gameSoundService.stopSound(R.raw.pacman_energizer_mode)
        sirenSoundPause.resume()
        counterEatingGhost = 0
        if (blinky.blinkyState.value.ghostLifeStatement) {
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                levelsData[boardController.boardState.value.currentLevel]?.blinkySpeedDelay
                    ?: ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
            )
            blinky.changeSpeedDelay(actorsMovementsTimerController.getBlinkySpeedDelay().toLong())
        }
        centralTimerController.unpauseTimerController(CentralTimerController.GHOST_TIMER)
        centralTimerController.stopTimerController(CentralTimerController.ENERGIZER_TIMER)
    }

    private fun handleBellTimer() {
        boardController.updateCurrentMap(
            levelsData[boardController.boardState.value.currentLevel]?.pacmanDefaultPosition?:Position(-1,-1),
            BoardController.EMPTY_SPACE
        )
        centralTimerController.stopTimerController(CentralTimerController.BELL_TIMER)
    }

    private fun clockManagement() {
        if (centralTimerController.getTimerTicksController(CentralTimerController.GHOST_TIMER) > ghostTimerTarget) {
            handleGhostTimer()
        }
        if (centralTimerController.getTimerTicksController(CentralTimerController.ENERGIZER_TIMER) > GameConstants.ENERGIZER_TIME) {
            handleEnergizerTimer()
        }
        if (centralTimerController.getTimerTicksController(CentralTimerController.BELL_TIMER) > GameConstants.BELL_TIME) {
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

    private fun pacmanSpeedDelay(): Int = when (bellsEaten) {
        0 -> 250
        1 -> 240
        2 -> 230
        3 -> 220
        4 -> 210
        else -> 200
    }

    private fun checkBellAppear() {
        val levelData = levelsData[boardController.boardState.value.currentLevel]
        if (levelData?.isBell != true) return

        if (boardController.boardState.value.remainFood > 140 || isBellAppear) return

        val bellPosition = levelData.pacmanDefaultPosition
        if (pacman.pacmanState.value.pacmanPosition == bellPosition ||
            blinky.blinkyState.value.ghostPosition == bellPosition ||
            pinky.pinkyState.value.ghostPosition == bellPosition ||
            inky.inkyState.value.ghostPosition == bellPosition ||
            clyde.clydeState.value.ghostPosition == bellPosition
        ) return

        boardController.updateCurrentMap(bellPosition, BoardController.BELL_CHAR)
        isBellAppear = true
        centralTimerController.startTimerController(CentralTimerController.BELL_TIMER)
    }
    fun onPause() {
        if(boardController.boardState.value.gameStatus!=GameStatus.ONGOING) return
        if (!isGameStarted) return
        pauseController.pause {
            gameJobIsPaused = true
            centralTimerController.pauseAllTimersController()
            actorsMovementsTimerController.pause()
            collisionHandler.pauseCollisionObservation()
            if (pacman.pacmanState.value.energizerStatus) gameSoundService.pauseSound(R.raw.pacman_energizer_mode)
            if (!sirenSoundPause.isPaused) sirenSoundPause.pause()
        }
    }

    fun onResume() {
        if (!gameJobIsPaused) return
        pauseController.resume {
            gameJobIsPaused = false
            centralTimerController.unpauseAllTimersController()
            actorsMovementsTimerController.resume()
            collisionHandler.resumeCollisionObservation()
            if (pacman.pacmanState.value.energizerStatus) gameSoundService.playSound(R.raw.pacman_energizer_mode)
            if (sirenSoundPause.isPaused) sirenSoundPause.resume()
        }
    }

    fun muteSounds() {
        gameSoundService.muteSounds()
    }

    fun recoverSounds() {
        gameSoundService.recoverSound()
    }


}


