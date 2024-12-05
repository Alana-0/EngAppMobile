package com.myapps.pacman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.myapps.pacman.databinding.ActivityMainBinding
import com.myapps.pacman.ui.GameMainView
import com.myapps.pacman.ui.PacmanSurfaceView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


// Inicia a atividade com a injeção de dependência do Hilt
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel:PacmanGameViewModel by viewModels()

    private lateinit var binding:ActivityMainBinding

    // Método que é chamado quando a atividade é criada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureDownButton()
        configureLeftButton()
        configureRightButton()
        configureUpButton()
        configureStartResetButton()

        configurePauseResumeButton()

        lifecycleScope.launch {
            viewModel.gameIsStarted.collect{
                if(it){
                    binding.pacmanView.setActiveGameView(true) // Ativa a visualização do jogo
                    // Atualiza os dados do jogo com informações do ViewModel
                    binding.pacmanView.setGameBoardData(viewModel.mapBoardData)
                    binding.pacmanView.setPacmanData(viewModel.pacmanData)
                    binding.pacmanView.setBlinkyData(viewModel.blinkyData)
                    binding.pacmanView.setInkyData(viewModel.inkyData)
                    binding.pacmanView.setPinkyData(viewModel.pinkyData)
                    binding.pacmanView.setClydeData(viewModel.clydeData)
                }
                else{
                    binding.pacmanView.setActiveGameView(false) // Desativa a visualização do jogo
                    binding.pacmanView.stopAllCurrentJobs() // Para todas as tarefas em andamento
                }
            }
        }
        // Observa o estado de "gameIsMute" e ajusta o status de som do jogo
        lifecycleScope.launch {
            viewModel.gameIsMute.collect{
                if(it){
                    binding.pacmanView.changeSoundGameStatus(true)
                }
                else{
                    binding.pacmanView.changeSoundGameStatus(false)
                }
            }
        }
        // Observa o estado de "gameIsPaused" e ajusta o status de pausa do jogo
        lifecycleScope.launch{
            viewModel.gameIsPaused.collect{
                if(it){
                    binding.pacmanView.changePauseGameStatus(true)// Pausa o jogo
                    binding.pacmanView.pauseGameDraw() // Pausa a renderização do jogo
                }
                else{
                    binding.pacmanView.changePauseGameStatus(false) // Retoma o jogo
                    binding.pacmanView.resumeGameDraw() // Retoma a renderização do jogo
                }
            }
        }
    }

    // Configura o botão de movimentação para cima
    private fun configureUpButton(){
        binding.upButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.UpDirection)
        }
    }

    // Configura o botão de movimentação para baixo
    private fun configureDownButton(){
        binding.downButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.DownDirection)
        }
    }

    // Configura o botão de movimentação para a direita
    private fun configureRightButton(){
        binding.rightButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.RightDirection)
        }
    }

    // Configura o botão de movimentação para a esquerda
    private fun configureLeftButton(){
        binding.leftButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.LeftDirection)
        }
    }

    // Configura o botão de iniciar/zerar o jogo
    private fun configureStartResetButton() {
        binding.startResetButton.setOnClickListener {
            binding.startResetButton.isEnabled = false

            if (viewModel.gameIsStarted.value) {
                viewModel.onEvents(PacmanEvents.Stop)
            } else {
                viewModel.onEvents(PacmanEvents.Start)
            }

            binding.startResetButton.postDelayed({
                binding.startResetButton.isEnabled = true
            }, 1000)
        }
    }

    // Configura o botão de pausa/retomar o jogo
    private fun configurePauseResumeButton(){
        binding.pauseResetButton.setOnClickListener {
            binding.pauseResetButton.isEnabled = false
            if(viewModel.gameIsPaused.value){
                viewModel.onEvents(PacmanEvents.Resume)
            }
            else{
                viewModel.onEvents(PacmanEvents.Pause)
            }
            binding.pauseResetButton.postDelayed({
                binding.pauseResetButton.isEnabled = true
            }, 500)
        }
    }



    // Método chamado quando a atividade é destruída, garantindo que o jogo seja parado e os recursos sejam liberados
    override fun onDestroy() {
        super.onDestroy()
        viewModel.onEvents(PacmanEvents.Stop)
        binding.pacmanView.stopAllCurrentJobs()
        binding.pacmanView.stopDrawJob()
    }

}