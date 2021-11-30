/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mg.eight.mplayer.model;

/**
 *
 * @author Mauyz
 */
public class Shortcut {
    
    private final String action;
    private final String value;

    public Shortcut(String action, String value) {
        this.action = action;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }
        
}
