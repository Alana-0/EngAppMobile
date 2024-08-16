package com.myapps.pacman.utils


data class LevelStartData(
    val mapCharData:List<String>,
    val pacmanDefaultPosition:Position,
    val blinkyDefaultPosition:Position,
    val inkyDefaultPosition:Position,
    val pinkyDefaultPosition:Position,
    val clydeDefaultPosition:Position,
    val homeTargetPosition:Position,
    val ghostHomeXRange:IntRange,
    val ghostHomeYRange:IntRange,
    val blinkyScatterPosition:Position,
    val inkyScatterPosition:Position,
    val pinkyScatterPosition:Position,
    val clydeScatterPosition:Position,
    val doorTarget:Position,
    val width:Int,
    val height:Int,
    val amountOfFood:Int,
    val blinkySpeedDelay:Long,
    val isBell:Boolean
)
