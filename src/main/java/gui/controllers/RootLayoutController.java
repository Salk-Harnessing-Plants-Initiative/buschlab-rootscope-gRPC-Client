package gui.controllers;

import connection.MMClient;
import gui.utils.ImgViewZoomable;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.MaskerPane;

public class RootLayoutController {
    @FXML
    private BorderPane rootLayout;
    @FXML
    private StackPane rootStack;
    @FXML
    private MaskerPane rootMasker;

    private MMClient client;
    private ImgViewZoomable imgView = new ImgViewZoomable();

    @FXML
    private void initialize() {
        rootMasker.setVisible(false);
        rootStack.getChildren().add(imgView);
        System.out.println("initialize " + rootLayout.getCenter().toString());
    }

    public RootLayoutController(){
    }

    public void setClient(MMClient client) {
        this.client = client;
    }

    public void getImageFromServer() {

        int width = 800;
        int heigth = 500;
        rootMasker.setVisible(true);

        Image mmImg = client.getImgData("hey");

        imgView.setFitHeight(heigth);
        imgView.setFitWidth(width);
        imgView.activateZoom(mmImg.getWidth(),mmImg.getHeight());
        imgView.setPreserveRatio(true);
        imgView.setViewport(new Rectangle2D(0,0,width, heigth));
        imgView.setImage(mmImg);

        rootMasker.setVisible(false);
    }
}
