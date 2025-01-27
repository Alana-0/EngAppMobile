package com.myapps.pacman.timer

//// A classe CentralTimerController gerencia múltiplos temporizadores no jogo Pacman.
// Ela permite inicializar, iniciar, pausar, retomar, reiniciar e parar os temporizadores de
// diversos componentes (como fantasmas e energizadores) através de controladores específicos.
// Cada temporizador é identificado por um ID único, e é possível gerenciar todos ou um temporizador
// de forma individual. A classe também oferece a funcionalidade de obter os "ticks" de um temporizador,
// que são contagens do tempo decorrido.

import javax.inject.Inject

class CentralTimerController(
    private val timer:TimerInterface
):ICentralTimerController{

    companion object{
        const val GHOST_TIMER = "GhostTimer"
        const val BELL_TIMER = "BellTimer"
        const val ENERGIZER_TIMER = "EnergizerTimer"
    }

    private val timerControllers = mutableMapOf<String, TimerController>()


    override fun initTimerFunction(){
        timer.init()
    }

    override fun stopTimerFunction(){
        timer.stop()
    }

    override fun addNewTimerController(timerId:String){
        if (!timerControllers.containsKey(timerId)) {
            val timer = TimerController(timer = timer)
            timerControllers[timerId] = timer
        }
    }

    override fun startTimerController(timerId:String){
        timerControllers[timerId]?.start()
    }

    override fun startAllTimersController(timerId: String){
        timerControllers.forEach{ (_,timerController)-> timerController.start() }
    }

    override fun restartTimerController(timerId: String) {
        timerControllers[timerId]?.restart()
    }

    override fun pauseTimerController(timerId: String) {
        timerControllers[timerId]?.pause()
    }

    override fun unpauseTimerController(timerId: String) {
        timerControllers[timerId]?.unpause()
    }

    override fun getTimerTicksController(timerId: String): Int {
        return timerControllers[timerId]?.getTicks()?:0
    }

    override fun stopTimerController(timerId: String) {
        timerControllers[timerId]?.reset()
    }

    override fun stopAllTimersController() {
        timerControllers.forEach { (_, timerController) -> timerController.reset() }
    }

    override fun pauseAllTimersController() {
        timerControllers.forEach { (_, timerController) -> timerController.pause() }
    }

    override fun unpauseAllTimersController() {
        timerControllers.forEach { (_, timerController) -> timerController.unpause() }
    }

    override fun removeTimerController(timerId:String){
        timerControllers.remove(timerId)
    }
    override fun removeAllTimersController(){
        timerControllers.clear()
    }
}