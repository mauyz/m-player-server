/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.manager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import mg.eight.mplayer.model.Command;
import mg.eight.mplayer.model.Message;
import mg.eight.mplayer.model.Song;
import static mg.eight.mplayer.manager.AppManager.writeLog;

/**
 *
 * @author Mauyz
 */
public class DownloadManager implements Runnable {

    private final Socket socket;
    private final ArrayList<Song> queueList = new ArrayList<>();

    public DownloadManager(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket != null && !socket.isClosed()) {
            int i = 0;
            while (i < queueList.size()) {
                Song song = queueList.get(i);
                File file = new File(song.getPath());
                if (!file.exists()) {
                    try {
                        sendMessage(new Message<>(Command.ERROR, "Download error"));
                    } catch (IOException e) {
                        writeLog("IOException " + e.getMessage());
                        break;
                    }
                    queueList.remove(i);
                    continue;
                }
                try (BufferedInputStream bis = new BufferedInputStream(
                        new FileInputStream(file));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
                    Message<?> msg;
                    try {
                        sendMessage(new Message<>(Command.NAME, song.getName()));
                        msg = readMessage();
                        if (msg == null) {
                            break;
                        }
                        if (msg.getCommand() != Command.CONTINUE) {
                            queueList.remove(i);
                            continue;
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        writeLog("IOException " + e.getMessage());
                        break;
                    }
                    int b;
                    byte[] bytes = new byte[1024 * 1024];
                    boolean canceled = false;
                    while ((b = bis.read(bytes)) != -1) {
                        bos.write(bytes, 0, b);
                        byte[] bs = bos.toByteArray();
                        bos.reset();
                        try {
                            sendMessage(new Message<>(Command.BYTE, bs));
                            msg = readMessage();
                            if (msg == null) {
                                queueList.clear();
                                return;
                            }
                            if (msg.getCommand() != Command.CONTINUE) {
                                canceled = true;
                                break;
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            writeLog("IOException " + e.getMessage());
                            queueList.clear();
                            return;
                        }
                    }
                    if (canceled) {
                        queueList.remove(i);
                        continue;
                    }
                    try {
                        if (msg == null) {
                            break;
                        }
                        if (msg.getCommand() == Command.CONTINUE) {
                            sendMessage(new Message<>(Command.END, null));
                        }
                        queueList.remove(i);
                    } catch (IOException e) {
                        writeLog("IOException " + e.getMessage());
                        break;
                    }
                } catch (IOException e) {
                    try {
                        sendMessage(new Message<>(Command.ERROR, "Download error"));
                    } catch (IOException ex) {
                        writeLog("IOException " + ex.getMessage());
                        break;
                    }
                    writeLog("IOException " + e.getMessage());
                    queueList.remove(i);
                }
            }
            queueList.clear();
        }
    }

    public ArrayList<Song> getQueueList() {
        return queueList;
    }

    private void sendMessage(Object object) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(object);
        out.flush();
    }

    private Message<?> readMessage() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        Object temp = in.readObject();
        if (temp != null && temp.getClass().equals(Message.class)) {
            //System.err.println(temp.toString());
            return (Message<?>) temp;
        }
        return null;
    }
}
