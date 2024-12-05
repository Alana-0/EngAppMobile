package com.myapps.pacman.game

object GameConstants {
    const val SCATTER_TIME = 7 //Duração da fase em que os fantasmas se dispersam.
    const val CHASE_TIME = 20 //Duração da fase em que os fantasmas perseguem o Pacman.
    const val ENERGIZER_TIME = 6 // Tempo que o Pacman permanece energizado após comer
    const val BELL_TIME = 10  //Tempo durante o qual um sino coletado permanece ativo.
    const val PELLET_POINTS = 10 // Pontos por comer um pellet.
    const val ENERGIZER_POINTS = 50 //Pontos por comer um energizer.
    const val SIREN_DELAY = 330L
    const val BELL_POINTS = 200 //Pontos por coletar um sino.
    const val BELL_REDUCTION_TIME = 10 //Redução no tempo restante do sino, caso necessário.
    const val PACMAN_LIVES = 3 //Número de vidas iniciais do Pacman.
}