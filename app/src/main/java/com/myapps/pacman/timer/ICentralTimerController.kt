package com.myapps.pacman.timer
// Interface que define os métodos para controlar os temporizadores no jogo Pacman. Ela permite inicializar ,
// ]parar, iniciar, pausar, retomar, reiniciar e remover temporizadores individuais ou de todos os
// controladores. Além disso, oferece a capacidade de obter os "ticks" (tempo decorrido) de um temporizador
// específico.

interface ICentralTimerController {
    fun initTimerFunction()
    fun stopTimerFunction()
    fun addNewTimerController(timerId:String)
    fun startTimerController(timerId:String)
    fun startAllTimersController(timerId: String)
    fun restartTimerController(timerId: String)
    fun pauseTimerController(timerId: String)
    fun unpauseTimerController(timerId: String)
    fun getTimerTicksController(timerId: String): Int
    fun stopTimerController(timerId: String)
    fun stopAllTimersController()
    fun pauseAllTimersController()
    fun unpauseAllTimersController()
    fun removeTimerController(timerId:String)
    fun removeAllTimersController()
}