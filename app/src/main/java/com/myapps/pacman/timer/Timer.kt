package com.myapps.pacman.timer

class Timer {
    private var startTicks = 0
    private var pausedTicks = 0
    private var isPaused = false
    private var isStarted = false


    fun start() {
        isStarted = true
        isPaused = false
        startTicks = TimeFlow.getCurrentTicks()
        pausedTicks = 0
    }

    fun reset() {
        startTicks = 0
        pausedTicks = 0
        isPaused = false
        isStarted = false
    }

    fun restart() {
        this.reset()
        this.start()
    }

    fun pause() {
        if (isStarted && !isPaused) {
            isPaused = true
            pausedTicks = TimeFlow.getCurrentTicks() - startTicks
            startTicks = 0
        }
    }

    fun unpause() {
        if (isStarted && isPaused) {
            isPaused = false
            startTicks = TimeFlow.getCurrentTicks() - pausedTicks
            pausedTicks = 0
        }
    }

    fun getTicks(): Int =
        if (isStarted) {
            if (isPaused) {
                pausedTicks
            } else TimeFlow.getCurrentTicks() - startTicks
        } else 0

    fun isStarted(): Boolean = isStarted
    fun isPaused(): Boolean = isPaused
}
