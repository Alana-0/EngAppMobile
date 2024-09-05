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
![level_1](https://github.com/user-attachments/assets/d7bd8345-0d9c-4a6c-a4e6-71c83155c893)
![level_2](https://github.com/user-attachments/assets/4cd4b5de-d3c3-4282-a2d7-edfaef6bc036)
![level_3](https://github.com/user-attachments/assets/db87b007-1e35-4420-933b-2133ba9da33e)
![level_4](https://github.com/user-attachments/assets/98f716c3-216e-4609-b08e-9b8ffbfffe9b)
![level_5](https://github.com/user-attachments/assets/f1e3dfa3-8dae-4b4c-bd16-9f958832f50b)
![level_6](https://github.com/user-attachments/assets/7604bf73-8b12-47e4-adba-11cac7472e1a)
![level_7](https://github.com/user-attachments/assets/b010cfee-7514-450f-9a58-d29b9ce05081)
![level_8](https://github.com/user-attachments/assets/af45e295-f768-411b-a09e-88ffc6963ec9)
![level_9](https://github.com/user-attachments/assets/5a8a236a-91a0-4bcb-9693-a3ef056a4f6f)
![level_10](https://github.com/user-attachments/assets/8480d9b4-16c8-407c-b491-f00afd8c9945)







## Tech Stack Used And Architectural pattern ⚙️
* XML - For the views
* MVVM(Model-View-ViewModel) - Main Architecture pattern
* Kotlin - The main language
* Coroutines and Flows
* JSON files for the data of the levels

## Authors ✒️

* **Mauro Serantes** - [Mauro Serantes](https://github.com/MauroSerantes)
