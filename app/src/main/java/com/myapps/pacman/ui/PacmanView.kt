package com.myapps.pacman.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.R
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs


class PacmanView(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    //external variables
    private var soundIsMuted = false
    private var gameIsPaused = false
    private var gameIsLost = false
    private var gameIsWin = false

    //pacman mouth
    private var mouthOpen = true
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var collectMapDataJob: Job? = null
    private var collectPacmanDataJob: Job? = null
    private var collectPinkyDataJob: Job? = null
    private var collectBlinkyDataJob: Job? = null
    private var collectInkyDataJob: Job? = null
    private var collectClydeDataJob: Job? = null


    private var runnable: Runnable = object : Runnable {
        override fun run() {
            mouthOpen = !mouthOpen
            invalidate()
            handler.postDelayed(this, 200)
        }
    }

    //game view elements painters
    private val pacmanPaint = createPaint(color = Color.YELLOW)
    private val scorerPaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 22f, textAlign = Paint.Align.LEFT)
    private val blinkyPaint = createPaint(color = Color.RED)
    private val pinkyPaint = createPaint(color = resources.getColor(R.color.pink, resources.newTheme()))
    private val clydePaint = createPaint(color =  resources.getColor(R.color.orange, resources.newTheme()))
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
    private val endGameScreenPaint = createPaint(color =  resources.getColor(R.color.littleTransparentBlack,resources.newTheme()))
    private val endGameMessageScreenPaint = createPaint(color = Color.YELLOW, strokeWidth = 25f, textSize = 40f)


    private fun createPaint(
        color: Int = Color.BLACK,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 0f,
        textSize: Float = 0f,
        isAntiAlias: Boolean = false,
        textAlign:Align = Paint.Align.CENTER
    ):Paint =
        Paint().apply {
            this.color = color
            this.style = style
            this.strokeWidth = strokeWidth
            this.textSize = textSize
            this.isAntiAlias = isAntiAlias
            this.textAlign = textAlign
        }

    //game variables
    private var scorer = 0
    private var pacmanLives = 0

    //blinky variables
    private var blinkyPosition = Pair(-1f, -1f)
    private var blinkyDirection = Direction.NOWHERE
    private var blinkyIsAlive = true

    //pacman variables
    private var pacmanPosition = Pair(-1f, -1f)
    private var pacmanEnergizerState = false
    private var pacmanDirection = Direction.RIGHT

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


    private var mapMatrix = Matrix<Char>(0, 0)

    init {
        handler.post(runnable)
    }

    fun setGameBoardData(gameBoardData: StateFlow<BoardData>) {
        collectMapDataJob?.cancel()
        collectMapDataJob = coroutineScope.launch {
            gameBoardData.collect {
                mapMatrix = it.gameBoardData
                scorer = it.scorer
                pacmanLives = it.pacmanLives
                gameIsLost = it.isGameLose
                gameIsWin = it.isGameWin
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
                invalidate()
            }
        }
    }

    fun setPacmanData(pacmanData: StateFlow<PacmanData>) {
        collectPacmanDataJob?.cancel()
        collectPacmanDataJob = coroutineScope.launch {
            pacmanData.collect {
                pacmanEnergizerState = it.energizerStatus
                pacmanDirection = it.pacmanDirection
               if (shouldAnimate(pacmanPosition, it.pacmanPosition)) {
                   animateActorTo(
                       pacmanPosition,
                       it.pacmanPosition,
                       pacmanDirection,
                       it.speedDelay
                   ){
                           pair ->
                       pacmanPosition = pair
                   }
                } else {
                    pacmanPosition = it.pacmanPosition
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
                if (shouldAnimate(blinkyPosition, it.ghostPosition)) {
                    animateActorTo(
                        blinkyPosition,
                        it.ghostPosition,
                        blinkyDirection,
                        it.ghostDelay
                    ){
                            pair ->
                        blinkyPosition = pair
                    }
                } else {
                    blinkyPosition = it.ghostPosition
                    invalidate()
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
                if (shouldAnimate(inkyPosition, it.ghostPosition)) {
                    animateActorTo(
                        inkyPosition,
                        it.ghostPosition,
                        inkyDirection,
                        it.ghostDelay
                    ){
                            pair ->
                        inkyPosition = pair
                    }
                } else {
                    inkyPosition = it.ghostPosition
                    invalidate()
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
                if (shouldAnimate(pinkyPosition, it.ghostPosition)) {
                    animateActorTo(
                        pinkyPosition,
                        it.ghostPosition,
                        pinkyDirection,
                        it.ghostDelay
                    ){
                            pair ->
                        pinkyPosition = pair
                    }
                } else {
                    pinkyPosition = it.ghostPosition
                    invalidate()
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
                if (shouldAnimate(clydePosition, it.ghostPosition)) {
                    animateActorTo(
                        startPosition = clydePosition,
                        targetPosition = it.ghostPosition,
                        direction = clydeDirection,
                        speedDelay = it.ghostDelay
                    ){
                        pair ->
                        clydePosition = pair
                    }
                } else {
                    clydePosition = it.ghostPosition
                    invalidate()
                }
            }
        }
    }

    fun stopAllCurrentJobs() {
        collectMapDataJob?.cancel()
        collectPacmanDataJob?.cancel()
        collectBlinkyDataJob?.cancel()
        collectInkyDataJob?.cancel()
        collectClydeDataJob?.cancel()
        collectPinkyDataJob?.cancel()
    }

    private fun animateActorTo(
        startPosition: Pair<Float, Float>,
        targetPosition: Pair<Float, Float>,
        direction: Direction,
        speedDelay:Long,
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
                invalidate()
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    updatePosition(targetPosition)
                    invalidate()
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
        if(startPosition == endPosition) return false
        if (abs(startPosition.first - endPosition.first) > 2f) return false
        if (abs(startPosition.second - endPosition.second) > 2f) return false
        return true
    }


    fun changePauseGameStatus(pauseGame: Boolean) {
        gameIsPaused = pauseGame
    }

    fun changeSoundGameStatus(soundActivate: Boolean) {
        soundIsMuted = soundActivate
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

        // draw the head of the ghost(arc)
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
            var waveCx = cx + headRadius
            var wave = true
            while (waveCx >= cx - headRadius) {
                lineTo(waveCx, cy + size / 2 + if (wave) waveRadius else -waveRadius)
                wave = !wave
                waveCx -= waveRadius
            }
            lineTo(cx - headRadius, cy + size / 2 - waveRadius)
            close()
        }
        canvas.drawPath(headPath, ghostPaint)

        // draw the eyes of the ghost
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


    private fun drawPauseScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, pauseScreenPaint)
        val xPos = width/2
        val yPos = (height / 2 - (pauseMessagePaint.descent() + pauseMessagePaint.ascent()) / 2)
        canvas.drawText("PAUSE",xPos,yPos,pauseMessagePaint)
    }

    private fun drawWinScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ){
        canvas.drawRect(0f,0f,width,height,endGameScreenPaint)
        val xPos = width/2
        val yPos = (height / 2 - (endGameMessageScreenPaint.descent() + endGameMessageScreenPaint.ascent()) / 2)
        canvas.drawText("YOU WIN",xPos,yPos,endGameMessageScreenPaint)
    }
    private fun drawLoseScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ){
        canvas.drawRect(0f,0f,width,height,endGameScreenPaint)
        val xPos = width/2
        val yPos = (height / 2 - (endGameMessageScreenPaint.descent() + endGameMessageScreenPaint.ascent()) / 2)
        canvas.drawText("YOU LOSE",xPos,yPos,endGameMessageScreenPaint)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellSize: Float = width.toFloat() / 28  // assuming square cells

        // Draw the map
        for (i in 0 until mapMatrix.getRows()) {
            for (j in 0 until mapMatrix.getColumns()) {
                val left = (j * cellSize)
                val top = (i * cellSize)
                val right = left + cellSize
                val bottom = top + cellSize

                when (mapMatrix.getElementByPosition(i, j)) {
                    '_' -> canvas.drawRect(left, top, right, bottom, emptySpace)
                    '|' -> canvas.drawRect(left + 1, top + 1, right - 1, bottom - 1, wallPaint)
                    '.' -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        canvas.drawPoint((left + right) / 2, (top + bottom) / 2, food)
                    }

                    'o' -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        canvas.drawCircle(
                            (left + right) / 2,
                            (top + bottom) / 2,
                            cellSize / 4,
                            energizer
                        )
                    }

                    'b' -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        drawBell(
                            canvas,
                            (left + right) / 2,
                            (top + bottom) / 2,
                            cellSize * 0.6f,
                            cellSize * 0.8f
                        )
                    }

                    ' ', '-' -> canvas.drawRect(left, top, right, bottom, emptySpace)
                    '=' -> canvas.drawRect(left, top, right, bottom, doorPaint)
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
        val left = (1 * cellSize)
        val top = (1 * cellSize)
        canvas.drawText("Scorer  $scorer", top, left, scorerPaint)

        // draw pacman lives
        val left1 = (1 * cellSize)
        val top1 = (35 * cellSize)
        val bottom1 = top1 + cellSize

        for (i in 1..pacmanLives) {
            val leftLive =
                left1 + (i - 1) * (cellSize + 10) //adjust the position of pacman lives
            val rightLive = leftLive + cellSize
            canvas.drawArc(
                leftLive,
                top1,
                rightLive,
                bottom1,
                225f,
                270f,
                true,
                pacmanPaint
            )
        }

        // drawing the pacman actors

        // Draw Pacman
        val pacmanLeft = (pacmanPosition.second * cellSize)
        val pacmanTop = (pacmanPosition.first * cellSize)
        val pacmanRight = pacmanLeft + cellSize
        val pacmanBottom = pacmanTop + cellSize

        canvas.drawRect(pacmanLeft, pacmanTop, pacmanRight, pacmanBottom, emptySpace)
        if (mouthOpen) {
            when (pacmanDirection) {
                Direction.RIGHT -> {
                    canvas.drawArc(
                        pacmanLeft,
                        pacmanTop,
                        pacmanRight,
                        pacmanBottom,
                        45f,
                        270f,
                        true,
                        pacmanPaint
                    )
                }

                Direction.LEFT -> {
                    canvas.drawArc(
                        pacmanLeft,
                        pacmanTop,
                        pacmanRight,
                        pacmanBottom,
                        225f,
                        270f,
                        true,
                        pacmanPaint
                    )
                }

                Direction.UP -> {
                    canvas.drawArc(
                        pacmanLeft,
                        pacmanTop,
                        pacmanRight,
                        pacmanBottom,
                        315f,
                        270f,
                        true,
                        pacmanPaint
                    )
                }

                Direction.DOWN -> {
                    canvas.drawArc(
                        pacmanLeft,
                        pacmanTop,
                        pacmanRight,
                        pacmanBottom,
                        135f,
                        270f,
                        true,
                        pacmanPaint
                    )
                }

                Direction.NOWHERE -> {}
            }
        } else {
            canvas.drawArc(
                pacmanLeft,
                pacmanTop,
                pacmanRight,
                pacmanBottom,
                0f,
                360f,
                true,
                pacmanPaint
            )
        }

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
            if (!blinkyIsAlive) {
                emptySpace
            } else {
                if (pacmanEnergizerState) {
                    frightenedGhostPaint
                } else {
                    blinkyPaint
                }
            },
            if (!blinkyIsAlive) {
                eyeBluePaint
            } else {
                if (pacmanEnergizerState) {
                    eyeWhitePaint
                } else {
                    eyeBluePaint
                }
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
            if (!inkyIsAlive) {
                emptySpace
            } else {
                if (pacmanEnergizerState) {
                    frightenedGhostPaint
                } else {
                    inkyPaint
                }
            },
            if (!inkyIsAlive) {
                eyeBluePaint
            } else {
                if (pacmanEnergizerState) {
                    eyeWhitePaint
                } else {
                    eyeBluePaint
                }
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
            if (!pinkyIsAlive) {
                emptySpace
            } else {
                if (pacmanEnergizerState) {
                    frightenedGhostPaint
                } else {
                    pinkyPaint
                }
            },
            if (!pinkyIsAlive) {
                eyeBluePaint
            } else {
                if (pacmanEnergizerState) {
                    eyeWhitePaint
                } else {
                    eyeBluePaint
                }
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
            if (!clydeIsAlive) {
                emptySpace
            } else {
                if (pacmanEnergizerState) {
                    frightenedGhostPaint
                } else {
                    clydePaint
                }
            },
            if (clydeIsAlive) {
                eyeBluePaint
            } else {
                if (pacmanEnergizerState) {
                    eyeWhitePaint
                } else {
                    eyeBluePaint
                }
            },
            clydeDirection
        )

        if (gameIsPaused) {
            drawPauseScreen(
                canvas,
                width = width.toFloat(),
                height = height.toFloat(),
            )
        }
        if(gameIsLost){
            drawLoseScreen(
                canvas,
                width = width.toFloat(),
                height = height.toFloat()
            )
        }
        if(gameIsWin){
            drawWinScreen(
                canvas,
                width = width.toFloat(),
                height = height.toFloat()
            )
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAllCurrentJobs()
    }
}

