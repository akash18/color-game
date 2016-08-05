------------
Introduction
------------

ColorGame is a multiplayer web game, where a player can join or start a new game. All player will be assigned a random color.
When the player opts to join a game, then he will be added in the game with least number of players.They have to color the 
maximum number of blocks in given constraints to win the game. Use help button in the application for more detailed rules.


---------------------
Technology Stack Used
---------------------

IDE- NetBeans
BackEnd Language- Java- 1.8 version
Server- GlassFish
UI- HTML5, Jsp, JavaScripts
UI Framework- Twitter Bootstrap
Communication- WebSockets (Javax)
Game configurations- properties file (config.properties) 

-------------
How to run it
-------------

Delpoy the game using a glassfish server and you are good to go.

-------
Design
-------

The client side mainly comprises of HTML5,JSPs designed using bootstrap framework.To make UI more responsive and reuse the code, 
choosed to use div show/hide feature.Javascript is being used as client side scripting langauge which responds to different events 
and interact with the backEnd using sockets (websockets.js).Java is used as the backend language were all the game logic is written.
We have different handlers for handling Players and Game. The state of the game is maitained in Game model and that of the player in 
the Player model. The player is maintained via sessions in the game using Maps. All updates in game are broadcasted through the backend
to all connected sessions of that game.

------------------
Issues faced
------------------

There was a minor delay while sending the messages from server to client, so I overcame it by using Async Remote End Point instead of Basic 
Remote End Point. Async Remote End Point creates a new thread to send a message instead of using the main thread which is used by Basic 
Remote End Point.

-----------------
Known issues
-----------------

Google Chrome browser has a feature to slow down the javascript of inactive tabs, so if a player opens 3 tabs in a chrome to play the game, 
then there might be delay while unlocking the UI as jQuery is used for locking the UI. 


Enjoy the game. 
