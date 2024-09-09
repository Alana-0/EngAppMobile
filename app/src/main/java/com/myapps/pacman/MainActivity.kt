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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel:PacmanGameViewModel by viewModels()

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureDownButton()
        configureLeftButton()
        configureRightButton()
        configureUpButton()
        configureStartResetButton()
        configureSoundButton()
        configurePauseResumeButton()

        lifecycleScope.launch {
            viewModel.gameIsStarted.collect{
                if(it){
                    binding.pacmanView.setActiveGameView(false)
                    binding.pacmanView.stopAllCurrentJobs()
                }
                else{
                    binding.pacmanView.setActiveGameView(true)
                    binding.pacmanView.setGameBoardData(viewModel.mapBoardData)
                    binding.pacmanView.setPacmanData(viewModel.pacmanData)
                    binding.pacmanView.setBlinkyData(viewModel.blinkyData)
                    binding.pacmanView.setInkyData(viewModel.inkyData)
                    binding.pacmanView.setPinkyData(viewModel.pinkyData)
                    binding.pacmanView.setClydeData(viewModel.clydeData)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.gameIsMute.collect{
                if(it){
                    binding.pacmanView.changeSoundGameStatus(false)
                }
                else{
                    binding.pacmanView.changeSoundGameStatus(true)
                }
            }
        }
        lifecycleScope.launch{
            viewModel.gameIsPaused.collect{
                if(it){
                    binding.pacmanView.changePauseGameStatus(false)
                    binding.pacmanView.resumeGameDraw()
                }
                else{
                    binding.pacmanView.changePauseGameStatus(true)
                    binding.pacmanView.pauseGameDraw()
                }
            }
        }
    }

    private fun configureUpButton(){
        binding.upButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.UpDirection)
        }
    }

    private fun configureDownButton(){
        binding.downButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.DownDirection)
        }
    }

    private fun configureRightButton(){
        binding.rightButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.RightDirection)
        }
    }

    private fun configureLeftButton(){
        binding.leftButton.setOnClickListener {
            viewModel.onEvents(PacmanEvents.LeftDirection)
        }
    }

    private fun configureStartResetButton() {
        binding.startResetButton.setOnClickListener {
            binding.startResetButton.isEnabled = false

            if (viewModel.gameIsStarted.value) {
                viewModel.onEvents(PacmanEvents.Stop)
            } else {
                viewModel.onEvents(PacmanEvents.Start)
            }

            binding.startResetButton.postDelayed({
                binding.startResetButton.isEnabled = true // Reactiva el botón después de 500ms
            }, 1000)
        }
    }

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


    private fun configureSoundButton(){
        binding.soundButton.setOnClickListener {
            binding.soundButton.isEnabled = false
            if(viewModel.gameIsMute.value){
                viewModel.onEvents(PacmanEvents.RecoverSounds)
            }
            else{
                viewModel.onEvents(PacmanEvents.MuteSounds)
            }
            binding.soundButton.postDelayed({
                binding.soundButton.isEnabled = true
            }, 500)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onEvents(PacmanEvents.Stop)
        binding.pacmanView.stopAllCurrentJobs()
        binding.pacmanView.stopDrawJob()
    }

}