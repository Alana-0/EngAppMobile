package com.myapps.pacman.timer

// A interface TimerInterface define os métodos essenciais para controlar um temporizador: inicializar,
// pausar/despausar, resetar, parar e obter os "ticks" atuais. Essas funções são implementadas em
// classes que gerenciam o comportamento do temporizador, permitindo flexibilidade em como o temporizador é
// controlado.


interface TimerInterface {
    fun init()
    fun pauseUnpauseTime()
    fun resetCounter()
    fun stop()
    fun getCurrentTicks():Int
}