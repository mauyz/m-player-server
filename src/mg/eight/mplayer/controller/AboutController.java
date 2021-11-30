/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Mauyz
 */
public class AboutController implements Initializable {

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    
    private Stage stage;
    private HostServices hostServices;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
    
    private void browse(String url, ActionEvent event) {
        hostServices.showDocument(url);
        ((Hyperlink) event.getSource()).setVisited(false);
    }

    @FXML
    private void closeBtnAction(ActionEvent event) {
        stage.close();
    }

    @FXML
    private void fbLinkAction(ActionEvent event) {
        
        browse("https://www.facebook.com/pierremoise.tsiorinambinina", event);
    }

    @FXML
    private void googleLinkAction(ActionEvent event) {
        browse("https://mail.google.com", event);
    }

}
