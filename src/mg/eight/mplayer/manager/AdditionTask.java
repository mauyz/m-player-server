/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this valueslate file, choose Tools | Templates
 * and open the valueslate in the editor.
 */
package mg.eight.mplayer.manager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import mg.eight.mplayer.controller.InterfaceController;
import static mg.eight.mplayer.manager.AppManager.writeLog;
import mg.eight.mplayer.model.Song;

/**
 *
 * @author Mauyz
 */
public class AdditionTask extends Task<ArrayList<Song>> {

    private final InterfaceController controller;
    private List<File> files;
    private File filePl;
    private final ArrayList<Song> values = new ArrayList<>();

    public AdditionTask(InterfaceController controller, List<File> files) {
        this.controller = controller;
        this.files = files;
    }

    public AdditionTask(InterfaceController controller, File filePl) {
        this.controller = controller;
        this.filePl = filePl;
    }

    @Override
    protected ArrayList<Song> call() throws Exception {
        if (filePl != null) {
            try (LineNumberReader in = new LineNumberReader(new FileReader(filePl))) {
                while (in.ready()) {
                    String f = URLDecoder.decode(in.readLine(), "UTF-8");
                    if (controller.isSupportedFile(f)) {
                        Song s = controller.addNewSong(f, values);
                        values.add(s);
                        Platform.runLater(() -> {
                            controller.getOriginalList().add(s);
                        });
                    }
                }
                in.close();
            } catch (IOException ex) {
                writeLog("IOException " + ex.getMessage());
            }
        } else if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    addDir(f);
                } else if (controller.isSupportedFile(f.getAbsolutePath())) {
                    Song s = controller.addNewSong(f.getAbsolutePath(), values);
                    values.add(s);
                    Platform.runLater(() -> {
                        controller.getOriginalList().add(s);
                    });
                }
            }
            if (!values.isEmpty()) {
                controller.addPlaylist(values);
            }
        }
        return values;
    }

    private void addDir(File f){
        for (File file : f.listFiles()) {
            if (file.isDirectory()) {
                addDir(file);
            } else if (controller.isSupportedFile(file.getAbsolutePath())) {
                Song s = controller.addNewSong(file.getAbsolutePath(), values);
                values.add(s);
                Platform.runLater(() -> {
                    controller.getOriginalList().add(s);
                });
            }
        }
    }

    private int getLinesCount(File file) {
        int nb = 0;
        Path p = Paths.get(file.getAbsolutePath());
        try (Stream<String> stream = Files.lines(p)) {
            nb = (int) stream.count();
            stream.close();
        } catch (IOException ex) {
            writeLog("Ioexception " + ex.getMessage());
        }
        return nb;
    }

    private int getFilesCount(List<File> files) {
        int i = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                i = +getFilesCount(Arrays.asList(file.listFiles()));
            } else if (controller.isSupportedFile(file.getAbsolutePath())) {
                i++;
            }
        }
        return i;
    }
}
