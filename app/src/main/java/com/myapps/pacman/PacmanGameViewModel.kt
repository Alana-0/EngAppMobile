package com.myapps.pacman

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapps.pacman.flowData.BoardData
import com.myapps.pacman.flowData.GhostData
import com.myapps.pacman.flowData.PacmanData
import com.myapps.pacman.game.Game
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.LevelsData
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PacmanGameViewModel : ViewModel() {

    private val _positionList = mutableListOf(Direction.RIGHT)
    private val _pacmanData = MutableStateFlow(
        PacmanData(Position(-1, -1), Direction.RIGHT, false, movementsDelay = 0L)
    )
    private val _pacmanMapping = MutableStateFlow(BoardData(Matrix(0,0), 0 ,3, isBell = false))

    private val _blinkyData = MutableStateFlow(
        GhostData(Position(-1, -1), Direction.NOWHERE)
    )
    private val _inkyData = MutableStateFlow(
        GhostData(Position(-1, -1), Direction.NOWHERE)
    )
    private val _pinkyData = MutableStateFlow(
        GhostData(Position(-1, -1), Direction.NOWHERE)
    )
    private val _clydeData = MutableStateFlow(
        GhostData(Position(-1, -1), Direction.NOWHERE)
    )

    private val pacmanGame = Game(LevelsData.pacmanGame)

    val pacmanPos: StateFlow<PacmanData> get() = _pacmanData
    val pacMapping: StateFlow<BoardData> get() = _pacmanMapping
    val blinkyPos: StateFlow<GhostData> get() = _blinkyData
    val inkyPos: StateFlow<GhostData> get() = _inkyData
    val pinkyPos: StateFlow<GhostData> get() = _pinkyData
    val clydePos: StateFlow<GhostData> get() = _clydeData

    private fun startGame(){
        pacmanGame.initGame(_positionList)
        collectMapData()
        collectPacmanData()
        collectBlinkyData()
        collectInkyData()
        collectPinkyData()
        collectClydeData()
    }

    private fun stopGame(){
        pacmanGame.stopGame()
        _positionList.clear()
        _positionList.add(Direction.RIGHT)
    }

    private fun collectPacmanData() {
        viewModelScope.launch {
            pacmanGame.pacmanPosition.collect {
                _pacmanData.value = PacmanData(
                    it.currentPosition,
                    it.direction,
                    it.energizerStatus,
                    it.movementsDelay
                )
            }
        }
    }

    private fun collectMapData() {
        viewModelScope.launch {
            pacmanGame.mapFlow.collect {
                _pacmanMapping.value = it
            }
        }

    }

    private fun collectBlinkyData() {
        viewModelScope.launch {
            pacmanGame.blinkyPosition.collect {
                _blinkyData.value = GhostData(
                    it.currentPosition,
                    it.direction,
                    it.lifeStatement,
                    it.movementsDelay
                )
            }
        }
    }

    private fun collectInkyData() {
        viewModelScope.launch {
            pacmanGame.inkyPosition.collect {
                _inkyData.value = GhostData(
                    it.currentPosition,
                    it.direction,
                    it.lifeStatement,
                    it.movementsDelay
                )
            }
        }
    }

    private fun collectPinkyData() {
        viewModelScope.launch {
            pacmanGame.pinkyPosition.collect {
                _pinkyData.value = GhostData(
                    it.currentPosition,
                    it.direction,
                    it.lifeStatement,
                    it.movementsDelay
                )
            }
        }
    }

    private fun collectClydeData() {
        viewModelScope.launch {
            pacmanGame.clydePosition.collect {
                _clydeData.value = GhostData(
                    it.currentPosition,
                    it.direction,
                    it.lifeStatement,
                    it.movementsDelay
                )
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
        }
    }

    override fun onCleared() {
        super.onCleared()
        pacmanGame.stopGame()
    }
}
