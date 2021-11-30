/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.model;

import java.util.Arrays;
import mg.eight.mplayer.manager.AppManager;

/**
 *
 * @author Mauyz
 */
public class Setting {

    private static Setting instance;

    private boolean remote = true;
    private int port = 9999;
    private String playlist = "Default";
    private String repeat = "No repeat";
    private boolean random = false;
    private double volume = 50.0;
    private boolean mute = false;

    private Setting() {
    }

    public static Setting getInstance() {
        if (instance == null) {
            instance = new Setting();
        }
        return instance;
    }

    public String getPlaylist() {
        return playlist;
    }

    public Setting setPlaylist(String playlist) {
        if (!Arrays.asList(AppManager.getPlaylists()).contains(playlist)) {
            this.playlist = "Default";
            return instance;
        }
        this.playlist = playlist;
        return instance;
    }

    public boolean isRemote() {
        return remote;
    }

    public boolean isRandom() {
        return random;
    }

    public Setting setRandom(boolean random) {
        this.random = random;
        return instance;
    }

    public Setting setRandom(String random) {
        if (!random.equalsIgnoreCase("true")
                && !random.equalsIgnoreCase("false")) {
            this.random = false;
            return instance;
        }
        this.random = Boolean.valueOf(random);
        return instance;
    }

    public String getRandom() {
        return random ? "Random" : "Normal";
    }

    public boolean getRandom(String mode) {
        return mode.equals("Random");
    }

    public String getRepeat() {
        return repeat;
    }

    public Setting setRemote(boolean remote) {
        this.remote = remote;
        return instance;
    }

    public Setting setRemote(String remote) {
        if (!remote.equalsIgnoreCase("true")
                && !remote.equalsIgnoreCase("false")) {
            this.remote = true;
            return instance;
        }
        this.remote = Boolean.valueOf(remote);
        return instance;
    }

    public int getPort() {
        return port;
    }

    public Setting setPort(int port) {
        if (port < 1000) {
            this.port = 9999;
            return instance;
        }
        this.port = port;
        return instance;
    }

    public Setting setRepeat(String repeat) {
        if (!repeat.equalsIgnoreCase("No repeat")
                && !repeat.equalsIgnoreCase("Repeat one")
                && !repeat.equalsIgnoreCase("Repeat all")) {
            this.repeat = "No Repeat";
            return instance;
        }
        this.repeat = repeat;
        return instance;
    }

    public double getVolume() {
        return volume;
    }

    public Setting setVolume(double volume) {
        if (volume < 0 || volume > 100) {
            this.volume = 50;
            return instance;
        }
        this.volume = volume;
        return instance;
    }

    public boolean isMute() {
        return mute;
    }

    public Setting setMute(boolean mute) {
        this.mute = mute;
        return instance;
    }
}
