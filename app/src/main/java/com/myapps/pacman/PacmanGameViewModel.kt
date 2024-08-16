package com.myapps.pacman

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.game.PacmanGame
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.levels.LevelsData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PacmanGameViewModel @Inject constructor(
   @ApplicationContext context:Context
) : ViewModel() {

    private val _positionList = mutableListOf(Direction.RIGHT)
    private val pacmanGame = PacmanGame(context)
    private val _pacmanData = MutableStateFlow(PacmanData())
    private val _blinkyData = MutableStateFlow(GhostData())
    private val _inkyData = MutableStateFlow(GhostData())
    private val _pinkyData = MutableStateFlow(GhostData())
    private val _clydeData = MutableStateFlow(GhostData())
    private val _mapBoardData = MutableStateFlow(BoardData())

    val pacmanData:StateFlow<PacmanData> get() = _pacmanData
    val blinkyData:StateFlow<GhostData> get() = _blinkyData
    val inkyData:StateFlow<GhostData> get() = _inkyData
    val pinkyData:StateFlow<GhostData> get() = _pinkyData
    val clydeData:StateFlow<GhostData> get() = _clydeData
    val mapBoardData:StateFlow<BoardData> get() = _mapBoardData

    private fun startGame(){
        pacmanGame.initGame(_positionList)
        collectMapBoardData()
        collectPacmanData()
        collectPinkyData()
        collectBlinkyData()
        collectInkyData()
        collectClydeData()
    }

    private fun stopGame(){
        pacmanGame.stopGame()
        _positionList.clear()
        _positionList.add(Direction.RIGHT)
    }

    private fun collectMapBoardData(){
        viewModelScope.launch {
            pacmanGame.mapBoardData.collect{
                _mapBoardData.value = it
            }
        }
    }

    private fun collectPacmanData(){
        viewModelScope.launch{
            pacmanGame.pacmanState.collect{
                _pacmanData.value = it
            }
        }
    }
    private fun collectBlinkyData(){
        viewModelScope.launch {
            pacmanGame.blinkyState.collect{
                _blinkyData.value = it
            }
        }
    }private fun collectInkyData(){
        viewModelScope.launch {
            pacmanGame.inkyState.collect{
                _inkyData.value = it
            }
        }
    }
    private fun collectPinkyData(){
        viewModelScope.launch {
            pacmanGame.pinkyState.collect{
                _pinkyData.value = it
            }
        }
    }
    private fun collectClydeData(){
        viewModelScope.launch {
            pacmanGame.clydeState.collect{
                _clydeData.value = it
            }
        }
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
               startGame()
            }

            is PacmanEvents.Stop->{
                stopGame()
            }

            is PacmanEvents.Pause->{
                pacmanGame.onPause()
            }

            is PacmanEvents.Resume->{
                pacmanGame.onResume()
            }

            is PacmanEvents.MuteSounds->{
                pacmanGame.muteSounds()
            }

            is PacmanEvents.RecoverSounds->{
                pacmanGame.recoverSounds()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pacmanGame.stopGame()
    }
}
