package multiplayergame;

import java.awt.Color;

public class Player {
    long id;
    String name;
    Color color;
    int x;
    int y;
    
    public Player(long ID, String name, Color c, int x, int y)
    {
        this.id = ID;
        this.name = name;
        this.color = c;
        this.x = x;
        this.y = y;
    }
    
}
