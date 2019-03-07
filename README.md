# Networked-Multiplayer-Game-Template

This is a project meant to serve as a template for students hoping to create a networked multiplayer game in Java.

To use:
- download zipped folder and extract the contents.
- open existing project in NetBeans
- change ip on line that says: String HOST = "10.65.0.128"; to be the ip of computer on which you'll run the server.
- run Server.java
- run Client.java from as many computers as you want


This application uses a technique called "polling". Basically, the server has a master list of the status of all elements in the game. The client program simply requests or "polls" the server for updates every few milliseconds and updates the view accordingly.

Right now, all the application does is allow connected clients to:
1. choose a name to identify themselves by on the server
2. connect to a server
3. control the movement of their square
4. change their color

See if you can make something cooler than that such as an IO game like Paper.io or Powerline.io
