package com.myapps.pacman.timer
// A classe Timer gerencia um temporizador com base em coroutines. Ela utiliza um intervalo de tempo
// definido (default 1000ms) para contar os "ticks" enquanto o temporizador não estiver pausado.
// Permite inicializar o temporizador, pausar/despausar, resetar o contador e parar o temporizador.
// O contador é armazenado de forma atômica, permitindo acesso seguro em ambientes concorrentes.

import com.myapps.pacman.modules.qualifiers.DispatcherDefault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class Timer @Inject constructor(
    @DispatcherDefault
    private val coroutineDispatcher: CoroutineDispatcher,
    private val intervalMillis: Long = 1000L
) : TimerInterface {

    private var currentTicks = AtomicInteger(0)
    private var job: Job? = null
    private var isPaused = false

    override fun init() {
        job?.cancel()
        job = CoroutineScope(coroutineDispatcher).launch {
            while (isActive) {
                if (!isPaused) {
                    currentTicks.incrementAndGet()
                }
                delay(intervalMillis)
            }
        }
    }

    override fun pauseUnpauseTime() {
        isPaused = !isPaused
    }

    override fun resetCounter() {
        currentTicks.set(0)
    }

    override fun stop() {
        job?.cancel()
        currentTicks.set(0)
    }

    override fun getCurrentTicks(): Int = currentTicks.get()
}