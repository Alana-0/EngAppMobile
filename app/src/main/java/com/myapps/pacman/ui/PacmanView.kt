package com.myapps.pacman.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.R
import com.myapps.pacman.flowData.BoardData
import com.myapps.pacman.flowData.GhostData
import com.myapps.pacman.flowData.PacmanData
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


    //pacman mouth
    private var mouthOpen = true
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var coroutineScope = CoroutineScope(Dispatchers.Main)

    private var runnable: Runnable = object : Runnable {
        override fun run() {
            mouthOpen = !mouthOpen
            invalidate()
            handler.postDelayed(this, 200)
        }
    }

    private val pacmanPaint = Paint().apply {
        color = Color.YELLOW
    }

    //game view elements painters
    private var scorerPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 20f
        textSize = 22f
    }
    private val blinkyPaint = Paint().apply {
        color = Color.RED
    }
    private val pinkyPaint = Paint().apply {
        color = resources.getColor(R.color.pink, resources.newTheme())
    }
    private val clydePaint = Paint().apply {
        color = resources.getColor(R.color.orange, resources.newTheme())
    }
    private val inkyPaint = Paint().apply {
        color = resources.getColor(R.color.lightBlue, resources.newTheme())
    }
    private val eyeWhitePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val eyeBluePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }
    private val emptySpace = Paint().apply {
        color = Color.BLACK
    }
    private val frightenedGhostPaint = Paint().apply {
        color = resources.getColor(R.color.darkBlue, resources.newTheme())
    }

    private var wallPaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 3f
    }
    private var food = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
    }
    private var energizer = Paint().apply {
        color = Color.WHITE
        strokeWidth = 20f
    }
    private var doorPaint = Paint().apply {
        color = Color.WHITE
    }

    private val bellPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    //game variables
    private var scorer = 0
    private var pacmanLives = 0

    //blinky variables
    private var blinkyPosition = Pair(-1f, -1f)
    private var targetBlinky = blinkyPosition
    private var blinkyDirection = Direction.NOWHERE
    private var blinkyIsAlive = true

    //pacman variables
    private var pacmanPosition = Pair(-1f, -1f)
    private var pacmanEnergizerState = false
    private var pacmanTarget = pacmanPosition
    private var pacmanDirection = Direction.RIGHT

    //pinky variables
    private var pinkyPosition = Pair(-1f, -1f)
    private var targetPinky = pinkyPosition
    private var pinkyDirection = Direction.RIGHT
    private var pinkyIsAlive = true

    //inky variables
    private var inkyPosition = Pair(-1f, -1f)
    private var targetInky = inkyPosition
    private var inkyDirection = Direction.RIGHT
    private var inkyIsAlive = true

    // clyde variables
    private var clydePosition = Pair(-1f, -1f)
    private var targetClyde = clydePosition
    private var clydeDirection = Direction.RIGHT
    private var clydeIsAlive = true

    // interpolations Jobs (this is with the purpose to make pacman and ghost movements pleasent to the eye)
    private var pacmanInterpolationJob: Job? = null
    private var blinkyInterpolationJob: Job? = null
    private var inkyInterpolationJob: Job? = null
    private var pinkyInterpolationJob: Job? = null
    private var clydeInterpolationJob: Job? = null


    private var mapMatrix = Matrix<Char>(0, 0)

    init {
        handler.post(runnable)
    }

    fun setPacmanData(pacmanData: StateFlow<PacmanData>) {
        coroutineScope.launch {
            pacmanData.collect { data ->
                pacmanEnergizerState = data.isEnergizer
                pacmanTarget =
                    Pair(data.position.positionX.toFloat(), data.position.positionY.toFloat())
                pacmanDirection = data.direction

                if (shouldInterpolatePosition(pacmanPosition, pacmanTarget)) {
                    startPacmanInterpolation(
                        pacmanPosition,
                        pacmanTarget,
                        pacmanData.value.direction,
                        getStepsBySpeedDelay(data.movementsDelay)
                    )
                } else {
                    pacmanPosition = pacmanTarget
                    invalidate()
                }
            }
        }
    }

    fun setGameMapFlow(gameMapFlow: StateFlow<BoardData>) {
        coroutineScope.launch {
            gameMapFlow.collect { boardData ->
                mapMatrix = boardData.charData
                scorer = boardData.scorer
                pacmanLives = boardData.pacmanLives
                wallPaint.color = when (boardData.currentLevel) {
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

    fun setBlinkyFlow(blinkyFlow: StateFlow<GhostData>) {
        coroutineScope.launch {
            blinkyFlow.collect { blinky ->
                blinkyIsAlive = blinky.lifeStatement
                targetBlinky =
                    Pair(blinky.position.positionX.toFloat(), blinky.position.positionY.toFloat())
                blinkyDirection = blinky.direction
                if (shouldInterpolatePosition(blinkyPosition, targetBlinky)) {
                    startBlinkyInterpolation(
                        blinkyPosition,
                        targetBlinky,
                        blinky.direction,
                        getStepsBySpeedDelay(blinky.speedDelay)
                    )
                } else {
                    blinkyPosition = targetBlinky
                    invalidate()
                }
            }
        }
    }

    fun setInkyFlow(inkyFlow: StateFlow<GhostData>) {
        coroutineScope.launch {
            inkyFlow.collect { inky ->
                inkyIsAlive = inky.lifeStatement
                targetInky =
                    Pair(inky.position.positionX.toFloat(), inky.position.positionY.toFloat())
                inkyDirection = inky.direction
                if (shouldInterpolatePosition(inkyPosition, targetInky)) {
                    startInkyInterpolation(
                        inkyPosition,
                        targetInky,
                        inky.direction,
                        getStepsBySpeedDelay(inky.speedDelay)
                    )
                } else {
                    inkyPosition = targetInky
                    invalidate()
                }
            }
        }
    }

    fun setPinkyFlow(pinkyFlow: StateFlow<GhostData>) {
        coroutineScope.launch {
            pinkyFlow.collect { pinky ->
                pinkyIsAlive = pinky.lifeStatement
                targetPinky =
                    Pair(pinky.position.positionX.toFloat(), pinky.position.positionY.toFloat())
                pinkyDirection = pinky.direction
                if (shouldInterpolatePosition(pinkyPosition, targetPinky)) {
                    startPinkyInterpolation(
                        pinkyPosition,
                        targetPinky,
                        pinky.direction,
                        getStepsBySpeedDelay(pinky.speedDelay)
                    )
                } else {
                    pinkyPosition = targetPinky
                    invalidate()
                }
            }
        }
    }

    fun setClydeFlow(clydeFlow: StateFlow<GhostData>) {
        coroutineScope.launch {
            clydeFlow.collect { clyde ->
                clydeIsAlive = clyde.lifeStatement
                targetClyde =
                    Pair(clyde.position.positionX.toFloat(), clyde.position.positionY.toFloat())
                clydeDirection = clyde.direction

                if (shouldInterpolatePosition(clydePosition, targetClyde)) {
                    startClydeInterpolation(
                        clydePosition,
                        targetClyde,
                        clyde.direction,
                        getStepsBySpeedDelay(clyde.speedDelay)
                    )
                } else {
                    clydePosition = targetClyde
                    invalidate()
                }
            }
        }
    }


    fun stopAllInterpolationJobs() {
        pacmanInterpolationJob?.cancel()
        blinkyInterpolationJob?.cancel()
        inkyInterpolationJob?.cancel()
        pinkyInterpolationJob?.cancel()
        clydeInterpolationJob?.cancel()
    }


    private fun startBlinkyInterpolation(
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        direction: Direction,
        steps: Int,
    ) {
        blinkyInterpolationJob?.cancel()
        blinkyInterpolationJob = coroutineScope.launch {
            for (i in 1..steps) {
                val fraction = i / steps.toFloat()
                blinkyPosition = interpolatePosition(start, end, fraction, direction)
                invalidate()
                delay(16)  // Adjust delay for smoother/faster interpolation
            }
            blinkyPosition = end // Ensure final position is exact
        }
    }

    private fun startInkyInterpolation(
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        direction: Direction,
        steps: Int,
    ) {
        inkyInterpolationJob?.cancel()
        inkyInterpolationJob = coroutineScope.launch {
            for (i in 1..steps) {
                val fraction = i / steps.toFloat()
                inkyPosition = interpolatePosition(start, end, fraction, direction)
                invalidate()
                delay(16)  // Adjust delay for smoother/faster interpolation
            }
            inkyPosition = end // Ensure final position is exact
        }
    }

    private fun startPinkyInterpolation(
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        direction: Direction,
        steps: Int,
    ) {
        pinkyInterpolationJob?.cancel()
        pinkyInterpolationJob = coroutineScope.launch {
            for (i in 1..steps) {
                val fraction = i / steps.toFloat()
                pinkyPosition = interpolatePosition(start, end, fraction, direction)
                invalidate()
                delay(16)  // Adjust delay for smoother/faster interpolation
            }
            pinkyPosition = end // Ensure final position is exact
        }
    }

    private fun startClydeInterpolation(
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        direction: Direction,
        steps: Int,
    ) {
        clydeInterpolationJob?.cancel()
        clydeInterpolationJob = coroutineScope.launch {
            for (i in 1..steps) {
                val fraction = i / steps.toFloat()
                clydePosition = interpolatePosition(start, end, fraction, direction)
                invalidate()
                delay(16)  // Adjust delay for smoother/faster interpolation
            }
            clydePosition = end // Ensure final position is exact
        }
    }

    private fun startPacmanInterpolation(
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        direction: Direction,
        steps: Int,
    ) {
        pacmanInterpolationJob?.cancel()
        pacmanInterpolationJob = coroutineScope.launch {
            for (i in 1..steps) {
                val fraction = i / steps.toFloat()
                pacmanPosition = interpolatePosition(start, end, fraction, direction)
                invalidate()
                delay(16) // Adjust delay for smoother/faster interpolation
            }
            pacmanPosition = end // Ensure final position is exact
        }
    }

    private fun interpolatePosition(
        start: Pair<Float, Float>,
        end: Pair<Float, Float>,
        fraction: Float,
        direction: Direction
    ): Pair<Float, Float> =
        when (direction) {
            Direction.RIGHT -> {
                val y = start.second + fraction * (end.second - start.second)
                Pair(end.first, y)
            }

            Direction.LEFT -> {
                val y = start.second + fraction * (end.second - start.second)
                Pair(end.first, y)
            }

            Direction.UP -> {
                val x = start.first + fraction * (end.first - start.first)
                Pair(x, end.second)
            }

            Direction.DOWN -> {
                val x = start.first + fraction * (end.first - start.first)
                Pair(x, end.second)
            }

            else -> {
                start
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


    // this will depend on how much smoother you want the animation and
    // the delay used in the actors movements
    private fun getStepsBySpeedDelay(speedDelay: Long): Int {
        if (speedDelay == 100L) return 5
        if (speedDelay == 150L) return 7
        if (speedDelay == 160L) return 8
        if (speedDelay == 170L) return 8
        if (speedDelay == 180L) return 9
        if (speedDelay == 190L) return 9
        if (speedDelay == 200L) return 10
        if (speedDelay == 500L) return 35
        return 10
    }

    private fun shouldInterpolatePosition(
        positionOne: Pair<Float, Float>,
        positionTwo: Pair<Float, Float>
    ): Boolean {
        if (abs(positionOne.first - positionTwo.first) > 2f) return false
        if (abs(positionOne.second - positionTwo.second) > 2f) return false
        return true
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
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), borderPaint)
    }
}

