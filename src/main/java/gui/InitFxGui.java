package gui;

import app.Main;
import connection.MMClient;
import gui.controllers.RootLayoutController;
import gui.logging.Log;
import gui.logging.LogHandler;
import gui.logging.LogView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.MasterDetailPane;

import static app.Main.logger;

import java.io.IOException;
import java.util.logging.Level;

public class InitFxGui extends Application{

    private static Stage primaryStage;

    private final static Log queue = new Log();
    private final static LogHandler handlerFx = new LogHandler(queue);
    private static LogView logView;

    private static MMClient mmClient;

    @Override
    public void start(Stage inPrimaryStage) {

        handlerFx.setLevel(Level.ALL);
        logger.addHandler(handlerFx);

        logView = new LogView(queue);

        primaryStage = inPrimaryStage;
        primaryStage.setTitle("rootscope client FX Gui");

        final MasterDetailPane masterDetailPane = new MasterDetailPane();

        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(InitFxGui.class.getResource("/views/rootLayout.fxml"));
            BorderPane root = loader.load();

            masterDetailPane.setMasterNode(root);
            masterDetailPane.setDetailNode(logView);
            masterDetailPane.setDetailSide(Side.RIGHT);
            masterDetailPane.setDividerPosition(0.6);
            masterDetailPane.setShowDetailNode(true);

            Scene scene = new Scene(masterDetailPane);
            scene.getStylesheets().add(LogView.class.getResource("/css/log-view.css").toExternalForm());

            RootLayoutController rootLayoutController = loader.getController();
            rootLayoutController.setClient(mmClient);

            String[] props =  mmClient.getCameraProperties("test").split("/");
            for (String entry : props)
                logger.info("camera property: " + entry);

            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            logger.severe(Main.StackTraceToString(e));
        }
    }

    public static void run(MMClient client) {
        mmClient = client;
        Application.launch(InitFxGui.class);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
