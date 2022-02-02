/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mg.eight.mplayer.model.Setting;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Mauyz
 */
public class AppManager {

    private static final File confDir = new File(System.getProperty("user.home") 
            + File.separator + ".mplayer"),
            config = new File(confDir.getAbsolutePath() + File.separator 
                   + "mplayer.conf"),
            playlistDir = new File(confDir.getAbsolutePath() + File.separator 
                    + "playlist"),
            playlistDefault = new File(playlistDir.getAbsolutePath() 
                    + File.separator + "Default"),
            lockFile = new File(confDir.getAbsolutePath() + File.separator 
                    + ".lock"),
            logFile = new File(confDir.getAbsolutePath() + File.separator 
                    + "mplayer.log");

    private static RandomAccessFile randomAccessFile;
    private static FileLock fileLock;

    private static final DocumentBuilderFactory 
            builderFactory = DocumentBuilderFactory.newInstance();

    public static void loadConfig() throws ParserConfigurationException,
            SAXException, IOException {

        if (!confDir.exists()) {
            confDir.mkdir();
        }

        Files.setAttribute(Paths.get(confDir.getAbsolutePath()), "dos:hidden",
                Boolean.TRUE);
        if (!playlistDir.exists()) {
            playlistDir.mkdir();
        }
        if (!playlistDefault.exists()) {
            playlistDefault.createNewFile();
        }
        if (config.exists()) {
            Element root = builderFactory.newDocumentBuilder()
                    .parse(config).getDocumentElement();
            Setting setting = Setting.getInstance();
            NodeList node = root.getElementsByTagName("playlist");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setPlaylist(node.item(0).getFirstChild()
                            .getNodeValue());
                } catch (IllegalArgumentException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
            node = root.getElementsByTagName("repeat");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setRepeat(root.getElementsByTagName("repeat")
                            .item(0).getFirstChild().getNodeValue());
                } catch (IllegalArgumentException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
            node = root.getElementsByTagName("random");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setRandom(root.getElementsByTagName("random")
                            .item(0).getFirstChild().getNodeValue());
                } catch (IllegalArgumentException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
            node = root.getElementsByTagName("remote");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setRemote(root.getElementsByTagName("remote")
                            .item(0).getFirstChild().getNodeValue());
                } catch (IllegalArgumentException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
            node = root.getElementsByTagName("volume");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setVolume(Double.parseDouble(root
                            .getElementsByTagName("volume")
                            .item(0).getFirstChild().getNodeValue()));
                } catch (NumberFormatException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
            node = root.getElementsByTagName("port");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setPort(Integer.parseInt(root
                            .getElementsByTagName("port")
                            .item(0).getFirstChild().getNodeValue()));
                } catch (NumberFormatException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
            node = root.getElementsByTagName("mute");
            if (node != null && node.getLength() > 0 && node.item(0)
                    .hasChildNodes()) {
                try {
                    setting.setMute(Boolean.valueOf(root
                            .getElementsByTagName("mute")
                            .item(0).getFirstChild().getNodeValue()));
                } catch (DOMException e) {
                    writeLog("IllegalArgumentException +" + e.getMessage());
                }
            }
        } else {
            try {
                saveConfig();
            } catch (IllegalArgumentException | IllegalAccessException 
                    | TransformerException ex) {
                writeLog(ex.getClass().getSimpleName() + " " + ex.getMessage());
            }
        }
    }

    public static void saveConfig() throws ParserConfigurationException, 
            IllegalArgumentException, IllegalAccessException, 
           TransformerConfigurationException, TransformerException, IOException{
        Document document = builderFactory
                .newDocumentBuilder()
                .newDocument();
        Element root = document.createElement("Setting");
        Setting setting = Setting.getInstance();
        Class<? extends Setting> cl = setting.getClass();
        for (Field field : cl.getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getType().equals(Setting.class)) {
                Element e = document.createElement(field.getName());
                e.appendChild(document.createTextNode(String.valueOf(field
                        .get(setting))));
                root.appendChild(e);
            }
        }
        document.appendChild(root);
        Transformer t = TransformerFactory.newInstance()
                .newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult sr = new StreamResult(new FileWriter(config));
        t.transform(new DOMSource(document), sr);
        sr.getWriter().close();
    }

    public static void writeLog(String log) {
        if (!confDir.exists()) {
            confDir.mkdir();
        }
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.append(new Date().toString() + "  " + log + "\n");
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public static File getPlaylistDir() {
        return playlistDir;
    }

    public static FileLock getFileLock() {
        return fileLock;
    }

    public static String[] getPlaylists() {
        return playlistDir.list();
    }

    public static void lock() throws FileNotFoundException, IOException {
        if (!confDir.exists()) {
            confDir.mkdir();
        }
        randomAccessFile = new RandomAccessFile(lockFile, "rw");
        fileLock = randomAccessFile.getChannel().tryLock();
    }

    public static void unlock() throws IOException {
        if (fileLock != null) {
            fileLock.release();
        }
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}
