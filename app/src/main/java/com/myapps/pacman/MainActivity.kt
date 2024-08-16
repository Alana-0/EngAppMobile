package com.myapps.pacman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.myapps.pacman.databinding.ActivityMainBinding
import com.myapps.pacman.sound.PacmanSoundService
import com.myapps.pacman.ui.GameMainView
import com.myapps.pacman.ui.PacmanView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<PacmanGameViewModel>()

    private lateinit var binding:ActivityMainBinding
    private lateinit var pacmanView: PacmanView
    private lateinit var gameMainView: GameMainView
    private var isSoundMute = false
    private var gameIsStarted = false
    private var gameIsPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pacmanView = PacmanView(this)
        gameMainView = GameMainView(this)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.pacmanView.removeAllViews()
        binding.pacmanView.addView(gameMainView,layoutParams)

        configureDownButton()
        configureLeftButton()
        configureRightButton()
        configureUpButton()
        configureStartResetButton()
        configureSoundButton()
        configurePauseResumeButton()
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

    private fun configureStartResetButton(){
        binding.startResetButton.setOnClickListener {
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            gameIsStarted = if(gameIsStarted){
                viewModel.onEvents(PacmanEvents.Stop)
                binding.pacmanView.removeAllViews()
                binding.pacmanView.addView(gameMainView,layoutParams)
                false
            } else{
                viewModel.onEvents(PacmanEvents.Start)
                binding.pacmanView.removeAllViews()
                binding.pacmanView.addView(pacmanView,layoutParams)
                pacmanView.setGameBoardData(viewModel.mapBoardData)
                pacmanView.setPacmanData(viewModel.pacmanData)
                pacmanView.setBlinkyData(viewModel.blinkyData)
                pacmanView.setInkyData(viewModel.inkyData)
                pacmanView.setPinkyData(viewModel.pinkyData)
                pacmanView.setClydeData(viewModel.clydeData)
                true
            }
        }
    }

    private fun configurePauseResumeButton(){
        binding.pauseResetButton.setOnClickListener {
            gameIsPaused = !gameIsPaused
            pacmanView.changePauseGameStatus(gameIsPaused)

            if(gameIsPaused){
                viewModel.onEvents(PacmanEvents.Pause)
            }
            else{
                viewModel.onEvents(PacmanEvents.Resume)
            }
        }
    }


    private fun configureSoundButton(){
        binding.soundButton.setOnClickListener {
            isSoundMute = !isSoundMute
            pacmanView.changeSoundGameStatus(isSoundMute)
            if(!isSoundMute){
                viewModel.onEvents(PacmanEvents.RecoverSounds)
            } else{
                viewModel.onEvents(PacmanEvents.MuteSounds)
            }
        }
    }

}