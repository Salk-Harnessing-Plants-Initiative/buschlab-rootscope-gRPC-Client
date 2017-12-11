package gui.controllers;

import connection.MMClient;
import gui.InitFxGui;
import gui.utils.ImgViewZoomable;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.MaskerPane;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class RootLayoutController {
    @FXML
    private BorderPane rootLayout;
    @FXML
    private StackPane rootStack;
    @FXML
    private MaskerPane rootMasker;
    @FXML
    private ButtonBar buttonBar;

    private MMClient client;
    private ImgViewZoomable imgView = new ImgViewZoomable();

    @FXML
    private void initialize() {

        rootStack.getChildren().add(imgView);
        rootMasker.setVisible(false);

        System.out.println("initialize " + rootLayout.getCenter().toString());

        Button getImgButton = new Button("fetch image");
        Button exportImageButton = new Button("export image");
        Button exitButton = new Button("Exit");

        buttonBar.getButtons().addAll(getImgButton,exportImageButton,exitButton);
        buttonBar.setPadding(new Insets(5,5,5,5));

        getImgButton.setOnAction(e -> getImageFromServer());
        exportImageButton.setOnAction(e -> saveImage());
        exitButton.setOnAction(e -> System.exit(0));
    }

    public RootLayoutController() {
    }

    public void setClient(MMClient client) {
        this.client = client;
    }

    private void getImageFromServer() {

        rootMasker.setVisible(true);

        int width = 400;
        int height = 400;

        imgView.setFitHeight(height);
        imgView.setFitWidth(width);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
            imgView.setImage(null);
            updateMessage("fetching image from server...");
            imgView.setImage(client.getImgData("hey"));
            imgView.activateZoom(imgView.getFitWidth(), imgView.getFitHeight());
            imgView.setPreserveRatio(true);
            imgView.setViewport(new Rectangle2D(0, 0, width, height));
            return null;
            }
        };

        rootMasker.textProperty().bind(task.messageProperty());
        new Thread(task).start();

        task.setOnSucceeded(WorkerStateEvent -> rootMasker.setVisible(false));
    }

    private void saveImage() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select directory");
        File defaultDirectory = new File(System.getProperty("user.home"));
        directoryChooser.setInitialDirectory(defaultDirectory);
        File selectedDirectory = directoryChooser.showDialog(InitFxGui.getPrimaryStage());

        if (selectedDirectory != null) {
            File outFile = new File(selectedDirectory.getAbsolutePath() + "pic.png" );
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(imgView.getImage(), null), "png", outFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}


