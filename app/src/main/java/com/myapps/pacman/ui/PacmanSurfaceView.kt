package com.myapps.pacman.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.myapps.pacman.R
import com.myapps.pacman.board.BoardController
import com.myapps.pacman.modules.qualifiers.DispatcherDefault
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GameStatus
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.convertPositionToPair
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs


class PacmanSurfaceView(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs) ,SurfaceHolder.Callback {

    private var drawingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var isPlaying = false


    //game view elements painters
    private val pacmanPaint = createPaint(color = Color.YELLOW)
    private val scorerPaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 22f, textAlign = Paint.Align.LEFT)
    private val blinkyPaint = createPaint(color = Color.RED)
    private val pinkyPaint = createPaint(color = resources.getColor(R.color.pink, resources.newTheme()))
    private val clydePaint = createPaint(color = resources.getColor(R.color.orange, resources.newTheme()))
    private val inkyPaint = createPaint(color = resources.getColor(R.color.lightBlue, resources.newTheme()))
    private val eyeWhitePaint = createPaint(color = Color.WHITE)
    private val eyeBluePaint = createPaint(color = Color.BLUE)
    private val emptySpace = createPaint(color = Color.BLACK)
    private val frightenedGhostPaint = createPaint(color = resources.getColor(R.color.darkBlue, resources.newTheme()))
    private var wallPaint = createPaint(color = Color.BLUE, strokeWidth = 3f)
    private var food = createPaint(color = Color.WHITE, strokeWidth = 3f)
    private var energizer = createPaint(color = Color.WHITE, strokeWidth = 20f)
    private var doorPaint = createPaint(color = Color.WHITE)
    private val bellPaint = createPaint(color = Color.YELLOW, isAntiAlias = true)
    private val soundPaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 22f, textAlign = Paint.Align.LEFT)
    private val pauseScreenPaint = createPaint(color = resources.getColor(R.color.transparentBlack, resources.newTheme()))
    private val pauseMessagePaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 30f)
    private val endGameScreenPaint = createPaint(color = resources.getColor(R.color.littleTransparentBlack, resources.newTheme()))
    private val endGameMessageScreenPaint = createPaint(color = Color.YELLOW, strokeWidth = 25f, textSize = 40f)
    private val tittleMainScreen = createPaint(Color.YELLOW, textSize = 100f, textAlign = Paint.Align.CENTER, isAntiAlias = true)
    private val textMainScreenPaint = createPaint(Color.WHITE, textSize = 50f, textAlign = Paint.Align.CENTER, isAntiAlias = true)


    //game variables
    private var mapMatrix = Matrix<Char>(0, 0)
    private var scorer = 0
    private var pacmanLives = 0

    //pacman variables
    private var pacmanPosition = Pair(-1f, -1f)
    private var pacmanEnergizerState = false
    private var pacmanDirection = Direction.RIGHT
    private var mouthOpen = false

    //blinky variables
    private var blinkyPosition = Pair(-1f, -1f)
    private var blinkyDirection = Direction.NOWHERE
    private var blinkyIsAlive = true

    //pinky variables
    private var pinkyPosition = Pair(-1f, -1f)
    private var pinkyDirection = Direction.RIGHT
    private var pinkyIsAlive = true

    //inky variables
    private var inkyPosition = Pair(-1f, -1f)
    private var inkyDirection = Direction.RIGHT
    private var inkyIsAlive = true

    // clyde variables
    private var clydePosition = Pair(-1f, -1f)
    private var clydeDirection = Direction.RIGHT
    private var clydeIsAlive = true

    private var isGamePaused = false
    private var gameState = GameStatus.ONGOING
    private var soundIsMuted = false

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var collectMapDataJob: Job? = null
    private var collectPacmanDataJob: Job? = null
    private var collectPinkyDataJob: Job? = null
    private var collectBlinkyDataJob: Job? = null
    private var collectInkyDataJob: Job? = null
    private var collectClydeDataJob: Job? = null


    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if(!isGamePaused){
                mouthOpen = !mouthOpen
            }
            invalidate()
            handler.postDelayed(this, 200)
        }
    }


    init {
        handler.post(runnable)
        holder.addCallback(this)
    }

    private fun startDrawing() {
        drawingJob = coroutineScope.launch(Dispatchers.Default) {
            while (isActive) {
                val canvas: Canvas? = holder.lockCanvas()
                if (canvas != null) {
                    try {
                        synchronized(holder) {
                            if(isPlaying){
                                drawGameElements(canvas)
                            }
                            else{
                                drawStartScreen(canvas, width = width.toFloat(),height.toFloat())
                            }
                        }
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
                delay(16)
            }
        }
    }


    fun setGameBoardData(gameBoardData: StateFlow<BoardData>) {
        collectMapDataJob?.cancel()
        collectMapDataJob = coroutineScope.launch {
            gameBoardData.collect {
                mapMatrix = it.gameBoardData
                scorer = it.score
                pacmanLives = it.pacmanLives
                gameState = it.gameStatus
                wallPaint.color = when (it.currentLevel) {
                    0 -> Color.BLUE
                    1 -> resources.getColor(R.color.levelOne, resources.newTheme())
                    2 -> resources.getColor(R.color.levelTwo, resources.newTheme())
                    3 -> resources.getColor(R.color.levelThree, resources.newTheme())
                    4 -> resources.getColor(R.color.levelFour, resources.newTheme())
                    5 -> resources.getColor(R.color.levelFive, resources.newTheme())
                    6 -> resources.getColor(R.color.levelSix, resources.newTheme())
                    7 -> resources.getColor(R.color.levelSeven, resources.newTheme())
                    8 -> resources.getColor(R.color.levelEight, resources.newTheme())
                    9 -> resources.getColor(R.color.levelNine, resources.newTheme())
                    else -> Color.BLUE
                }
               // invalidate()
            }
        }
    }

    fun setPacmanData(pacmanData: StateFlow<PacmanData>) {
        collectPacmanDataJob?.cancel()
        collectPacmanDataJob = coroutineScope.launch {
            pacmanData.collect {
                pacmanEnergizerState = it.energizerStatus
                pacmanDirection = it.pacmanDirection
                val pairPosition = convertPositionToPair(it.pacmanPosition)
                if (shouldAnimate(pacmanPosition, pairPosition)) {
                    animateActorTo(
                        pacmanPosition,
                        pairPosition,
                        pacmanDirection,
                        it.speedDelay
                    ) { pair ->
                        pacmanPosition = pair
                    }
                } else {
                    pacmanPosition = pairPosition
                    invalidate()
                }
            }
        }
    }

    fun setBlinkyData(blinkyData: StateFlow<GhostData>) {
        collectBlinkyDataJob?.cancel()
        collectBlinkyDataJob = coroutineScope.launch {
            blinkyData.collect {
                blinkyDirection = it.ghostDirection
                blinkyIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(blinkyPosition, pairPosition)) {
                    animateActorTo(
                        blinkyPosition,
                        pairPosition,
                        blinkyDirection,
                        it.ghostDelay
                    ) { pair ->
                        blinkyPosition = pair
                    }
                } else {
                    blinkyPosition = pairPosition
                   // invalidate()
                }
            }
        }
    }

    fun setInkyData(inkyData: StateFlow<GhostData>) {
        collectInkyDataJob?.cancel()
        collectInkyDataJob = coroutineScope.launch {
            inkyData.collect {
                inkyDirection = it.ghostDirection
                inkyIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(inkyPosition, pairPosition)) {
                    animateActorTo(
                        inkyPosition,
                        pairPosition,
                        inkyDirection,
                        it.ghostDelay
                    ) { pair ->
                        inkyPosition = pair
                    }
                } else {
                    inkyPosition = pairPosition
                   // invalidate()
                }
            }
        }
    }

    fun setPinkyData(pinkyData: StateFlow<GhostData>) {
        collectPinkyDataJob?.cancel()
        collectPinkyDataJob = coroutineScope.launch {
            pinkyData.collect {
                pinkyDirection = it.ghostDirection
                pinkyIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(pinkyPosition, pairPosition)) {
                    animateActorTo(
                        pinkyPosition,
                        pairPosition,
                        pinkyDirection,
                        it.ghostDelay
                    ) { pair ->
                        pinkyPosition = pair
                    }
                } else {
                    pinkyPosition = pairPosition
                  //  invalidate()
                }
            }
        }
    }

    fun setClydeData(clydeData: StateFlow<GhostData>) {
        collectClydeDataJob?.cancel()
        collectClydeDataJob = coroutineScope.launch {
            clydeData.collect {
                clydeDirection = it.ghostDirection
                clydeIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(clydePosition, pairPosition)) {
                    animateActorTo(
                        startPosition = clydePosition,
                        targetPosition = pairPosition,
                        direction = clydeDirection,
                        speedDelay = it.ghostDelay
                    ) { pair ->
                        clydePosition = pair
                    }
                } else {
                    clydePosition = pairPosition
                   // invalidate()
                }
            }
        }
    }

    fun stopAllCurrentJobs() {
        collectMapDataJob?.let { if(it.isActive) it.cancel()}
        collectPacmanDataJob?.let { if(it.isActive) it.cancel()}
        collectBlinkyDataJob?.let { if(it.isActive) it.cancel()}
        collectInkyDataJob?.let { if(it.isActive) it.cancel()}
        collectClydeDataJob?.let { if(it.isActive) it.cancel()}
        collectPinkyDataJob?.let { if(it.isActive) it.cancel()}
    }

    fun stopDrawJob(){
        drawingJob?.let { if(it.isActive) it.cancel()}
    }

    private fun animateActorTo(
        startPosition: Pair<Float, Float>,
        targetPosition: Pair<Float, Float>,
        direction: Direction,
        speedDelay: Long,
        updatePosition: (Pair<Float, Float>) -> Unit,
    ) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (speedDelay.times(0.9)).toLong()

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val newPosition =
                    linearInterpolation(
                        startPosition,
                        targetPosition,
                        progress,
                        direction
                    )
                updatePosition(newPosition)
               // invalidate()
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    updatePosition(targetPosition)
                   // invalidate()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        animator.start()
    }

    private fun linearInterpolation(
        startPosition: Pair<Float, Float>,
        endPosition: Pair<Float, Float>,
        step: Float,
        direction: Direction
    ): Pair<Float, Float> =
        when (direction) {
            Direction.RIGHT -> {
                Pair(
                    endPosition.first,
                    startPosition.second + step * (endPosition.second - startPosition.second)
                )
            }

            Direction.LEFT -> {
                Pair(
                    endPosition.first,
                    startPosition.second + step * (endPosition.second - startPosition.second)
                )
            }

            Direction.UP -> {
                Pair(
                    startPosition.first + step * (endPosition.first - startPosition.first),
                    endPosition.second
                )
            }

            Direction.DOWN -> {
                Pair(
                    startPosition.first + step * (endPosition.first - startPosition.first),
                    endPosition.second
                )
            }

            Direction.NOWHERE -> {
                Pair(endPosition.first, endPosition.second)
            }
        }

    private fun shouldAnimate(
        startPosition: Pair<Float, Float>,
        endPosition: Pair<Float, Float>
    ): Boolean {
        if (startPosition == endPosition) return false
        if (abs(startPosition.first - endPosition.first) > 2f) return false
        if (abs(startPosition.second - endPosition.second) > 2f) return false
        return true
    }

    fun setActiveGameView(isPlayingGame:Boolean){
        isPlaying = isPlayingGame
    }

    fun changePauseGameStatus(pauseGame: Boolean) {
        isGamePaused = pauseGame
    }

    fun changeSoundGameStatus(soundActivate: Boolean) {
        soundIsMuted = soundActivate
    }

    private fun createPaint(
        color: Int = Color.BLACK,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 0f,
        textSize: Float = 0f,
        isAntiAlias: Boolean = false,
        textAlign: Paint.Align = Paint.Align.CENTER
    ): Paint =
        Paint().apply {
            this.color = color
            this.style = style
            this.strokeWidth = strokeWidth
            this.textSize = textSize
            this.isAntiAlias = isAntiAlias
            this.textAlign = textAlign
        }


    fun pauseGameDraw() {
        isGamePaused = true
    }

    fun resumeGameDraw() {
        isGamePaused = false
    }


    private fun drawGameElements(canvas: Canvas) {
        val cellSize: Float = width.toFloat() / 28

        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),emptySpace)
        // draw map
        for (i in 0 until mapMatrix.getRows()) {
            for (j in 0 until mapMatrix.getColumns()) {
                val left = (j * cellSize)
                val top = (i * cellSize)
                val right = left + cellSize
                val bottom = top + cellSize

                when (mapMatrix.getElementByPosition(i, j)) {
                    BoardController.EMPTY_SPACE,BoardController.BLANK_SPACE -> canvas.drawRect(
                        left,
                        top,
                        right,
                        bottom,
                        emptySpace
                    )

                    BoardController.WALL_CHAR -> canvas.drawRect(
                        left + 1,
                        top + 1,
                        right - 1,
                        bottom - 1,
                        wallPaint
                    )

                    BoardController.PELLET_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        canvas.drawPoint((left + right) / 2, (top + bottom) / 2, food)
                    }

                    BoardController.ENERGIZER_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        canvas.drawCircle(
                            (left + right) / 2,
                            (top + bottom) / 2,
                            cellSize / 4,
                            energizer
                        )
                    }

                    BoardController.BELL_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        drawBell(
                            canvas,
                            (left + right) / 2,
                            (top + bottom) / 2,
                            cellSize * 0.6f,
                            cellSize * 0.8f
                        )
                    }

                    BoardController.GHOST_DOOR_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, doorPaint)
                    }
                }
            }
        }
        for (j in 0 until mapMatrix.getColumns()) {
            val left = (j * cellSize)
            val top = (mapMatrix.getRows() * cellSize)
            val right = left + cellSize
            val bottom = top + cellSize
            canvas.drawRect(left, top, right, bottom, emptySpace)
        }

        // draw sound Icon

        val leftSpeaker = (22 * cellSize)
        val topSpeaker = (1 * cellSize)

        if (soundIsMuted) {
            canvas.drawText("SOUND OFF", leftSpeaker, topSpeaker, soundPaint)
        } else {
            canvas.drawText("SOUND ON", leftSpeaker, topSpeaker, soundPaint)
        }

        // draw scorer
        canvas.drawText("Scorer  $scorer", cellSize, cellSize, scorerPaint)

        // draw pacman lives
        val left1 = (1 * cellSize)
        val top1 = (35 * cellSize)
        val bottom1 = top1 + cellSize

        for (i in 1..pacmanLives) {
            val leftLive =
                left1 + (i - 1) * (cellSize + 10) //adjust the position of pacman lives
            val rightLive = leftLive + cellSize
            canvas.drawArc(leftLive, top1, rightLive, bottom1, 225f, 270f, true, pacmanPaint)
        }

        val left = (pacmanPosition.second * cellSize)
        val top = (pacmanPosition.first * cellSize)
        val right = left + cellSize
        val bottom = top + cellSize

        drawPacman(canvas, left, top, right, bottom)

        //DrawBlinky
        val blinkyLeft = (blinkyPosition.second * cellSize)
        val blinkyTop = (blinkyPosition.first * cellSize)
        val blikyRight = blinkyLeft + cellSize
        val blinkyBottom = blinkyTop + cellSize
        drawGhost(
            canvas,
            (blikyRight + blinkyLeft) / 2,
            (blinkyTop + blinkyBottom) / 2,
            cellSize,
            when {
                !blinkyIsAlive -> emptySpace
                pacmanEnergizerState -> frightenedGhostPaint
                else -> blinkyPaint
            },
            when {
                !blinkyIsAlive -> eyeBluePaint
                pacmanEnergizerState -> eyeWhitePaint
                else -> eyeBluePaint
            },
            blinkyDirection
        )

        //DrawInky
        val inkyLeft = (inkyPosition.second * cellSize)
        val inkyTop = (inkyPosition.first * cellSize)
        val inkyRight = inkyLeft + cellSize
        val inkyBottom = inkyTop + cellSize
        drawGhost(
            canvas,
            (inkyRight + inkyLeft) / 2,
            (inkyTop + inkyBottom) / 2,
            cellSize,
            when {
                !inkyIsAlive -> emptySpace
                pacmanEnergizerState -> frightenedGhostPaint
                else -> inkyPaint
            },
            when {
                !inkyIsAlive -> eyeBluePaint
                pacmanEnergizerState -> eyeWhitePaint
                else -> eyeBluePaint
            },
            inkyDirection
        )

        //DrawPinky
        val pinkyLeft = (pinkyPosition.second * cellSize)
        val pinkyTop = (pinkyPosition.first * cellSize)
        val pinkyRight = pinkyLeft + cellSize
        val pinkyBottom = pinkyTop + cellSize
        drawGhost(
            canvas,
            (pinkyRight + pinkyLeft) / 2,
            (pinkyTop + pinkyBottom) / 2,
            cellSize,
            when {
                !pinkyIsAlive -> emptySpace
                pacmanEnergizerState -> frightenedGhostPaint
                else -> pinkyPaint
            },
            when {
                !pinkyIsAlive -> eyeBluePaint
                pacmanEnergizerState -> eyeWhitePaint
                else -> eyeBluePaint
            },
            pinkyDirection
        )

        //DrawClyde
        val clydeLeft = (clydePosition.second * cellSize)
        val clydeTop = (clydePosition.first * cellSize)
        val clydeRight = clydeLeft + cellSize
        val clydeBottom = clydeTop + cellSize
        drawGhost(
            canvas,
            (clydeRight + clydeLeft) / 2,
            (clydeTop + clydeBottom) / 2,
            cellSize,
            when {
                !clydeIsAlive -> emptySpace
                pacmanEnergizerState -> frightenedGhostPaint
                else -> clydePaint
            },
            when {
                !clydeIsAlive -> eyeBluePaint
                pacmanEnergizerState -> eyeWhitePaint
                else -> eyeBluePaint
            },
            clydeDirection
        )

        if (isGamePaused) {
            drawPauseScreen(canvas, width.toFloat(), height.toFloat())
        }
        if (gameState == GameStatus.LOSE) {
            drawLoseScreen(canvas, width.toFloat(), height.toFloat())
        }
        if (gameState == GameStatus.WON) {
            drawWinScreen(canvas, width.toFloat(), height.toFloat())
        }
    }

    private fun drawPacman(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        canvas.drawRect(left, top, right, bottom, emptySpace)
        if (mouthOpen) {
            when (pacmanDirection) {
                Direction.RIGHT -> {
                    canvas.drawArc(left, top, right, bottom, 45f, 270f, true, pacmanPaint)
                }

                Direction.LEFT -> {
                    canvas.drawArc(left, top, right, bottom, 225f, 270f, true, pacmanPaint)
                }

                Direction.UP -> {
                    canvas.drawArc(left, top, right, bottom, 315f, 270f, true, pacmanPaint)
                }

                Direction.DOWN -> {
                    canvas.drawArc(left, top, right, bottom, 135f, 270f, true, pacmanPaint)
                }

                Direction.NOWHERE -> {}
            }
        } else {
            canvas.drawArc(
                left, top, right, bottom, 0f, 360f, true, pacmanPaint
            )
        }
    }

    private fun drawGhost(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        ghostPaint: Paint,
        pupilColor: Paint,
        direction: Direction
    ) {
        val headRadius = size / 2
        val waveRadius = headRadius / 4

        val headPath = Path().apply {

            addArc(
                cx - headRadius,
                cy - size / 2,
                cx + headRadius,
                cy + headRadius - size / 2,
                180f,
                180f
            )

            lineTo(cx + headRadius, cy + size / 2 - waveRadius)

            val numberOfWaves = ((headRadius * 2) / waveRadius).toInt()
            var waveCx = cx + headRadius
            var wave = true

            for (i in 0..numberOfWaves) {
                lineTo(waveCx, cy + size / 2 + if (wave) waveRadius else -waveRadius)
                wave = !wave
                waveCx -= waveRadius
            }

            lineTo(cx - headRadius, cy + size / 2 - waveRadius)
            close()
        }
        canvas.drawPath(headPath, ghostPaint)

        val eyeRadius = headRadius / 3
        val eyeOffsetX = headRadius / 2.5f
        val eyeOffsetY = headRadius / 3

        canvas.drawCircle(cx - eyeOffsetX, cy - eyeOffsetY, eyeRadius, eyeWhitePaint)
        canvas.drawCircle(cx + eyeOffsetX, cy - eyeOffsetY, eyeRadius, eyeWhitePaint)

        val pupilRadius = eyeRadius / 2
        var pupilOffsetX = 0f
        var pupilOffsetY = 0f

        when (direction) {
            Direction.LEFT -> pupilOffsetX = -eyeRadius / 2.5f
            Direction.RIGHT -> pupilOffsetX = eyeRadius / 2.5f
            Direction.UP -> pupilOffsetY = -eyeRadius / 2.5f
            Direction.DOWN -> pupilOffsetY = eyeRadius / 2.5f
            else -> {}
        }
        canvas.drawCircle(
            cx - eyeOffsetX + pupilOffsetX,
            cy - eyeOffsetY + pupilOffsetY,
            pupilRadius,
            eyeBluePaint
        )
        canvas.drawCircle(
            cx + eyeOffsetX + pupilOffsetX,
            cy - eyeOffsetY + pupilOffsetY,
            pupilRadius,
            eyeBluePaint
        )
    }

    private fun drawPauseScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, pauseScreenPaint)
        val xPos = width / 2
        val yPos = (height / 2 - (pauseMessagePaint.descent() + pauseMessagePaint.ascent()) / 2)
        canvas.drawText("PAUSE", xPos, yPos, pauseMessagePaint)
    }

    private fun drawWinScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, endGameScreenPaint)
        val xPos = width / 2
        val yPos =
            (height / 2 - (endGameMessageScreenPaint.descent() + endGameMessageScreenPaint.ascent()) / 2)
        canvas.drawText("YOU WIN", xPos, yPos, endGameMessageScreenPaint)
    }

    private fun drawLoseScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, endGameScreenPaint)
        val xPos = width / 2
        val yPos =
            (height / 2 - (endGameMessageScreenPaint.descent() + endGameMessageScreenPaint.ascent()) / 2)
        canvas.drawText("YOU LOSE", xPos, yPos, endGameMessageScreenPaint)
    }

    private fun drawBell(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        bellWidth: Float,
        bellHeight: Float
    ) {
        val top = centerY - bellHeight / 2
        val bottom = centerY + bellHeight / 2

        val path = Path().apply {
            moveTo(centerX - bellWidth / 2, top + bellHeight / 4)
            quadTo(centerX, top - bellHeight / 4, centerX + bellWidth / 2, top + bellHeight / 4)
            lineTo(centerX + bellWidth / 2, bottom - bellHeight / 6)
            quadTo(
                centerX,
                bottom + bellHeight / 8,
                centerX - bellWidth / 2,
                bottom - bellHeight / 6
            )
            close()
        }

        canvas.drawPath(path, bellPaint)
        canvas.drawCircle(centerX, bottom - bellHeight / 8, bellHeight / 8, bellPaint)
    }

    private fun drawStartScreen(canvas: Canvas,width: Float,height: Float){
        canvas.drawRect(0f, 0f, width, height, emptySpace)
        canvas.drawText("Pac-Man", width / 2f, 200f, tittleMainScreen)
        canvas.drawText("Press Start To Play", width / 2f, height / 2f + 25f, textMainScreenPaint)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        startDrawing()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}