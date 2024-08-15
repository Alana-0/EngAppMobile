package com.myapps.pacman

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myapps.pacman.databinding.ActivityMainBinding
import com.myapps.pacman.sound.SoundService
import com.myapps.pacman.ui.GameMainView
import com.myapps.pacman.ui.PacmanView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<PacmanGameViewModel>()

    private lateinit var binding:ActivityMainBinding
    private lateinit var pacmanView: PacmanView
    private lateinit var gameMainView: GameMainView
    private var gameIsStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SoundService.init(this)
        pacmanView = PacmanView(this)
        gameMainView = GameMainView(this)

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.pacmanView.removeAllViews()
        binding.pacmanView.addView(gameMainView,layoutParams)

        lifecycleScope.launch {
            pacmanView.setGameMapFlow(viewModel.pacMapping)
            pacmanView.setBlinkyFlow(viewModel.blinkyPos)
            pacmanView.setInkyFlow(viewModel.inkyPos)
            pacmanView.setClydeFlow(viewModel.clydePos)
            pacmanView.setPinkyFlow(viewModel.pinkyPos)
            pacmanView.setPacmanData(viewModel.pacmanPos)
        }

        configureDownButton()
        configureLeftButton()
        configureRightButton()
        configureUpButton()
        configureStartButton()
        configureStopButton()
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

    private fun configureStartButton(){
        binding.start.setOnClickListener {
            if(!gameIsStarted){
                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                viewModel.onEvents(PacmanEvents.Start)
                binding.pacmanView.removeAllViews()
                binding.pacmanView.addView(pacmanView,layoutParams)
                gameIsStarted = true
            }
        }
    }

    private fun configureStopButton(){
        binding.stopGame.setOnClickListener {
            if(gameIsStarted){
                val layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                viewModel.onEvents(PacmanEvents.Stop)
                pacmanView.stopAllInterpolationJobs()
                binding.pacmanView.removeAllViews()
                binding.pacmanView.addView(gameMainView,layoutParams)
                gameIsStarted = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundService.release()
    }
}