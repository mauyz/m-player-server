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
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Mauyz
 */
public class ErrorsController implements Initializable {
    
    private Stage stage;
    
    @FXML
    private TextArea errorsTxt;

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorsTxt.setStyle("-fx-text-fill: red;");
        // TODO
    }    

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    @FXML
    private void closeBtnAction(ActionEvent event) {
        stage.close();
    }
    
    public void appendError(String error){
        errorsTxt.appendText(error+"\n\n");
    }

    @FXML
    private void clearBtnAction(ActionEvent event) {
        errorsTxt.clear();
    }
}
