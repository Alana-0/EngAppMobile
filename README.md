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

![level_one](https://github.com/user-attachments/assets/6db4bd9d-3931-4758-9dea-1bbabcc13b84)
![level_two](https://github.com/user-attachments/assets/6bcd3a0a-f212-4cd3-8649-772bd2fbd980)
![level_three](https://github.com/user-attachments/assets/05d9be40-575f-47cc-b483-3e02881eadcb)
![level_four](https://github.com/user-attachments/assets/19b0ee5c-39a7-4c13-b9d5-f1e1bd15458e)
![level_five](https://github.com/user-attachments/assets/9a9896b7-bbe5-431a-883f-2aae50ce384e)
![level_six](https://github.com/user-attachments/assets/da6cb919-5478-416f-8111-be3e1d662b7b)
![level_seven](https://github.com/user-attachments/assets/e1443143-076b-4d2b-9e57-42dd70ae0767)
![level_eight](https://github.com/user-attachments/assets/8eb7f7da-a2cf-4908-b203-80ad6805dcbc)
![level_nine](https://github.com/user-attachments/assets/42cf4163-87ac-48c5-a1c9-d725e3af236c)
![level_ten](https://github.com/user-attachments/assets/1dd04fe0-2659-4ff1-8700-f76db2898dd3)



## Tech Stack Used And Architectural pattern ⚙️
* XML - For the views
* MVVM(Model-View-ViewModel) - Main Architecture pattern
* Kotlin - The main language
* Coroutines and Flows

## Authors ✒️

* **Mauro Serantes** - [Mauro Serantes](https://github.com/MauroSerantes)
