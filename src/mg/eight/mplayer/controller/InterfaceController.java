/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mg.eight.mplayer.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import mg.eight.mplayer.model.Command;
import mg.eight.mplayer.model.Message;
import mg.eight.mplayer.model.Setting;
import mg.eight.mplayer.model.Song;
import mg.eight.mplayer.Main;
import mg.eight.mplayer.manager.AdditionService;
import mg.eight.mplayer.manager.AppManager;
import static mg.eight.mplayer.manager.AppManager.writeLog;
import mg.eight.mplayer.manager.DownloadManager;
import mg.eight.mplayer.manager.MessageSender;
import mg.eight.mplayer.manager.SocketManager;
import org.xml.sax.SAXException;

/**
 *
 * @author Mauyz
 */
public class InterfaceController implements Initializable {
    
    private double x, y;
    private boolean maximized = false;
    private Rectangle2D backupWindowBounds = null;
    
    private final Setting setting = Setting.getInstance();
    private final SocketManager socketManager = new SocketManager(this);
    private MessageSender messageSender;
    private DownloadManager downloadManager;
    private int port;
    
    private MediaPlayer player;
    private Duration duration;
    private final ChangeListener<Duration> changeListener = (ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
        updateValues();
    };
    
    private final AdditionService additionService = new AdditionService(this);
    private boolean isRunning = false;
    private final DataFormat songFormat = new DataFormat("java.util.ArrayList");
    private Song songPlayed;
    private int indice = -1, indiceRandom = -1;
    private final ArrayList<Integer> randomList = new ArrayList<>();
    private final ObservableList<Song> originalList = FXCollections.observableArrayList();
    private FilteredList<Song> filteredList;
    private final ObservableList<String> listPlaylist = FXCollections.observableArrayList();
    private final Image head = new Image(Main.class
            .getResource("view/images/head.png").toString());
    
    private Stage mainStage;
    private Stage aboutStage;
    private HostServices hostServices;
    private Stage manualStage, errorsStage;
    private ErrorsController errorsController;
    private Alert deleteDialog;
    private TextInputDialog editDialog;
    
    private final Pane pane = new Pane();
    private MediaView mediaView;
    
