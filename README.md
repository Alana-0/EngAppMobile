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


#### Here some screenshots of the app (the ten levels)
![level_one](https://github.com/user-attachments/assets/2b972a17-fb94-44c9-85c7-447d862bcfec)
![level_two](https://github.com/user-attachments/assets/9a1ab055-ee46-4779-9ead-da81d81e0ab9)
![level_three](https://github.com/user-attachments/assets/6458af0b-14e5-410b-9269-92cc9c49e7e0)
![level_four](https://github.com/user-attachments/assets/15544fb7-bd26-462b-9ef8-807617d49cdf)
![level_five](https://github.com/user-attachments/assets/7ec39a30-38ab-4d30-a615-cca35511daf4)
![level_six](https://github.com/user-attachments/assets/0369d7df-0c4b-46c7-a0d3-9ec8426e7860)
![level_seven](https://github.com/user-attachments/assets/55a6bbb9-86ba-455d-b896-230e23e2d0ee)
![level_eight](https://github.com/user-attachments/assets/f5d934a4-3aca-4848-9dc1-1247e9a1418e)
![level_nine](https://github.com/user-attachments/assets/fde86a93-33b7-4ad9-a2ed-87dcebba0539)
![level_ten](https://github.com/user-attachments/assets/ff4a3a5e-2819-4122-8247-3731ae126a8c)

## Tech Stack Used And Architectural pattern ⚙️
* XML - For the views
* MVVM(Model-View-ViewModel) - Main Architecture pattern
* Kotlin - The main language
* Coroutines and Flows

## Authors ✒️

* **Mauro Serantes** - [Mauro Serantes](https://github.com/MauroSerantes)
