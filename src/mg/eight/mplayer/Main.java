
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mg.eight.mplayer;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.JOptionPane;
import mg.eight.mplayer.controller.InterfaceController;
import mg.eight.mplayer.manager.AppManager;
import static mg.eight.mplayer.manager.AppManager.writeLog;

/**
 *
 * @author Mauyz
 */

public class Main extends Application {

    @Override
    public void init(){
        String version = System.getProperty("java.version");
        if (Double.valueOf(version.substring(0, version.indexOf(".") + 2)) < 1.8) {
            JOptionPane.showMessageDialog(null, "To use this application, install java version >= 1.8 !", 
                    "Java version error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Java version error, install java version >= 1.8");
            System.exit(-1);
        }
        try{
            AppManager.lock();
        }catch (IOException e){
            writeLog("Lock applicatin failed IOException "+e.getMessage());
        }
        if (AppManager.getFileLock() == null) {
            writeLog("Application already run");
            System.exit(-1);
        }
    }

    @Override
    public void start(Stage mainStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(new URL(Main.class
                .getResource("view/fxml/interface.fxml").toString()));
        AnchorPane root = loader.load();
        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize(),
                size = new Dimension(screen.width - 300, screen.height - 150);
        mainStage.initStyle(StageStyle.UNDECORATED);
        
        mainStage.setScene(new Scene(root, size.width, size.height));
        mainStage.getIcons().add(new Image(Main.class.getResource("view/images/head.png").toString()));
        ((InterfaceController) loader.getController()).setMainStage(mainStage);
        ((InterfaceController) loader.getController()).setHostServices(getHostServices());
        mainStage.show();
    }

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        launch(args);
    }
}
