package com.myapps.pacman.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.myapps.pacman.R

object SoundService {

    private lateinit var audioAttributes:AudioAttributes
    private lateinit var soundPool: SoundPool
    private lateinit var soundMap: MutableMap<Int, Int>

    fun init(context: Context){
        audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(5)
            .build()
        soundMap = mutableMapOf()
        loadSounds(context)
    }

    private fun loadSounds(context: Context) {
        soundMap[R.raw.pacman_intro] = soundPool.load(context, R.raw.pacman_intro, 1)
        soundMap[R.raw.ghost_siren] = soundPool.load(context, R.raw.ghost_siren, 1)
        soundMap[R.raw.pacman_energizer_mode] = soundPool.load(context, R.raw.pacman_energizer_mode, 1)
        soundMap[R.raw.pacman_eatghost] = soundPool.load(context, R.raw.pacman_eatghost, 1)
        soundMap[R.raw.pacman_eating_fruit] = soundPool.load(context, R.raw.pacman_eating_fruit, 1)
        soundMap[R.raw.pacman_death] = soundPool.load(context, R.raw.pacman_death, 1)
        soundMap[R.raw.pacman_eating_pellet] = soundPool.load(context,R.raw.pacman_eating_pellet,1)
        soundMap[R.raw.pacman_extra_life] = soundPool.load(context,R.raw.pacman_extra_life,1)
    }

    fun playSound(soundId: Int, isLoop: Boolean,rate:Float,rightVolume:Float,leftVolume:Float) {
        val sound = soundMap[soundId]
        sound?.let {
            soundPool.play(
                it,
                leftVolume,
                rightVolume,
                0,
                if (isLoop) -1 else 0,
                rate
            )
        }
    }

    fun pauseSound(soundId: Int) {
        val sound = soundMap[soundId]
        sound?.let {
            soundPool.pause(it)
        }
    }

    fun stopSound(soundId: Int){
        val sound = soundMap[soundId]
        sound?.let {
            soundPool.stop(it)
        }
    }

    fun release() {
        soundPool.release()
    }
}