package com.myapps.pacman.ghost

enum class GhostMode {
    CHASE, //O fantasma persegue diretamente o Pac-Man. Esse é o comportamento padrão quando o fantasma está tentando capturar o Pac-Man.
    SCATTER, //O fantasma se espalha para uma área específica do mapa, geralmente a sua "casa" ou um ponto designado. Esse modo é usado para dar ao jogo uma pausa entre as perseguições e aumentar a dificuldade.
}