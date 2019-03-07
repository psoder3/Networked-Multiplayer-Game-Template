/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multiplayergame;

import java.awt.Color;

/**
 *
 * @author psoderquist
 */
public class ClientAction {
    int direction;
    long id;
    Color color;
    
    // A constructor for simple action of polling server
    // This type is used by server to know whether client has disconnected
    public ClientAction(long id)
    {
        this.id = id;
    }
    
    // A constructor for an action that moves the player
    public ClientAction(long id, int direction)
    {
        this.direction = direction;
        this.id = id;
    }
    
    // A constructor for an action that changes the player's color
    public ClientAction(long id, Color color)
    {
        this.color = color;
        this.id = id;
    }
    
}
