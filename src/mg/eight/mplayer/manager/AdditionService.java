/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this valueslate file, choose Tools | Templates
 * and open the valueslate in the editor.
 */

package mg.eight.mplayer.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.concurrent.Service;
import mg.eight.mplayer.controller.InterfaceController;
import mg.eight.mplayer.model.Song;

/**
 *
 * @author Mauyz
 */
public class AdditionService extends Service<ArrayList<Song>> {

    private final InterfaceController controller;
    private AdditionTask task;

    public AdditionService(InterfaceController controller) {
        this.controller = controller;
    }

    @Override
    protected AdditionTask createTask() {
        return task;
    }

    public void setFiles(List<File> files) {
        task = new AdditionTask(controller, files);
    }

    public void setFilePl(File filePl) {
        task = new AdditionTask(controller, filePl);
    }
}
