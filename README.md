# Pacman Game

_This is a implementation of the clasical Pacman arcade game for android. Although it is a personal version,
all the main mechanics are the same than the original version._

## About the game
The game contains ten different levels, all of them with its own map. All the maps are full with pellets and 
four energizers (from level one to seven) wichs ones pacman should eat.
Pacman has three lives, if it loses all of them, the game is lost. 
In five of the ten levels, the red ghost will increase his speed and the only way for pacman to match that speed is 
eating the five bells that will appear in some levels.

## About the implementation of the game 📋
When i propouse myself to develop this game for android, i tried to make it simple avoiding some libraries like Open GL or SDL.
At the end i decided to use some of the native tools that kotlin provides. In conclusion the game was developed using coroutines
and flows in order to control the state of the game and the Canvas class for the ui.
There is one activity wich contains six buttons: up,right,left,down,start and stop.


#### Here some screenshots of the app

![level_one](https://github.com/user-attachments/assets/ef65f0a9-7092-4a99-9778-c49524b52387)
![level_two](https://github.com/user-attachments/assets/64afe0d1-4da1-4f62-9da0-a3ce59a4b787)
![level_three](https://github.com/user-attachments/assets/63904cfb-e347-4ead-abf9-497ea6694d1e)
![level_four](https://github.com/user-attachments/assets/1d2f8945-e107-4922-a1c6-df475e6d4606)
![level_five](https://github.com/user-attachments/assets/295d1e7a-6863-44b4-935a-22b35c5f9ee6)
![level_six](https://github.com/user-attachments/assets/544125f4-256f-4969-b738-4b0e8e4334d8)
![level_seven](https://github.com/user-attachments/assets/0d10c0f2-515c-4cab-b688-e0eb69e0cb8a)
![level_eight](https://github.com/user-attachments/assets/466fe26b-579f-4314-a71c-244b4392276f)
![level_nine](https://github.com/user-attachments/assets/e9d4f581-1bd8-4010-b295-9f2ca96277fe)
![level_ten](https://github.com/user-attachments/assets/fe5e3566-d475-48bc-96a0-0a287011a773)



## Tech Stack Used And Architectural pattern ⚙️
* XML - For the views
* MVVM(Model-View-ViewModel) - Main Architecture pattern
* Kotlin - The main language
* Coroutines and Flows

## Authors ✒️

* **Mauro Serantes** - [Mauro Serantes](https://github.com/MauroSerantes)
