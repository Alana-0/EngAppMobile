package com.myapps.pacman

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.game.PacmanGame
import com.myapps.pacman.utils.Direction
import dagger.hilt.android.lifecycle.HiltViewModel


import javax.inject.Inject

@HiltViewModel
class PacmanGameViewModel @Inject constructor(
   private val pacmanGame: PacmanGame
) : ViewModel() {

    private val _positionList =  mutableListOf(Direction.RIGHT)
    val pacmanData = pacmanGame.pacmanState
        .stateIn(viewModelScope, SharingStarted.Lazily, PacmanData())
    val blinkyData = pacmanGame.blinkyState
        .stateIn(viewModelScope, SharingStarted.Lazily, GhostData())
    val inkyData = pacmanGame.inkyState
        .stateIn(viewModelScope, SharingStarted.Lazily,GhostData())
    val pinkyData =pacmanGame.pinkyState
        .stateIn(viewModelScope, SharingStarted.Lazily,GhostData())
    val clydeData = pacmanGame.clydeState
        .stateIn(viewModelScope, SharingStarted.Lazily,GhostData())
    val mapBoardData = pacmanGame.boardState
        .stateIn(viewModelScope, SharingStarted.Lazily,BoardData())

    val gameIsStarted = MutableStateFlow(false)
    val gameIsMute = MutableStateFlow(false)
    val gameIsPaused = MutableStateFlow(false)


    private fun startGame(){
        pacmanGame.initGame(_positionList)

    }

    private fun stopGame(){
        pacmanGame.stopGame()
        _positionList.clear()
        _positionList.add(Direction.RIGHT)
    }


    fun onEvents(pacmanEvents: PacmanEvents) {
        when (pacmanEvents) {
            is PacmanEvents.RightDirection -> {
                _positionList.add(Direction.RIGHT)
                if (_positionList.size > 2) {
                    _positionList.removeAt(1)
                }
            }

            is PacmanEvents.LeftDirection -> {
                _positionList.add(Direction.LEFT)
                if (_positionList.size > 2) {
                    _positionList.removeAt(1)
                }
            }

            is PacmanEvents.UpDirection -> {
                _positionList.add(Direction.UP)
                if (_positionList.size > 2) {
                    _positionList.removeAt(1)
                }
            }

            is PacmanEvents.DownDirection -> {
                _positionList.add(Direction.DOWN)
                if (_positionList.size > 2) {
                    _positionList.removeAt(1)
                }
            }

            is PacmanEvents.Start->{
                gameIsStarted.value = true
                startGame()
            }

            is PacmanEvents.Stop->{
                gameIsStarted.value = false
                stopGame()
            }

            is PacmanEvents.Pause->{
                gameIsPaused.value = true
                pacmanGame.onPause()
            }

            is PacmanEvents.Resume->{
                gameIsPaused.value = false
                pacmanGame.onResume()
            }

            is PacmanEvents.MuteSounds->{
                gameIsMute.value = true
                pacmanGame.muteSounds()
            }

            is PacmanEvents.RecoverSounds->{
                gameIsMute.value = false
                pacmanGame.recoverSounds()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pacmanGame.stopGame()
    }
}