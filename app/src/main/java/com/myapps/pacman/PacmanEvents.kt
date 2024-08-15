package com.myapps.pacman

sealed interface PacmanEvents {
    data object Start:PacmanEvents
    data object Stop:PacmanEvents
    data object RightDirection:PacmanEvents
    data object LeftDirection:PacmanEvents
    data object UpDirection:PacmanEvents
    data object DownDirection:PacmanEvents
}