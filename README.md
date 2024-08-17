# Pacman Game

_This is a implementation of the clasical Pacman arcade game for android. Although it is a personal version,
all the main mechanics are the same than the original version._

## About the game
The game contains ten different levels, all of them with its own map. All the maps are full with pellets and 
four energizers (from level one to eight) wichs ones pacman should eat.
Pacman has three lives, if it loses all of them, the game is lost. 
In five of the ten levels, the red ghost will increase his speed and the only way for pacman to match that speed is 
eating the five bells that will appear in some levels.

## About the implementation of the game 📋
When i propouse myself to develop this game for android, i tried to make it simple avoiding some libraries like Open GL or SDL.
At the end i decided to use some of the native tools that kotlin provides. In conclusion the game was developed using coroutines
and flows in order to control the state of the game and the Canvas class for the ui.
There is one activity wich contains six buttons: up,right,left,down,start and stop.


#### Here some screenshots of the app





## Tech Stack Used And Architectural pattern ⚙️
* XML - For the views
* MVVM(Model-View-ViewModel) - Main Architecture pattern
* Kotlin - The main language
* Coroutines and Flows

## Authors ✒️

* **Mauro Serantes** - [Mauro Serantes](https://github.com/MauroSerantes)
