/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import mg.eight.mplayer.model.Setting;
import mg.eight.mplayer.controller.InterfaceController;
import static mg.eight.mplayer.manager.AppManager.writeLog;
/**
 *
 * @author Mauyz
 */
public class SocketManager implements Runnable {

    private final InterfaceController controller;
    private ServerSocket serverSocket, downloadServerSocket;
    private Socket socket, downloadSocket;
    private final Setting setting = Setting.getInstance();

    public SocketManager(InterfaceController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(setting.getPort());
            }
            if (downloadServerSocket == null) {
                downloadServerSocket = new ServerSocket(setting.getPort() + 1);
            }
            writeLog("Socket started on port : " + setting.getPort());
            socket = serverSocket.accept();
            socket.setSoTimeout(6000);
            downloadSocket = downloadServerSocket.accept();
            MessageSender sender = new MessageSender(socket);
            controller.setResponseManager(sender, new DownloadManager(downloadSocket));
            new Thread(sender).start();
            controller.replyRefreshing();
            new Thread(new MessageReceiver(controller,socket)).start();
            serverSocket.close();
            serverSocket = null;
            downloadServerSocket.close();
            downloadServerSocket = null;
        } catch (IOException e) {
            writeLog("IOException " + e.getMessage());
        }
    }

    public void startServer() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                writeLog("IOException " + e.getMessage());
            }
        }
        if (downloadSocket != null) {
            try {
                downloadSocket.close();
            } catch (IOException e) {
                writeLog("IOException " + e.getMessage());
            }
        }
        new Thread(this).start();
    }

    public void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                writeLog("IOException " + e.getMessage());
            }
            serverSocket = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                writeLog("IOException " + ex.getMessage());
            }
            socket = null;
        }
        if (downloadServerSocket != null) {
            try {
                downloadServerSocket.close();
            } catch (IOException e) {
                writeLog("IOException " + e.getMessage());
            }
            downloadServerSocket = null;
        }
        if (downloadSocket != null) {
            try {
                downloadSocket.close();
            } catch (IOException ex) {
                writeLog("IOException " + ex.getMessage());
            }
            downloadSocket = null;
        }
    }
}
