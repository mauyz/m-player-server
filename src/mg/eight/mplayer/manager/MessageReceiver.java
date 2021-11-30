/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import mg.eight.mplayer.model.Command;
import mg.eight.mplayer.model.Message;
import mg.eight.mplayer.controller.InterfaceController;
import static mg.eight.mplayer.manager.AppManager.writeLog;

/**
 *
 * @author Mauyz
 */
public class MessageReceiver implements Runnable {

    private final InterfaceController controller;
    private final Socket socket;

    public MessageReceiver(InterfaceController controller, Socket socket) {
        this.controller = controller;
        this.socket = socket;
    }

    @Override
    public void run() {
        while (socket != null && !socket.isClosed()) {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Object temp = in.readObject();
                if (temp != null && temp.getClass().equals(Message.class)) {
                    Message<?> message = (Message<?>) temp;
                    if (message.getCommand() == Command.SECRET) {
                        if (!message.getData().toString().equals(Command.SECRET.toString())) {
                            break;
                        }
                    } else {
                        controller.handleMessage(message);
                    }
                } else {
                    in.close();
                    break;
                }
            } catch (IOException | ClassNotFoundException ex) {
                writeLog(ex.getClass().getSimpleName() + " " + ex.getMessage());
                break;
            }
        }
        controller.startServer();
    }
}