    @FXML
    private Label songLbl;
    @FXML
    private ListView<Song> playList;
    @FXML
    private Button playBtn;
    @FXML
    private Slider timeSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ContextMenu menu;
    @FXML
    private Label currentTimeLbl;
    @FXML
    private Label mediaTimeLbl;
    @FXML
    private Label deviceLbl;
    @FXML
    private ChoiceBox<String> playListChoice;
    @FXML
    private TextField searchTxt;
    @FXML
    private MenuItem renamePlBtn;
    @FXML
    private MenuItem deletePlBtn;
    @FXML
    private BorderPane videoPane;
    @FXML
    private VBox controlPane;
    @FXML
    private ImageView audioImageView;
    @FXML
    private AnchorPane mediaPane;
    @FXML
    private VBox playListPane;
    @FXML
    private ToggleButton playlistToggle;
    @FXML
    private ToggleButton repeatToggle;
    @FXML
    private ToggleButton shuffleToggle;
    @FXML
    private ToggleButton remoteToggle;
    @FXML
    private HBox mediaBtnPane;
    @FXML
    private Button clearBtn;
    @FXML
    private MenuItem openFileItem;
    @FXML
    private MenuItem openFolderItem;
    @FXML
    private Label songTitleLbl;
    @FXML
    private StackPane stackPane;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private Label statusLbl;
    @FXML
    private Button previousBtn;
    @FXML
    private Button nextBtn;
    @FXML
    private Region windowResizeBtn;
    @FXML
    private ToggleButton volumeBtn;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSettings();
        loadList();
        initView();
        File f = new File(AppManager.getPlaylistDir().getAbsolutePath()
                + File.separator + setting.getPlaylist());
        additionService.setFilePl(f);
        additionService.start();
        additionService.setOnSucceeded((WorkerStateEvent event) -> {
            if (!additionService.getValue().isEmpty()) {
                sendPlaylist();
            }
        });
        if (setting.isRemote()) {
            startServer();
        } else {
            stopServer();
        }
    }

    // ====#===== View function ====#=====
    private void initView() {
        // init playlist
        filteredList = originalList.filtered((Song t) -> t.getName().substring(0, t.getName().lastIndexOf("."))
                .toLowerCase().contains(searchTxt.getText().toLowerCase()));
        
        searchTxt.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                clearBtn.setVisible(!newValue.isEmpty());
                filteredList.setPredicate((Song t) -> t.getName().substring(0, t.getName()
                        .lastIndexOf(".")).toLowerCase()
                        .contains(newValue.toLowerCase()));
                indice = filteredList.indexOf(songPlayed);
                if (setting.isRandom()) {
                    indiceRandom = -1;
                    randomList.clear();
                }
            }
        });
        playList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        playList.setItems(filteredList);
        VBox placeHolder = new VBox();
        placeHolder.setMaxSize(215, 130);
        placeHolder.setMinSize(215, 130);
        placeHolder.setOnMouseReleased((MouseEvent event) -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                addFiles(null);
            }
        });
        placeHolder.getChildren().addAll(new ImageView(new Image(Main.class
                .getResourceAsStream("view/images/ic_playlist_add_black_48.png"))),
                new Label("Playlist is empty"),
                new Label("Drope a file(s) here or select from menu"));
        placeHolder.setAlignment(Pos.CENTER);
        playList.setPlaceholder(placeHolder);
        playList.setCellFactory(listView -> {
            ListCell<Song> cell = new ListCell<Song>() {
                @Override
                public void updateItem(Song item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty && item != null) {
                        ImageView imageView = new ImageView(new Image(Main.class
                                .getResourceAsStream("view/images/head.png")));
                        imageView.setFitWidth(30);
                        imageView.setFitHeight(30);
                        Label label = new Label(item.getName());
                        HBox hBox = new HBox(5, imageView, label);
                        HBox.setMargin(imageView, new Insets(3));
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        if (item.equals(songPlayed)) {
                            label.setFont(Font.font(Font.getDefault().getFamily(),
                                    FontWeight.BOLD, Font.getDefault().getSize()));
                            if (!isSelected()) {
                                hBox.setStyle("-fx-background-color: #cccccc;");
                            }
                        } else {
                            label.setFont(Font.font(Font.getDefault().getFamily(),
                                    FontWeight.NORMAL, Font.getDefault().getSize()));
                        }
                        setPadding(Insets.EMPTY);
                        setGraphic(hBox);
                    } else {
                        Pane p = new Pane();
                        p.setStyle("-fx-background-color: #ffffff;");
                        setPadding(Insets.EMPTY);
                        setGraphic(p);
                    }
                }
                
                @Override
                public void updateSelected(boolean selected) {
                    super.updateSelected(selected);
                    if (selected) {
                        getGraphic().setStyle("-fx-background-color: transparent;");
                    } else if (getItem() != null && getItem().equals(songPlayed)) {
                        getGraphic().setStyle("-fx-background-color: #cccccc;");
                    }
                }
            };
            
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty() && !playList.getSelectionModel()
                        .getSelectedItems().isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(songFormat, new ArrayList<>(
                            playList.getSelectionModel().getSelectedItems()));
                    db.setContent(cc);
                }
            });
            cell.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(songFormat)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            });
            cell.setOnDragDone(event -> event.consume());
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(songFormat)) {
                    ArrayList<Song> names = new ArrayList<>(
                            playList.getSelectionModel().getSelectedItems());
                    ArrayList<Song> temp = new ArrayList<>(originalList);
                    temp.removeAll(names);
                    int index = temp.indexOf(cell.getItem());
                    temp.addAll(index, names);
                    originalList.setAll(temp);
                    indice = filteredList.indexOf(songPlayed);
                    savePlaylist();
                    sendPlaylist();
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
            });
            return cell;
        });
        
        menu.setOnShowing((WindowEvent event) -> {
            ArrayList<Song> selected = new ArrayList<>(playList.getSelectionModel()
                    .getSelectedItems());
            MenuItem playNext = menu.getItems().get(2), remove = menu.getItems().get(3),
                    clear = menu.getItems().get(4);
            remove.setDisable(selected.isEmpty());
            remove.setVisible(!selected.isEmpty());
            clear.setDisable(originalList.isEmpty());
            clear.setVisible(!originalList.isEmpty());
            selected.remove(songPlayed);
            playNext.setDisable(selected.isEmpty()
                    || songPlayed == null
                    || player == null);
            playNext.setVisible(!playNext.isDisable());
        });
        // init media controller
        playListChoice.setItems(listPlaylist);
        renamePlBtn.setDisable(setting.getPlaylist().equals("Default"));
        deletePlBtn.setDisable(setting.getPlaylist().equals("Default"));
        playListChoice.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                applySettings();
                renamePlBtn.setDisable(setting.getPlaylist().equals("Default"));
                deletePlBtn.setDisable(setting.getPlaylist().equals("Default"));
            }
        });
        volumeBtn.setSelected(setting.isMute());
        volumeBtn.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            volumeSlider.getTooltip().setText("Volume " + (newValue
                    ? "mute"
                    : (int) setting.getVolume()) + "");
            if (player != null) {
                player.setMute(newValue);
            }
            applySettings();
        });
        volumeSlider.setValue(setting.getVolume());
        volumeSlider.getTooltip().setText("Volume " + (volumeBtn.isSelected()
                ? "mute"
                : (int) setting.getVolume()) + "");
        volumeSlider.valueProperty().addListener((Observable ov) -> {
            setVolume(volumeSlider.getValue() / 100);
            playList.requestFocus();
            mediaView.requestFocus();
            audioImageView.requestFocus();
        });
        final EventHandler<MouseEvent> longPressHandler = new EventHandler<MouseEvent>() {
            boolean called = false;
            Timer timer;
            
            @Override
            public void handle(final MouseEvent event) {
                if (timer != null) {
                    timer.cancel();
                }
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED
                        && player != null) {
                    called = false;
                    timer = null;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            called = true;
                            Platform.runLater(() -> {
                                if (event.getSource().equals(previousBtn)) {
                                    if (timeSlider.getValue() > 0) {
                                        timeSlider.decrement();
                                        changeSeek(null);
                                    }
                                } else {
                                    if (timeSlider.getValue() < 100) {
                                        timeSlider.increment();
                                        changeSeek(null);
                                    } else {
                                        timeSlider.setValue(0);
                                        next();
                                    }
                                }
                            });
                        }
                    }, 500, 100);
                } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    if (timer != null && called) {
                        timer.cancel();
                    } else {
                        if (timer != null) {
                            timer.cancel();
                        }
                        if (event.getSource().equals(previousBtn)) {
                            previous();
                        } else {
                            next();
                        }
                    }
                }
            }
        };
        previousBtn.addEventHandler(MouseEvent.ANY, longPressHandler);
        nextBtn.addEventHandler(MouseEvent.ANY, longPressHandler);
        timeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (timeSlider.isValueChanging()) {
                if (duration != null) {
                    setTime(timeSlider.getValue() / 100.0);
                }
                playList.requestFocus();
                mediaView.requestFocus();
                audioImageView.requestFocus();
            }
        });
        videoPane.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (oldValue != newValue) {
                if (videoPane.getCenter() == pane) {
                    mediaView.setFitWidth(videoPane.getWidth());
                    mediaView.setFitHeight(videoPane.getHeight());
                    mediaView.setTranslateX((videoPane.getWidth() - mediaView
                            .prefWidth(-1)) / 2);
                    mediaView.setTranslateY((videoPane.getHeight() - mediaView
                            .prefHeight(-1)) / 2);
                } else if (audioImageView.getImage() != head) {
                    audioImageView.setFitWidth(videoPane.getWidth());
                    audioImageView.setFitHeight(videoPane.getHeight());
                }
            }
        });
        audioImageView.setImage(head);
        audioImageView.setFitHeight(144);
        audioImageView.setFitWidth(144);
        videoPane.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (oldValue != newValue) {
                if (videoPane.getCenter() == pane) {
                    mediaView.setFitWidth(videoPane.getWidth());
                    mediaView.setFitHeight(videoPane.getHeight());
                    mediaView.setTranslateX((videoPane.getWidth() - mediaView
                            .prefWidth(-1)) / 2);
                    mediaView.setTranslateY((videoPane.getHeight() - mediaView
                            .prefHeight(-1)) / 2);
                } else if (audioImageView.getImage() != head) {
                    audioImageView.setFitWidth(videoPane.getWidth());
                    audioImageView.setFitHeight(videoPane.getHeight());
                }
            }
        });
        videoPane.setOnDragOver((DragEvent event) -> {
            onDragOverFiles(event);
        });
        videoPane.setOnDragDropped((DragEvent event) -> {
            onDraggedFiles(event);
        });
        mediaView = new MediaView();
        mediaView.setFocusTraversable(true);
        mediaView.setOnKeyPressed((KeyEvent event) -> {
            keyPressed(event);
        });
        mediaView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() >= 2) {
                if (!mainStage.isFullScreen()) {
                    setUpFullScreen();
                } else {
                    mainStage.setFullScreen(false);
                }
            }
        });
        mediaView.setOnDragOver((DragEvent event) -> {
            onDragOverFiles(event);
        });
        mediaView.setOnDragDropped((DragEvent event) -> {
            onDraggedFiles(event);
        });
        mediaView.setOnMouseMoved((MouseEvent event) -> {
            if (mediaView.getCursor() == Cursor.NONE) {
                controlPane.setVisible(true);
                mediaView.setCursor(Cursor.DEFAULT);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mainStage.isFullScreen()) {
                            controlPane.setVisible(false);
                            mediaView.setCursor(Cursor.NONE);
                        } else {
                            mediaView.setCursor(Cursor.NONE);
                        }
                    }
                }, 5000);
            }
        });
        mediaView.setOnMouseEntered((MouseEvent event) -> {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mainStage.isFullScreen()) {
                        controlPane.setVisible(false);
                        mediaView.setCursor(Cursor.NONE);
                    } else {
                        mediaView.setCursor(Cursor.NONE);
                    }
                }
            }, 5000);
        });
        remoteToggle.setSelected(setting.isRemote());
        remoteToggle.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                initEditDialog();
                editDialog.setResult(null);
                editDialog.setTitle("Start server");
                editDialog.setContentText("Port (>1024) :");
                final ButtonType okBtn = editDialog.getDialogPane()
                        .getButtonTypes().get(0);
                final TextField textField = editDialog.getEditor();
                textField.setOnKeyReleased((KeyEvent event) -> {
                    if (event.getCode() == KeyCode.ENTER
                            && !textField.getText().trim().isEmpty()
                            && isValablePort(textField.getText())) {
                        editDialog.setResult("OK");
                        port = Integer.parseInt(textField.getText());
                        applySettings();
                    } else {
                        editDialog.getDialogPane().lookupButton(okBtn)
                                .setDisable(textField.getText().trim().isEmpty()
                                        || !isValablePort(textField.getText()));
                    }
                });
                editDialog.setOnShown((DialogEvent event) -> {
                    textField.setText(String.valueOf(port));
                    editDialog.getDialogPane().lookupButton(okBtn)
                            .setDisable(textField.getText().trim().isEmpty()
                                    || !isValablePort(textField.getText()));
                    textField.requestFocus();
                    textField.selectAll();
                });
                ((Button) editDialog.getDialogPane().lookupButton(okBtn))
                        .setOnAction((ActionEvent event) -> {
                            editDialog.setResult("OK");
                            editDialog.close();
                            port = Integer.parseInt(textField.getText());
                            applySettings();
                        });
                editDialog.showAndWait();
                if (editDialog.getResult() == null) {
                    remoteToggle.setSelected(false);
                    remoteToggle.setTooltip(new Tooltip("Start server"));
                }
            } else {
                applySettings();
            }
        });
        playlistToggle.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            playListPane.setVisible(newValue);
            playList.getSelectionModel().clearSelection();
        });
        shuffleToggle.setSelected(setting.isRandom());
        shuffleToggle.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            applySettings();
        });
        repeatToggle.setTooltip(new Tooltip(setting.getRepeat()));
        setupRepeatToggle(setting.getRepeat());
        repeatToggle.setOnAction((ActionEvent event) -> {
            repeatToggle.setTooltip(new Tooltip(
                    setting.getRepeat().equals("No repeat") ? "Repeat one"
                    : setting.getRepeat().equals("Repeat one") ? "Repeat all"
                    : "No repeat"));
        });
        repeatToggle.tooltipProperty().addListener((ObservableValue<? extends Tooltip> observable, Tooltip oldValue, Tooltip newValue) -> {
            if (newValue != null) {
                setupRepeatToggle(newValue.getText());
                applySettings();
            }
        });
        stackPane.visibleProperty().bind(additionService.runningProperty());
        progress.progressProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            Text text = (Text) progress.lookup(".percentage");
            if (text != null && text.getText().equals("Terminé")) {
                text.setText(null);
            }
        });
        windowResizeBtn.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            private double dragOffsetX, dragOffsetY;
            
            @Override
            public void handle(MouseEvent event) {
                if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                    mainStage.getScene().setCursor(Cursor.SE_RESIZE);
                }
                if (event.getEventType() == MouseEvent.MOUSE_EXITED
                        || event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                    mainStage.getScene().setCursor(Cursor.DEFAULT);
                }
                if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    dragOffsetX = (mainStage.getX() + mainStage.getWidth()) - event.getScreenX();
                    dragOffsetY = (mainStage.getY() + mainStage.getHeight()) - event.getScreenY();
                    event.consume();
                }
                if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    mainStage.getScene().setCursor(Cursor.SE_RESIZE);
                    ObservableList<Screen> screens = Screen.getScreensForRectangle(mainStage.getX(), mainStage.getY(), 1, 1);
                    final Screen screen;
                    if (screens.size() > 0) {
                        screen = Screen.getScreensForRectangle(mainStage.getX(), mainStage.getY(), 1, 1).get(0);
                    } else {
                        screen = Screen.getScreensForRectangle(0, 0, 1, 1).get(0);
                    }
                    Rectangle2D visualBounds = screen.getVisualBounds();
                    double maxX = Math.min(visualBounds.getMaxX(), event.getScreenX() + dragOffsetX);
                    double maxY = Math.min(visualBounds.getMaxY(), event.getScreenY() - dragOffsetY);
                    mainStage.setWidth(Math.max(700, maxX - mainStage.getX()));
                    mainStage.setHeight(Math.max(200, maxY - mainStage.getY()));
                    event.consume();
                }
            }
        });
    }
    
    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
        this.mainStage.fullScreenProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (t) {
                exitFullScreen();
            }
        });
        this.mainStage.setOnHiding((WindowEvent event) -> {
            try {
                AppManager.unlock();
            } catch (IOException e) {
                writeLog("IOException " + e.getMessage());
            }
            setting.setRemote(false);
            socketManager.stopServer();
            stopPlaying();
        });
    }
    
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
    
    public void setResponseManager(MessageSender sender,
            DownloadManager dm) {
        this.messageSender = sender;
        this.downloadManager = dm;
    }
    
    private void maximizeWindow() {
        final Screen screen = Screen.getScreensForRectangle(mainStage.getX(),
                mainStage.getY(), 1, 1).get(0);
        if (maximized) {
            maximized = false;
            if (backupWindowBounds != null) {
                mainStage.setX(backupWindowBounds.getMinX());
                mainStage.setY(backupWindowBounds.getMinY());
                mainStage.setWidth(backupWindowBounds.getWidth());
                mainStage.setHeight(backupWindowBounds.getHeight());
            }
        } else {
            maximized = true;
            backupWindowBounds = new Rectangle2D(mainStage.getX(), mainStage.getY(),
                    mainStage.getWidth(), mainStage.getHeight());
            mainStage.setX(screen.getVisualBounds().getMinX());
            mainStage.setY(screen.getVisualBounds().getMinY());
            mainStage.setWidth(screen.getVisualBounds().getWidth());
            mainStage.setHeight(screen.getVisualBounds().getHeight());
        }
    }
    
    private void showError(String message) {
        writeLog(message);
        try {
            if (errorsStage == null) {
                FXMLLoader loader = new FXMLLoader(new URL(Main.class
                        .getResource("view/fxml/errors.fxml").toString()));
                errorsStage = new Stage();
                errorsStage.getIcons().add(new Image(Main.class
                        .getResource("view/images/head.png").toString()));
                errorsStage.setTitle("Errors");
                errorsStage.setScene(new Scene((Parent) loader.load()));
                errorsController = (ErrorsController) loader.getController();
                errorsController.setStage(errorsStage);
            }
            errorsController.appendError(message);
            
            if (errorsStage.isShowing()) {
                errorsStage.setIconified(false);
                errorsStage.toFront();
            } else {
                errorsStage.show();
            }
            
        } catch (IOException ex) {
            writeLog("IOException " + ex.getMessage());
        }
    }

    // ====#===== Media function ====#=====
    private void previous() {
        Song temp = songPlayed;
        if (temp != null) {
            songPlayed = null;
            if (originalList.contains(temp)) {
                originalList.set(originalList.indexOf(temp), temp);
            }
        }
        if (!filteredList.isEmpty()) {
            if (setting.isRandom()) {
                if (indiceRandom > 0) {
                    indiceRandom--;
                } else {
                    indiceRandom = (int) (Math.random() * (filteredList.size() - 1));
                    randomList.add(0, indiceRandom);
                    indiceRandom = 0;
                }
                indice = randomList.get(indiceRandom);
            } else {
                if (indice > 0) {
                    indice--;
                } else {
                    indice = filteredList.size() - 1;
                }
            }
            play(filteredList.get(indice).getId());
        } else {
            if (songPlayed != null) {
                play(songPlayed.getId());
            }
        }
    }
    
    private void next() {
        Song temp = songPlayed;
        if (temp != null) {
            songPlayed = null;
            if (originalList.contains(temp)) {
                originalList.set(originalList.indexOf(temp), temp);
            }
        }
        if (!filteredList.isEmpty()) {
            if (setting.isRandom()) {
                if (indiceRandom < randomList.size() - 1) {
                    indiceRandom++;
                } else {
                    indiceRandom = (int) (Math.random() * (filteredList.size() - 1));
                    randomList.add(indiceRandom);
                    indiceRandom = randomList.size() - 1;
                }
                indice = randomList.get(indiceRandom);
            } else {
                if (indice < filteredList.size() - 1) {
                    indice++;
                } else {
                    indice = 0;
                }
            }
            play(filteredList.get(indice).getId());
        } else {
            if (songPlayed != null) {
                play(songPlayed.getId());
            }
        }
    }
    
    private void play(String songId) {
        if (player != null) {
            player.currentTimeProperty().removeListener(changeListener);
            player.stop();
            player.dispose();
        }
        songPlayed = null;
        for (Song s : originalList) {
            if (s.getId().equals(songId)) {
                songPlayed = s;
                break;
            }
        }
        try {
            String source;
            if (songPlayed.getPath().contains("http")) {
                source = songPlayed.getPath();
            } else {
                File file = new File(songPlayed.getPath());
                if (!file.exists()) {
                    showError("Error occured when playing [" + songPlayed.getPath() + "]");
                    writeLog("MediaException file not found");
                    next();
                    return;
                }
                source = file.toURI().toString();
            }
            player = new MediaPlayer(new Media(source));
            player.play();
            player.setVolume(setting.getVolume() / 100);
            player.setMute(setting.isMute());
            player.setOnReady(() -> {
                if (originalList.contains(songPlayed)) {
                    originalList.set(originalList.indexOf(songPlayed), songPlayed);
                }
                duration = player.getMedia().getDuration();
                setMediaView();
                mediaTimeLbl.setText(formatTime(duration));
            });
            player.setOnError(() -> {
                showError("Error occured when playing [" + songPlayed.getPath() + "]");
                next();
            });
            player.currentTimeProperty().addListener(changeListener);
            player.setOnEndOfMedia(() -> {
                if (!filteredList.isEmpty()) {
                    if (setting.getRepeat().equals("Repeat one")) {
                        play(songPlayed.getId());
                    } else {
                        if (setting.isRandom()
                                || indice != filteredList.size() - 1
                                || setting.getRepeat().equals("Repeat all")) {
                            next();
                        } else {
                            stop();
                        }
                    }
                }
            });
            sendMessage(new Message<>(Command.PLAY, songPlayed));
            playList.scrollTo(songPlayed);
            songLbl.setText(songPlayed.getName());
            setupPlayBtn(true);
        } catch (MediaException e) {
            showError("Error occured when playing [" + songPlayed.getPath() + "]");
            writeLog("MediaException " + e.getMessage());
            next();
        }
    }
    
    private void playPause() {
        Status status;
        if (player != null && (status = player.getStatus()) != null
                && !status.equals(Status.UNKNOWN)) {
            if (status == Status.PLAYING) {
                player.pause();
                setupPlayBtn(false);
                sendMessage(new Message<>(Command.PAUSE, songPlayed));
            } else if (status == Status.PAUSED) {
                player.play();
                setupPlayBtn(true);
                sendMessage(new Message<>(Command.PLAY, songPlayed));
            } else {
                play(songPlayed.getId());
            }
        } else if ((indice = playList.getSelectionModel().getSelectedIndex()) != -1) {
            play(filteredList.get(indice).getId());
        } else if (!filteredList.isEmpty()) {
            if (setting.isRandom()) {
                indiceRandom = (int) (Math.random() * (filteredList.size() - 1));
                randomList.add(indiceRandom);
                indiceRandom = randomList.size() - 1;
                indice = randomList.get(indiceRandom);
            } else {
                indice = 0;
            }
            play(filteredList.get(indice).getId());
        } else {
            FileChooser chooser = new FileChooser();
            String[] ext = {"*.mp3", "*.aiff", "*.wav", "*.mp4", "*.flv"};
            chooser.getExtensionFilters().addAll(new ExtensionFilter("Audio files", ext));
            chooser.setTitle("Add files");
            List<File> files = chooser.showOpenMultipleDialog(mainStage);
            if (files != null && !files.isEmpty()) {
                additionService.setFiles(files);
                additionService.setOnSucceeded((WorkerStateEvent event) -> {
                    audioImageView.setOpacity(1);
                    if (!additionService.getValue().isEmpty()) {
                        sendPlaylist();
                    }
                    playPause();
                });
                audioImageView.setOpacity(0.1);
                additionService.restart();
            }
        }
    }
    
    private void stop() {
        if (player != null) {
            Song temp = songPlayed;
            songPlayed = null;
            if (originalList.contains(temp)) {
                originalList.set(originalList.indexOf(temp), temp);
            }
            player.stop();
            player.dispose();
            player = null;
        }
        audioImageView.setImage(head);
        audioImageView.setFitHeight(144);
        audioImageView.setFitWidth(144);
        videoPane.setCenter(audioImageView);
        setupPlayBtn(false);
        sendMessage(new Message<>(Command.STOP, null));
        songLbl.setText("");
        currentTimeLbl.setText("--:--");
        mediaTimeLbl.setText("--:--");
        timeSlider.setValue(0);
        if (mainStage != null && mainStage.isFullScreen()) {
            mainStage.setFullScreen(false);
        }
    }
    
    public void stopPlaying() {
        if (player != null) {
            player.stop();
            player.dispose();
        }
    }
    
    private void setupPlayBtn(boolean flag) {
        String image = Main.class.getResource(flag ? "view/images/ic_pause_white_36dp.png"
                : "view/images/ic_play_arrow_white_36dp.png").toExternalForm();
        playBtn.setStyle("-fx-background-image: "
                + "url(\"" + image + "\");");
    }
    
    private void setupRepeatToggle(String value) {
        String image = Main.class.getResource(
                value.equals("Repeat one")
                ? "view/images/ic_repeat_one_white_36dp.png"
                : "view/images/ic_repeat_white_36dp.png").toExternalForm();
        repeatToggle.setStyle("-fx-background-image: " + "url(\"" + image + "\");");
        repeatToggle.setSelected(!value.equals("No repeat"));
    }
    
    private void setMediaView() {
        if (player != null) {
            if (isAudioFile(songPlayed.getPath())) {
                audioImageView.setImage(head);
                audioImageView.setFitHeight(144);
                audioImageView.setFitWidth(144);
                if (mainStage.isFullScreen()) {
                    mainStage.setFullScreen(false);
                }
                ObservableMap<String, Object> metadata = player.getMedia()
                        .getMetadata();
                if (metadata != null) {
                    for (String s : metadata.keySet()) {
                        if (metadata.get(s).getClass().equals(Image.class)) {
                            audioImageView.setImage((Image) metadata.get(s));
                            audioImageView.setFitHeight(videoPane.getHeight() - 1);
                            audioImageView.setFitWidth(videoPane.getWidth());
                            break;
                        }
                    }
                }
                videoPane.setCenter(audioImageView);
            } else {
                mediaView.setMediaPlayer(player);
                mediaView.setFitWidth(videoPane.getWidth());
                mediaView.setFitHeight(videoPane.getHeight());
                if (!pane.getChildren().contains(mediaView)) {
                    playlistToggle.setSelected(false);
                }
                pane.getChildren().clear();
                pane.getChildren().add(mediaView);
                if (mainStage.isFullScreen()) {
                    if (mediaBtnPane.getChildren().contains(playlistToggle)) {
                        setUpFullScreen();
                    }
                } else {
                    if (videoPane.getCenter() != pane) {
                        videoPane.setCenter(pane);
                    }
                }
                mediaView.setTranslateX((videoPane.getWidth() - mediaView
                        .prefWidth(-1)) / 2);
                mediaView.setTranslateY((videoPane.getHeight() - mediaView
                        .prefHeight(-1)) / 2);
                if (!playListPane.isVisible()) {
                    songTitleLbl.setVisible(true);
                }
                songTitleLbl.setText(songPlayed.getName());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        songTitleLbl.setVisible(false);
                    }
                }, 5000);
            }
        }
    }
    
    private void setUpFullScreen() {
        songTitleLbl.setFont(Font.font(50));
        mediaBtnPane.getChildren().remove(playlistToggle);
        AnchorPane.setTopAnchor(mediaPane, 0.0);
        AnchorPane.setLeftAnchor(controlPane, 100.0);
        AnchorPane.setRightAnchor(controlPane, 100.0);
        AnchorPane.setBottomAnchor(controlPane, 5.0);
        mainStage.setFullScreenExitHint("");
        mainStage.setFullScreen(true);
    }
    
    private void exitFullScreen() {
        songTitleLbl.setFont(Font.font(30));
        if (!mediaBtnPane.getChildren().contains(playlistToggle)) {
            mediaBtnPane.getChildren().add(playlistToggle);
        }
        AnchorPane.setTopAnchor(mediaPane, 30.0);
        AnchorPane.setLeftAnchor(controlPane, 0.0);
        AnchorPane.setRightAnchor(controlPane, 0.0);
        AnchorPane.setBottomAnchor(controlPane, 0.0);
        controlPane.setVisible(true);
    }
    
    private void setVolume(double vol) {
        if (player != null) {
            player.setMute(vol == 0.0);
            player.setVolume(vol);
        }
        boolean mute = volumeBtn.isSelected();
        volumeBtn.setSelected(vol == 0.0);
        if (mute == volumeBtn.isSelected()) {
            applySettings();
            volumeSlider.getTooltip().setText("Volume " + (volumeBtn.isSelected()
                    ? "mute"
                    : (int) setting.getVolume()) + "");
        }
    }
    
    private void setTime(double t) {
        if (player != null) {
            player.seek(duration.multiply(t));
        }
    }
    
    private String formatTime(Duration duration) {
        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationMinutes * 60;
            
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d",
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d",
                        durationMinutes, durationSeconds);
            }
        } else {
            return "--:--";
        }
    }
    
    private void updateValues() {
        if (timeSlider != null && duration != null && player != null) {
            Duration currentTime = player.getCurrentTime();
            String time = formatTime(currentTime);
            Status status = player.getStatus();
            if (status != Status.DISPOSED) {
                sendMessage(new Message<>(Command.TIME, time + "/"
                        + mediaTimeLbl.getText()));
                Platform.runLater(() -> {
                    currentTimeLbl.setText(time);
                });
            }
            timeSlider.setDisable(duration.isUnknown());
            if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO)
                    && !timeSlider.isValueChanging()) {
                Platform.runLater(() -> {
                    timeSlider.setValue(currentTime.divide(duration.toMillis()).toMillis() * 100.0);
                });
                sendMessage(new Message<>(Command.DURATION, (int) timeSlider.getValue()));
            }
        }
    }
    
    private void loadPlaylist(String pl) {
        originalList.clear();
        File f = new File(AppManager.getPlaylistDir().getAbsolutePath()
                + File.separator + pl);
        additionService.setFilePl(f);
        additionService.setOnSucceeded((WorkerStateEvent event) -> {
            isRunning = false;
            onSuccess(false);
        });
        audioImageView.setOpacity(0.1);
        additionService.restart();
        isRunning = true;
        if (setting.isRandom()) {
            indiceRandom = -1;
            randomList.clear();
        }
        indice = -1;
        stop();
    }
    
    public Song addNewSong(String path, ArrayList<Song> list) {
        String id = "pl" + playListChoice.getSelectionModel().getSelectedIndex()
                + "_" + (originalList.size() + list.size());
        return new Song(id, path);
    }
    
    public boolean isSupportedFile(String name) {
        name = name.toLowerCase();
        return name.endsWith(".mp3")
                || name.endsWith(".wav")
                || name.endsWith(".aiff")
                || name.endsWith(".mp4") || name.endsWith(".flv");
    }
    
    private boolean isAudioFile(String name) {
        name = name.toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav")
                || name.endsWith(".aiff");
    }
    
    private void loadList() {
        listPlaylist.clear();
        Comparator<File> comparator = Comparator.comparing(file -> {
            try {
                return Files.readAttributes(Paths.get(file.toURI()), BasicFileAttributes.class).creationTime();
            } catch (IOException e) {
                return null;
            }
        });
        File[] pl = AppManager.getPlaylistDir().listFiles();
        Arrays.sort(pl, comparator);
        for (File p : pl) {
            listPlaylist.add(p.getName());
        }
        sendList();
        playListChoice.getSelectionModel().select(setting.getPlaylist());
    }
    
    private void loadSettings() {
        try {
            AppManager.loadConfig();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            writeLog(ex.getClass().getSimpleName() + " " + ex.getMessage());
        }
        port = setting.getPort();
    }
    
    private void applySettings() {
        boolean random = setting.isRandom(), remote = setting.isRemote(),
                mute = setting.isMute();
        String repeat = setting.getRepeat(), playlist = setting.getPlaylist();
        double volume = setting.getVolume();
        try {
            setting.setRandom(shuffleToggle.isSelected())
                    .setRepeat(repeatToggle.getTooltip().getText())
                    .setRemote(remoteToggle.isSelected())
                    .setVolume(volumeSlider.getValue())
                    .setMute(volumeBtn.isSelected())
                    .setPort(port)
                    .setPlaylist(playListChoice.getValue());
            AppManager.saveConfig();
        } catch (ParserConfigurationException | IllegalArgumentException |
                IllegalAccessException | TransformerException | IOException ex) {
            writeLog(ex.getClass().getSimpleName() + " " + ex.getMessage());
        }
        if (setting.isRandom() != random) {
            indiceRandom = -1;
            randomList.clear();
        }
        if (!setting.getPlaylist().equals(playlist)) {
            loadPlaylist(setting.getPlaylist());
            sendMessage(new Message<>(Command.SETLIST, setting.getPlaylist()));
        }
        if (!setting.isRemote()) {
            stopServer();
            writeLog("Server stopped");
        } else if (setting.isRemote() && setting.isRemote() != remote) {
            startServer();
        } else {
            if (!setting.getRepeat().equals(repeat)) {
                sendMessage(new Message<>(Command.REPEAT, setting.getRepeat()));
            }
            if (setting.isRandom() != random) {
                sendMessage(new Message<>(Command.RANDOM, setting.isRandom()));
            }
            if (setting.getVolume() != volume) {
                sendMessage(new Message<>(Command.VOLUME, (int) volumeSlider.getValue()));
            }
            if (setting.isMute() != mute) {
                sendMessage(new Message<>(Command.MUTE, setting.isMute()));
            }
        }
    }
    
    private void savePlaylist() {
        try (FileWriter fw = new FileWriter(getPlayList())) {
            String pl = "";
            for (Song s : originalList) {
                pl += URLEncoder.encode(s.getPath(),
                        "UTF-8") + "\n";
            }
            fw.write(pl);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            writeLog("IOException " + ex.getMessage());
        }
    }
    
    public void addPlaylist(ArrayList<Song> songs) {
        try (FileWriter fw = new FileWriter(getPlayList(), true)) {
            String pl = "";
            for (Song s : songs) {
                pl += URLEncoder.encode(s.getPath(),
                        "UTF-8") + "\n";
            }
            fw.write(pl);
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            writeLog("IOException " + ex.getMessage());
        }
    }
    
    private File getPlayList() {
        return new File(AppManager.getPlaylistDir()
                + File.separator + setting.getPlaylist());
    }

    // ====#===== Event function ====#=====
    @FXML
    private void windowMinAction(ActionEvent event) {
        mainStage.setIconified(true);
        
    }
    
    @FXML
    private void windowMaxAction(ActionEvent event) {
        maximizeWindow();
        
    }
    
    @FXML
    private void windowCloseAction(ActionEvent event) {
        mainStage.close();
    }
    
    @FXML
    private void mainToolbarDraggedAction(MouseEvent event) {
        if (!maximized) {
            mainStage.setX(event.getScreenX() - x);
            mainStage.setY(event.getScreenY() - y);
        }
    }
    
    @FXML
    private void mainToolbarClickedAction(MouseEvent event) {
        if (event.getClickCount() == 2) {
            maximizeWindow();
        }
    }
    
    @FXML
    private void mainToolbarPressedAction(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }
    
    @FXML
    private void showManual(ActionEvent event) {
        try {
            if (manualStage == null) {
                FXMLLoader loader = new FXMLLoader(new URL(Main.class
                        .getResource("view/fxml/manual.fxml").toString()));
                manualStage = new Stage();
                manualStage.getIcons().add(new Image(Main.class
                        .getResource("view/images/head.png").toString()));
                manualStage.setResizable(false);
                manualStage.setTitle("Manual");
                manualStage.setScene(new Scene((Parent) loader.load()));
                manualStage.initModality(Modality.APPLICATION_MODAL);
                ((ManualController) loader.getController()).setStage(manualStage);
            }
            if (manualStage.isShowing()) {
                manualStage.setIconified(false);
                manualStage.toFront();
            } else {
                manualStage.show();
            }
            
        } catch (IOException ex) {
            writeLog("IOException " + ex.getMessage());
        }
    }
    
    @FXML
    private void showAbout(ActionEvent event) {
        try {
            if (aboutStage == null) {
                FXMLLoader loader = new FXMLLoader(new URL(Main.class
                        .getResource("view/fxml/about.fxml").toString()));
                aboutStage = new Stage();
                aboutStage.getIcons().add(new Image(Main.class
                        .getResource("view/images/head.png").toString()));
                aboutStage.setResizable(false);
                aboutStage.setTitle("About");
                aboutStage.setScene(new Scene((Parent) loader.load()));
                aboutStage.initModality(Modality.APPLICATION_MODAL);
                ((AboutController) loader.getController()).setStage(aboutStage);
                ((AboutController) loader.getController()).setHostServices(hostServices);
            }
            aboutStage.show();
            
        } catch (IOException ex) {
            writeLog("IOException " + ex.getMessage());
        }
    }
    
    @FXML
    private void stopAction(ActionEvent event) {
        stop();
    }
    
    @FXML
    private void playAction(ActionEvent event) {
        playPause();
    }
    
    @FXML
    private void changeSeek(MouseEvent event) {
        if (duration != null && player != null) {
            player.seek(duration.multiply(timeSlider.getValue() / 100.0));
        }
        playList.requestFocus();
        mediaView.requestFocus();
        audioImageView.requestFocus();
    }
    
    @FXML
    private void doubleClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY
                && event.getClickCount() >= 2) {
            if (playList.getSelectionModel().getSelectedIndex() != -1) {
                if (songPlayed != null) {
                    Song temp = songPlayed;
                    songPlayed = null;
                    if (originalList.contains(temp)) {
                        originalList.set(originalList.indexOf(temp), temp);
                    }
                }
                indice = playList.getSelectionModel().getSelectedIndex();
                play(filteredList.get(indice).getId());
            }
        }
    }
    
    private void onSuccess(boolean play) {
        audioImageView.setOpacity(1);
        if (!additionService.getValue().isEmpty()) {
            sendPlaylist();
        }
        if (play) {
            if (songPlayed != null) {
                Song tmp = songPlayed;
                songPlayed = null;
                if (originalList.contains(tmp)) {
                    originalList.set(originalList.indexOf(tmp), tmp);
                }
            }
            if (setting.isRandom()) {
                indiceRandom = (int) (Math.random() * (originalList.size() - 1));
                randomList.add(indiceRandom);
                indiceRandom = randomList.size() - 1;
                indice = randomList.get(indiceRandom);
            } else {
                indice = originalList.size() - additionService.getValue().size();
            }
            play(originalList.get(indice).getId());
        }
    }
    
    @FXML
    private void addFiles(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        String[] ext = {"*.mp3", "*.aiff", "*.wav", "*.mp4", "*.flv"};
        chooser.getExtensionFilters().addAll(new ExtensionFilter("Audio files", ext));
        chooser.setTitle(actionEvent != null && actionEvent.getSource().equals(openFileItem)
                ? "Open file(s)" : "Add file(s)");
        if (!originalList.isEmpty()) {
            File folder = new File(originalList.get(originalList.size() - 1)
                    .getPath());
            chooser.setInitialDirectory(folder.getParentFile());
        }
        List<File> files = chooser.showOpenMultipleDialog(mainStage);
        if (files != null && !files.isEmpty()) {
            additionService.setFiles(files);
            additionService.setOnSucceeded((WorkerStateEvent event) -> {
                onSuccess(!additionService.getValue().isEmpty()
                        && actionEvent != null && actionEvent.getSource().equals(openFileItem));
            });
            audioImageView.setOpacity(0.1);
            additionService.restart();
        }
    }
    
    @FXML
    private void addDir(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (!originalList.isEmpty()) {
            File folder = new File(originalList.get(originalList.size() - 1)
                    .getPath());
            chooser.setInitialDirectory(folder.getParentFile());
        }
        chooser.setTitle(actionEvent.getSource().equals(openFolderItem)
                ? "Open folder(s)" : "Add folder(s)");
        File f = chooser.showDialog(mainStage);
        if (f != null) {
            additionService.setFiles(Arrays.asList(f.listFiles()));
            additionService.setOnSucceeded((WorkerStateEvent event) -> {
                onSuccess(!additionService.getValue().isEmpty()
                        && actionEvent.getSource().equals(openFolderItem));
            });
            audioImageView.setOpacity(0.1);
            additionService.restart();
        }
    }
    
    public ObservableList<Song> getOriginalList() {
        return originalList;
    }
    
    public void addDir(File f, ArrayList<Song> list) {
        for (File file : f.listFiles()) {
            if (isSupportedFile(file.getAbsolutePath())) {
                list.add(addNewSong(file.getAbsolutePath(), list));
            } else if (file.isDirectory()) {
                addDir(file, list);
            }
        }
    }
    
    private void playNext(ArrayList<Song> songs) {
        ArrayList<Song> temp = new ArrayList<>(originalList);
        songs.remove(songPlayed);
        temp.removeAll(songs);
        temp.addAll(temp.indexOf(songPlayed) + 1, songs);
        originalList.setAll(temp);
        sendPlaylist();
        savePlaylist();
    }
    
    private void removeFromList(ArrayList<Song> songs) {
        boolean isPlayed = songs.contains(songPlayed);
        if (!songs.isEmpty()) {
            Status status;
            originalList.removeAll(songs);
            sendPlaylist();
            savePlaylist();
            if (isPlayed && player != null && (status = player.getStatus()) != null
                    && status == Status.PLAYING) {
                stop();
            } else {
                indice = filteredList.indexOf(songPlayed);
            }
            playList.getSelectionModel().clearSelection();
        }
    }
    
    @FXML
    private void playNextAction(ActionEvent event) {
        playNext(new ArrayList<>(playList.getSelectionModel()
                .getSelectedItems()));
    }
    
    @FXML
    private void removeAction(ActionEvent event) {
        removeFromList(new ArrayList<>(playList.getSelectionModel()
                .getSelectedItems()));
    }
    
    @FXML
    private void clearPlaylist(ActionEvent event) {
        removeFromList(new ArrayList<>(originalList));
    }
    
    @FXML
    private void keyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER: {
                if (playList.getSelectionModel().isEmpty()) {
                    break;
                }
                if (songPlayed != null) {
                    Song temp = songPlayed;
                    songPlayed = null;
                    if (originalList.contains(temp)) {
                        originalList.set(originalList.indexOf(temp), temp);
                    }
                }
                indice = playList.getSelectionModel().getSelectedIndex();
                play(filteredList.get(indice).getId());
                break;
            }
            case SPACE: {
                playPause();
                break;
            }
            case N: {
                next();
                break;
            }
            case P: {
                previous();
                break;
            }
            case S: {
                stop();
                break;
            }
            case ADD: {
                volumeSlider.increment();
                break;
            }
            case C: {
                remoteToggle.setSelected(!remoteToggle.isSelected());
                break;
            }
            
            case L: {
                playlistToggle.setSelected(!playlistToggle.isSelected());
                break;
            }
            case R: {
                shuffleToggle.setSelected(!shuffleToggle.isSelected());
                break;
            }
            case B: {
                repeatToggle.setTooltip(new Tooltip(
                        setting.getRepeat().equals("No repeat") ? "Repeat one"
                        : setting.getRepeat().equals("Repeat one") ? "Repeat all"
                        : "No repeat"));
                break;
            }
            case SUBTRACT: {
                volumeSlider.decrement();
                break;
            }
            case M:
                volumeBtn.setSelected(!setting.isMute());
                break;
        }
    }
    
    private boolean isValablePort(String str) {
        try {
            int i = Integer.parseInt(str);
            return i > 1024;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isPlaylistExist(String pl) {
        for (File f : AppManager.getPlaylistDir().listFiles()) {
            if (f.getName().toLowerCase().equals(pl.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private void saveEdition(String name) {
        try {
            if (editDialog.getTitle().contains("Add")) {
                File newPL = new File(AppManager.getPlaylistDir().getAbsolutePath()
                        + File.separator + name);
                newPL.createNewFile();
                setting.setPlaylist(name);
                loadPlaylist(setting.getPlaylist());
            } else {
                Path pathOld = Paths.get(AppManager.getPlaylistDir().getAbsolutePath()
                        + File.separator + setting.getPlaylist()),
                        pathNew = Paths.get(AppManager.getPlaylistDir().getAbsolutePath()
                                + File.separator + name);
                Files.move(pathOld, pathNew);
                setting.setPlaylist(name);
            }
            loadList();
        } catch (IOException e) {
            writeLog("IOException " + e.getMessage());
        }
    }
    
    private void initEditDialog() {
        if (editDialog == null) {
            editDialog = new TextInputDialog();
            ((Stage) editDialog.getDialogPane().getScene().getWindow())
                    .getIcons().add(new Image(Main.class
                                    .getResource("view/images/head.png").toString()));
            editDialog.getDialogPane().setHeader(new Label());
            editDialog.getDialogPane().getButtonTypes().clear();
            final ButtonType okBtn = new ButtonType("Ok");
            editDialog.getDialogPane().getButtonTypes().add(okBtn);
            editDialog.getDialogPane().getButtonTypes().add(new ButtonType("Cancel",
                    ButtonBar.ButtonData.CANCEL_CLOSE));
        }
    }
    
    @FXML
    private void managePlaylist(ActionEvent event) {
        initEditDialog();
        final ButtonType okBtn = editDialog.getDialogPane()
                .getButtonTypes().get(0);
        final TextField textField = editDialog.getEditor();
        textField.setOnKeyReleased((KeyEvent event1) -> {
            if (event1.getCode() == KeyCode.ENTER && !textField.getText().trim().isEmpty() && !isPlaylistExist(textField.getText())) {
                saveEdition(textField.getText());
                editDialog.close();
            } else {
                editDialog.getDialogPane().lookupButton(okBtn)
                        .setDisable(textField.getText().trim().isEmpty()
                                || isPlaylistExist(textField.getText()));
            }
        });
        editDialog.setOnShown((DialogEvent event1) -> {
            editDialog.getDialogPane().lookupButton(okBtn)
                    .setDisable(textField.getText().trim().isEmpty()
                            || isPlaylistExist(editDialog.getEditor().getText()));
            textField.requestFocus();
            textField.selectAll();
        });
        ((Button) editDialog.getDialogPane().lookupButton(okBtn))
                .setOnAction((ActionEvent event1) -> {
                    saveEdition(textField.getText());
                });
        editDialog.getEditor().setText(event.getSource().equals(renamePlBtn)
                ? setting.getPlaylist()
                : "Playlist" + AppManager.getPlaylistDir().list().length);
        editDialog.setTitle(event.getSource().equals(renamePlBtn)
                ? "Rename playlist" : "Add playlist");
        editDialog.setContentText("");
        editDialog.show();
    }
    
    @FXML
    private void deletePlaylist(ActionEvent event) {
        if (deleteDialog == null) {
            deleteDialog = new Alert(Alert.AlertType.NONE,
                    "", new ButtonType("Yes"), new ButtonType("No"));
            deleteDialog.setTitle("Delete playlist");
            ((Stage) deleteDialog.getDialogPane().getScene().getWindow())
                    .getIcons().add(new Image(Main.class
                                    .getResource("view/images/head.png").toString()));
            deleteDialog.initModality(Modality.APPLICATION_MODAL);
            deleteDialog.setOnHiding((DialogEvent event1) -> {
                if (deleteDialog.getResult().getText().equals("Yes")) {
                    File source = new File(AppManager.getPlaylistDir()
                            .getAbsolutePath()
                            + File.separator + setting.getPlaylist());
                    source.delete();
                    setting.setPlaylist("Default");
                    loadList();
                    loadPlaylist(setting.getPlaylist());
                }
            });
        }
        deleteDialog.setContentText("Do you want to delete '"
                + setting.getPlaylist() + "' playlist");
        deleteDialog.show();
    }
    
    @FXML
    private void onDraggedFiles(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        dragEvent.setDropCompleted(dragboard.hasFiles());
        if (dragboard.hasFiles()) {
            additionService.setFiles(dragboard.getFiles());
            additionService.setOnSucceeded((WorkerStateEvent event) -> {
                onSuccess(!additionService.getValue().isEmpty()
                        && !dragEvent.getSource().equals(playList));
            });
            audioImageView.setOpacity(0.1);
            additionService.restart();
        }
        dragEvent.consume();
    }
    
    @FXML
    private void onDragOverFiles(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            List<File> files = event.getDragboard().getFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    if (isSupportedFile(f.getAbsolutePath())) {
                        event.acceptTransferModes(TransferMode.COPY);
                        break;
                    }
                } else {
                    event.acceptTransferModes(TransferMode.COPY);
                    break;
                }
            }
        } else {
            event.consume();
        }
    }
    
    @FXML
    private void clearAction(ActionEvent event) {
        searchTxt.setText("");
    }

    // ====#===== Remote control function ====#=====
    private void sendMessage(Message<?> response) {
        if (messageSender != null) {
            messageSender.addMessage(response);
        }
    }
    
    private void sendPlaylist() {
        Song[] songs = new Song[originalList.size()];
        originalList.toArray(songs);
        sendMessage(new Message<>(Command.PLAYLIST, songs));
    }
    
    private void sendList() {
        String[] pl = new String[listPlaylist.size()];
        listPlaylist.toArray(pl);
        sendMessage(new Message<>(Command.LIST, pl));
    }
    
    public void startServer() {
        if (setting.isRemote()) {
            Platform.runLater(() -> {
                remoteToggle.setTooltip(new Tooltip("Stop server"));
                statusLbl.setText("Device connected: ");
                String ip = getIpAddresses();
                deviceLbl.setText("no device (Connect to "+
                        (ip == null ? "Your IP adress" : ip)
                        +":"+ setting.getPort() + ")");
            });
            socketManager.startServer();
        }
    }
    
    private void stopServer() {
        remoteToggle.setTooltip(new Tooltip("Start server"));
        statusLbl.setText("disabled");
        deviceLbl.setText("");
        socketManager.stopServer();
    }
    
    private String getIpAddresses(){
        try{
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            for(NetworkInterface inter : Collections.list(interfaces)){
                if(inter.isLoopback() || inter.isVirtual() || !inter.isUp())
                    continue;
                for(InterfaceAddress inet : inter.getInterfaceAddresses()){
                    if(inet.getBroadcast() == null || inet
                            .getNetworkPrefixLength() == 8)
                        continue;
                    return inet.getAddress().getHostAddress();
                }
            }
        }catch(SocketException e){
            writeLog(e.getClass().getSimpleName() + " " + e.getMessage());
        }
        return null;
    }
    
    public void replyRefreshing() {
        sendMessage(new Message<>(Command.DEVICE,
                null));
        sendMessage(new Message<>(Command.VOLUME,
                (int) volumeSlider.getValue()));
        sendMessage(new Message<>(Command.MUTE,
                setting.isMute()));
        sendPlaylist();
        sendList();
        if (player != null) {
            Status status = player.getStatus();
            if (status == Status.PLAYING) {
                sendMessage(new Message<>(Command.PLAY,
                        songPlayed));
            }
            if (status == Status.PAUSED) {
                sendMessage(new Message<>(Command.PLAY,
                        songPlayed));
                sendMessage(new Message<>(Command.PAUSE,
                        songPlayed));
            }
        } else {
            sendMessage(new Message<>(Command.STOP, null));
        }
        sendMessage(new Message<>(Command.SETLIST,
                setting.getPlaylist()));
        sendMessage(new Message<>(Command.RANDOM, setting.isRandom()));
        sendMessage(new Message<>(Command.REPEAT, setting.getRepeat()));
    }
    
    public void handleMessage(Message<?> message) {
        if (isRunning) {
            return;
        }
        switch (message.getCommand()) {
            case PREVIOUS: {
                Platform.runLater(() -> {
                    previous();
                });
                break;
            }
            case PLAY: {
                Song song = (Song) message.getData();
                if (originalList.contains(song)) {
                    Platform.runLater(() -> {
                        if (songPlayed != null) {
                            Song temp = songPlayed;
                            songPlayed = null;
                            if (originalList.contains(temp)) {
                                originalList.set(originalList.indexOf(temp), temp);
                            }
                        }
                        indice = originalList.indexOf(song);
                        play(song.getId());
                    });
                }
                break;
            }
            case PLAYNEXT: {
                Song[] data = (Song[]) message.getData();
                ArrayList<Song> songs = new ArrayList<>(data.length);
                songs.addAll(Arrays.asList(data));
                Platform.runLater(() -> {
                    playNext(songs);
                });
                break;
            }
            case REMOVE: {
                Song[] data = (Song[]) message.getData();
                ArrayList<Song> songs = new ArrayList<>(data.length);
                songs.addAll(Arrays.asList(data));
                Platform.runLater(() -> {
                    removeFromList(songs);
                });
                break;
            }
            
            case PLAYPAUSE: {
                Platform.runLater(() -> {
                    playPause();
                });
                break;
            }
            case NEXT: {
                Platform.runLater(() -> {
                    next();
                });
                break;
            }
            case STOP: {
                Platform.runLater(() -> {
                    stop();
                });
                break;
            }
            case VOLUME: {
                Platform.runLater(() -> {
                    volumeSlider.setValue((Integer) message.getData());
                });
                break;
            }
            
            case MUTE: {
                Platform.runLater(() -> {
                    volumeBtn.setSelected((Boolean) message
                            .getData());
                });
                break;
            }
            
            case DURATION: {
                Platform.runLater(() -> {
                    timeSlider.setValue((Integer) message.getData());
                    changeSeek(null);
                });
                break;
            }
            case RANDOM: {
                Platform.runLater(() -> {
                    shuffleToggle.setSelected((Boolean) message
                            .getData());
                });
                break;
            }
            
            case REPEAT: {
                Platform.runLater(() -> {
                    repeatToggle.setTooltip(new Tooltip((String) message
                            .getData()));
                });
                break;
            }
            case DEVICE: {
                String data = ((String) message.getData());
                Platform.runLater(() -> {
                    deviceLbl.setText(data);
                    writeLog("Device connected " + data);
                });
                break;
            }
            
            case SETLIST: {
                Platform.runLater(() -> {
                    playListChoice.getSelectionModel().select((Integer) message
                            .getData());
                });
                break;
            }
            case REFRESH: {
                replyRefreshing();
                break;
            }
            
            case DOWNLOAD: {
                Song[] songs = (Song[]) message.getData();
                if (downloadManager.getQueueList().isEmpty()) {
                    downloadManager.getQueueList().addAll(Arrays.asList(songs));
                    new Thread(downloadManager).start();
                } else {
                    downloadManager.getQueueList().addAll(Arrays.asList(songs));
                }
                break;
            }
        }
    }
}
