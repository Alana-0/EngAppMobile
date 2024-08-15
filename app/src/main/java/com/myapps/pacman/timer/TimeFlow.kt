package com.myapps.pacman.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

object TimeFlow {

    private var currentTicks = AtomicInteger(0)

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun init(){
        job?.cancel()
        job = scope.launch {
            while (isActive){
                delay(1000)
                currentTicks.incrementAndGet()
            }
        }
    }

    fun stop(){
        job?.cancel()
        currentTicks.set(0)
    }

    fun getCurrentTicks():Int = currentTicks.get()
}