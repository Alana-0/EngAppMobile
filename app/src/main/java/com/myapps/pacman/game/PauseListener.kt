package com.myapps.pacman.game

//responsável por definir os métodos que serão chamados quando o jogo for pausado ou
// retomado. Ela serve para permitir que outras partes do sistema se inscrevam para
// receber notificações sobre o estado de pausa.

interface PauseListener {
    fun onPause()
    fun onResume()
}