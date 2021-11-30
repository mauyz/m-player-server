/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mg.eight.mplayer.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mg.eight.mplayer.model.Shortcut;

/**
 * FXML Controller class
 *
 * @author Mauyz
 */

public class ManualController implements Initializable {
    
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    
    private Stage stage;
    
    @FXML
    private TableView<Shortcut> shortcutTable;
    @FXML
    private TableColumn<Shortcut, String> action;
    @FXML
    private TableColumn<Shortcut, String> value;
    @FXML
    private TextArea manualTxt;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        manualTxt.setText("Add and play media files from the source using Default playlist.\n\n"
                +"You can also manage many playlists.\n\n"
                +"You are able to change your playlist every time.\n\n"
                +"Change the playing mode using random or repeat menu.\n\n"
                +"Start the socket and specify a port to use for a remote command\nfrom an android client.\n\n"
                +"Make sure that your firewall allows Java to send and to recieve packets.\n\n"
                +"Open the application in your mobile, set the ip and port server.\n\n"
                +"Now you can manage your player from your mobile and enjoy !!\n");
        action.setCellValueFactory(new PropertyValueFactory<>("action"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        shortcutTable.getItems().addAll(new Shortcut("Open file(s)", "Ctrl+O")
                ,new Shortcut("Open folder(s)", "Ctrl+F")
                ,new Shortcut("Quit", "Ctrl+Q")
                ,new Shortcut("Show manual", "F1")
                ,new Shortcut("About", "Ctrl+I")
                ,new Shortcut("Add file(s)", "Shift+O")
                ,new Shortcut("Add folder(s)", "Shift+F")
                ,new Shortcut("Play next", "Shift+X")
                ,new Shortcut("Remove item(s)", "Delete")
                ,new Shortcut("Clear the playlist", "Shift+V")
                ,new Shortcut("Add playlist", "Shift+A")
                ,new Shortcut("Rename playlist", "Shift+R")
                ,new Shortcut("Delete playlist", "Shift+D")
                ,new Shortcut("Play/Pause", "Space")
                ,new Shortcut("Stop", "S")
                ,new Shortcut("Previous", "P")
                ,new Shortcut("Next", "N")
                ,new Shortcut("Enable/disable mute", "M")
                ,new Shortcut("Enable/disable random", "R")
                ,new Shortcut("Normal/Repeat one/Repeat all", "B")
                ,new Shortcut("Show/hide playlist", "L")
                ,new Shortcut("Volume up", "+")
                ,new Shortcut("Volume down", "-")
                ,new Shortcut("Start/stop server", "C")
                ,new Shortcut("Exit full screen", "Escape"));
    }    

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    private void closeBtnAction(ActionEvent event) {
        stage.close();
    }
    
}
